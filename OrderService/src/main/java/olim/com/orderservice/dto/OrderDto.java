package olim.com.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import olim.com.orderservice.entity.Order;
import olim.com.orderservice.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Objects for Order operations
 */
public class OrderDto {

    /**
     * DTO for creating a new order
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotNull(message = "Restaurant ID is required")
        private Long restaurantId;

        @NotBlank(message = "Delivery address is required")
        private String deliveryAddress;

        @NotEmpty(message = "Order items are required")
        private List<OrderItemRequest> orderItems;
    }

    /**
     * DTO for order item in create request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }

    /**
     * DTO for order response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private Long id;
        private Long customerId;
        private Long restaurantId;
        private String restaurantName;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private String deliveryAddress;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<OrderItemResponse> orderItems;

        // Constructor from entity
        public OrderResponse(Order order) {
            this.id = order.getId();
            this.customerId = order.getCustomerId();
            this.restaurantId = order.getRestaurantId();
            this.restaurantName = order.getRestaurantName();
            this.status = order.getStatus();
            this.totalAmount = order.getTotalAmount();
            this.deliveryAddress = order.getDeliveryAddress();
            this.createdAt = order.getCreatedAt();
            this.updatedAt = order.getUpdatedAt();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemResponse::new)
                    .collect(Collectors.toList());
        }
    }

    /**
     * DTO for simple order response (without items)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleOrderResponse {
        private Long id;
        private Long restaurantId;
        private String restaurantName;
        private Order.OrderStatus status;
        private BigDecimal totalAmount;
        private String deliveryAddress;
        private LocalDateTime createdAt;

        // Constructor from entity
        public SimpleOrderResponse(Order order) {
            this.id = order.getId();
            this.restaurantId = order.getRestaurantId();
            this.restaurantName = order.getRestaurantName();
            this.status = order.getStatus();
            this.totalAmount = order.getTotalAmount();
            this.deliveryAddress = order.getDeliveryAddress();
            this.createdAt = order.getCreatedAt();
        }
    }

    /**
     * DTO for order item response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal price;

        // Constructor from entity
        public OrderItemResponse(OrderItem orderItem) {
            this.id = orderItem.getId();
            this.menuItemId = orderItem.getMenuItemId();
            this.menuItemName = orderItem.getMenuItemName();
            this.quantity = orderItem.getQuantity();
            this.price = orderItem.getPrice();
        }
    }

    /**
     * DTO for updating order status
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateOrderStatusRequest {
        @NotNull(message = "Status is required")
        private Order.OrderStatus status;
    }

    /**
     * Generic API response wrapper
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        // Static factory methods
        public static <T> ApiResponse<T> success(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> success(String message) {
            return new ApiResponse<>(true, message, null);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}