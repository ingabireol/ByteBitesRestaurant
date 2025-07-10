package olim.com.orderservice.controller;

import jakarta.validation.Valid;
import olim.com.orderservice.client.RestaurantClient;
import olim.com.orderservice.dto.OrderDto.*;
import olim.com.orderservice.dto.RestaurantDto;
import olim.com.orderservice.entity.Order;
import olim.com.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Order operations
 *
 * Handles HTTP requests for order management
 * Uses role-based security and resource ownership validation
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Create a new order (requires CUSTOMER role)
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            Order order = orderService.createOrder(request, customerId);
            OrderResponse response = new OrderResponse(order);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * Get customer's orders (requires CUSTOMER role)
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<SimpleOrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            List<Order> orders = orderService.getCustomerOrders(customerId);
            List<SimpleOrderResponse> response = orders.stream()
                    .map(SimpleOrderResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Get specific order details (requires CUSTOMER role and ownership)
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            Order order = orderService.getCustomerOrder(orderId, customerId)
                    .orElseThrow(() -> new RuntimeException("Order not found or access denied"));

            OrderResponse response = new OrderResponse(order);
            return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve order: " + e.getMessage()));
        }
    }

    /**
     * Cancel an order (requires CUSTOMER role and ownership)
     */
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            Order order = orderService.cancelOrder(orderId, customerId);
            OrderResponse response = new OrderResponse(order);

            return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    /**
     * Get orders for restaurant (requires RESTAURANT_OWNER role)
     */
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<List<SimpleOrderResponse>>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            // Note: In a real app, you'd verify the user owns this restaurant
            // For simplicity, we're allowing any restaurant owner to view any restaurant's orders
            List<Order> orders = orderService.getRestaurantOrders(restaurantId);
            List<SimpleOrderResponse> response = orders.stream()
                    .map(SimpleOrderResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurant orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve restaurant orders: " + e.getMessage()));
        }
    }

    /**
     * Get pending orders for restaurant (requires RESTAURANT_OWNER role)
     */
    @GetMapping("/restaurant/{restaurantId}/pending")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<List<SimpleOrderResponse>>> getPendingRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            List<Order> orders = orderService.getPendingRestaurantOrders(restaurantId);
            List<SimpleOrderResponse> response = orders.stream()
                    .map(SimpleOrderResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Pending orders retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pending orders: " + e.getMessage()));
        }
    }

    /**
     * Update order status (requires RESTAURANT_OWNER role)
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam Long restaurantId) {
        try {
            Long ownerId = Long.parseLong(userId);
            Order order = orderService.updateOrderStatus(orderId, request.getStatus(), restaurantId);
            OrderResponse response = new OrderResponse(order);

            return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }

    /**
     * Get customer order statistics
     */
    @GetMapping("/stats/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderService.CustomerOrderStats>> getCustomerStats(
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long customerId = Long.parseLong(userId);
            OrderService.CustomerOrderStats stats = orderService.getCustomerStats(customerId);

            return ResponseEntity.ok(ApiResponse.success("Customer statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve customer statistics: " + e.getMessage()));
        }
    }

    /**
     * Get restaurant order statistics
     */
    @GetMapping("/stats/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OrderService.RestaurantOrderStats>> getRestaurantStats(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            OrderService.RestaurantOrderStats stats = orderService.getRestaurantStats(restaurantId);

            return ResponseEntity.ok(ApiResponse.success("Restaurant statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve restaurant statistics: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Order service is running"));
    }

    /**
     * Debug endpoint to test restaurant service communication
     */
    @GetMapping("/debug/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<Object>> debugRestaurant(@PathVariable Long restaurantId) {
        try {
            RestaurantDto.ApiResponse<RestaurantDto.RestaurantResponse> response =
                    orderService.validateRestaurant(restaurantId);
            return ResponseEntity.ok(ApiResponse.success("Restaurant service response", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to call restaurant service: " + e.getMessage()));
        }
    }

    @GetMapping("/debug/restaurant/{restaurantId}/{itemId}")
    public ResponseEntity<ApiResponse<Object>> debugMenuItem(@PathVariable Long restaurantId, @PathVariable Long itemId) {
        try {
            RestaurantDto.ApiResponse<RestaurantDto.MenuItemResponse> response =
                    orderService.validateMenuItem(restaurantId,itemId);
            return ResponseEntity.ok(ApiResponse.success("Restaurant service response", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to call restaurant service: " + e.getMessage()));
        }
    }
}