package olim.com.orderservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import olim.com.orderservice.client.RestaurantClient;
import olim.com.orderservice.dto.OrderDto.*;
import olim.com.orderservice.dto.RestaurantDto;
import olim.com.orderservice.entity.Order;
import olim.com.orderservice.entity.OrderItem;
import olim.com.orderservice.repository.OrderRepository;
import olim.com.orderservice.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Order Service - Business logic for order management
 *
 * Handles order creation, validation, status updates, and retrieval
 * Uses Feign client to communicate with Restaurant Service
 */
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired(required = false) // Make optional - won't break if RabbitMQ isn't available
    private EventPublisherService eventPublisher;

    /**
     * Create a new order
     *
     * @param createRequest order creation request
     * @param customerId customer placing the order
     * @return created order
     */
    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "createOrderFallback")
    public Order createOrder(CreateOrderRequest createRequest, Long customerId) {
        System.out.println("🍔 Starting order creation for customer: " + customerId);

        // Step 1: Validate restaurant
        RestaurantDto.ApiResponse<RestaurantDto.RestaurantResponse> restaurantResponse =
                validateRestaurant(createRequest.getRestaurantId());

        if (!restaurantResponse.isSuccess()) {
            throw new RuntimeException("Failed to validate restaurant: " + restaurantResponse.getMessage());
        }

        RestaurantDto.RestaurantResponse restaurant = restaurantResponse.getData();
        if (!restaurant.isOpen()) {
            throw new RuntimeException("Restaurant is currently closed");
        }

        System.out.println("✅ Restaurant validated: " + restaurant.getName());

        // Step 2: Validate menu items and calculate total BEFORE creating order
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItemData> validatedItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : createRequest.getOrderItems()) {
            // Validate menu item
            RestaurantDto.MenuItemResponse menuItem = validateMenuItem(
                    createRequest.getRestaurantId(),
                    itemRequest.getMenuItemId()
            ).getData();

            if (!menuItem.isAvailable()) {
                throw new RuntimeException("Menu item '" + menuItem.getName() + "' is not available");
            }

            // Calculate item total price
            BigDecimal itemTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            // Store validated item data for later
            validatedItems.add(new OrderItemData(
                    itemRequest.getMenuItemId(),
                    menuItem.getName(),
                    itemRequest.getQuantity(),
                    itemTotal
            ));
        }

        // Step 3: Calculate final total with delivery fee
        BigDecimal totalAmount = subtotal.add(restaurant.getDeliveryFee());

        // Step 4: Check minimum order requirement
        if (subtotal.compareTo(restaurant.getMinimumOrder()) < 0) {
            throw new RuntimeException("Order subtotal must be at least $" + restaurant.getMinimumOrder() +
                    " (excluding delivery fee of $" + restaurant.getDeliveryFee() + ")");
        }

        System.out.println("💰 Order total calculated: $" + totalAmount);

        // Step 5: Create order entity with calculated total
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(createRequest.getRestaurantId());
        order.setRestaurantName(restaurant.getName());
        order.setDeliveryAddress(createRequest.getDeliveryAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount); // Set calculated total BEFORE saving

        // Step 6: Save order (now with valid total amount)
        order = orderRepository.save(order);
        System.out.println("✅ Order saved with ID: " + order.getId());

        // Step 7: Create and save order items
        for (OrderItemData itemData : validatedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(itemData.menuItemId);
            orderItem.setMenuItemName(itemData.menuItemName);
            orderItem.setQuantity(itemData.quantity);
            orderItem.setPrice(itemData.price);
            orderItem.setOrder(order);

            orderItemRepository.save(orderItem);
        }

        System.out.println("🎉 Order created successfully with " + validatedItems.size() + " items");

        // 🚀 Publish OrderPlacedEvent for async processing (non-blocking)
        if (eventPublisher != null) {
            try {
                Order finalOrder = orderRepository.findById(order.getId()).orElse(order);
                eventPublisher.publishOrderPlacedEvent(
                        finalOrder,
                        "customer@example.com", // TODO: Get from auth service
                        "Customer Name"         // TODO: Get from auth service
                );
                System.out.println("📨 Order event published successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to publish order event: " + e.getMessage() + " (Order creation was successful)");
            }
        } else {
            System.out.println("📨 Event publisher not available - skipping event publishing");
        }

        return orderRepository.findById(order.getId()).orElse(order); // Reload with items
    }

    /**
     * Fallback method for order creation when restaurant service is down
     */
    public Order createOrderFallback(CreateOrderRequest createRequest, Long customerId, Exception ex) {
        throw new RuntimeException("Restaurant service is currently unavailable. Please try again later.");
    }

    /**
     * Get all orders for a customer
     */
    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Get a specific order by ID and customer ID
     */
    public Optional<Order> getCustomerOrder(Long orderId, Long customerId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getCustomerId().equals(customerId));
    }

    /**
     * Get all orders for a restaurant (for restaurant owners)
     */
    public List<Order> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Get pending orders for a restaurant
     */
    public List<Order> getPendingRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, Order.OrderStatus.PENDING);
    }

    /**
     * Update order status (for restaurant owners)
     */
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus, Long restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify the order belongs to this restaurant
        if (!order.getRestaurantId().equals(restaurantId)) {
            throw new RuntimeException("You can only update orders for your restaurant");
        }

        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        String oldStatus = order.getStatus().name();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // 🚀 Publish OrderStatusChangedEvent (non-blocking)
        if (eventPublisher != null) {
            try {
                eventPublisher.publishOrderStatusChangedEvent(
                        updatedOrder,
                        oldStatus,
                        newStatus.name(),
                        "restaurant", // Changed by restaurant
                        "customer@example.com" // TODO: Get from auth service
                );
                System.out.println("📨 Order status change event published");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to publish status change event: " + e.getMessage());
            }
        }

        return updatedOrder;
    }

    /**
     * Cancel an order (for customers)
     */
    public Order cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify the order belongs to this customer
        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        // Check if order can be cancelled
        if (order.getStatus() != Order.OrderStatus.PENDING && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled in " + order.getStatus() + " status");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Get order statistics for a customer
     */
    public CustomerOrderStats getCustomerStats(Long customerId) {
        long totalOrders = orderRepository.countByCustomerId(customerId);
        List<Order> activeOrders = orderRepository.findActiveOrdersByCustomer(customerId);

        return new CustomerOrderStats(totalOrders, activeOrders.size());
    }

    /**
     * Get order statistics for a restaurant
     */
    public RestaurantOrderStats getRestaurantStats(Long restaurantId) {
        long totalOrders = orderRepository.countByRestaurantId(restaurantId);
        long pendingOrders = orderRepository.findByRestaurantIdAndStatus(
                restaurantId, Order.OrderStatus.PENDING).size();

        return new RestaurantOrderStats(totalOrders, pendingOrders);
    }

    // Private helper methods

    @CircuitBreaker(name = "restaurant-service")
    public RestaurantDto.ApiResponse<RestaurantDto.RestaurantResponse> validateRestaurant(Long restaurantId) {
        try {
            return restaurantClient.getRestaurant(restaurantId);
        } catch (Exception e) {
            System.out.println("❌ Error calling restaurant service: " + e.getMessage());
            return new RestaurantDto.ApiResponse<>(false, "Restaurant service unavailable: " + e.getMessage(), null);
        }
    }

    @CircuitBreaker(name = "restaurant-service")
    public RestaurantDto.ApiResponse<RestaurantDto.MenuItemResponse> validateMenuItem(Long restaurantId, Long menuItemId) {
        try {
            RestaurantDto.ApiResponse<RestaurantDto.MenuItemResponse> response =
                    restaurantClient.getMenuItem(restaurantId, menuItemId);

            if (!response.isSuccess()) {
                throw new RuntimeException("Failed to get menu item: " + response.getMessage());
            }

            return response;
        } catch (Exception e) {
            System.out.println("❌ Error calling restaurant service for menu item: " + e.getMessage());
            throw new RuntimeException("Failed to validate menu item: " + e.getMessage());
        }
    }

    private boolean isValidStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == Order.OrderStatus.CONFIRMED || newStatus == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == Order.OrderStatus.PREPARING || newStatus == Order.OrderStatus.CANCELLED;
            case PREPARING -> newStatus == Order.OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    // Statistics classes
    public static class CustomerOrderStats {
        private final long totalOrders;
        private final long activeOrders;

        public CustomerOrderStats(long totalOrders, long activeOrders) {
            this.totalOrders = totalOrders;
            this.activeOrders = activeOrders;
        }

        public long getTotalOrders() { return totalOrders; }
        public long getActiveOrders() { return activeOrders; }
    }

    public static class RestaurantOrderStats {
        private final long totalOrders;
        private final long pendingOrders;

        public RestaurantOrderStats(long totalOrders, long pendingOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
        }

        public long getTotalOrders() { return totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
    }

    /**
     * Helper class to store validated order item data
     */
    private static class OrderItemData {
        final Long menuItemId;
        final String menuItemName;
        final Integer quantity;
        final BigDecimal price;

        OrderItemData(Long menuItemId, String menuItemName, Integer quantity, BigDecimal price) {
            this.menuItemId = menuItemId;
            this.menuItemName = menuItemName;
            this.quantity = quantity;
            this.price = price;
        }
    }
}