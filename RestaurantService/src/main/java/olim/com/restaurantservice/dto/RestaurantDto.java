package olim.com.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import olim.com.restaurantservice.entity.Restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Objects for Restaurant operations
 */
public class RestaurantDto {

    /**
     * DTO for creating a new restaurant
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRestaurantRequest {
        @NotBlank(message = "Restaurant name is required")
        private String name;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @NotNull(message = "Cuisine type is required")
        private Restaurant.CuisineType cuisineType;

        @PositiveOrZero(message = "Delivery fee must be positive")
        private BigDecimal deliveryFee = BigDecimal.valueOf(2.99);

        @PositiveOrZero(message = "Minimum order must be positive")
        private BigDecimal minimumOrder = BigDecimal.valueOf(10.00);
    }

    /**
     * DTO for updating restaurant details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRestaurantRequest {
        @NotBlank(message = "Restaurant name is required")
        private String name;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @NotNull(message = "Cuisine type is required")
        private Restaurant.CuisineType cuisineType;

        @PositiveOrZero(message = "Delivery fee must be positive")
        private BigDecimal deliveryFee;

        @PositiveOrZero(message = "Minimum order must be positive")
        private BigDecimal minimumOrder;
    }

    /**
     * DTO for restaurant response (what customers see)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantResponse {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private Restaurant.CuisineType cuisineType;
        private boolean isOpen;
        private BigDecimal deliveryFee;
        private BigDecimal minimumOrder;
        private BigDecimal averageRating;
        private LocalDateTime createdAt;

        // Constructor from entity
        public RestaurantResponse(Restaurant restaurant) {
            this.id = restaurant.getId();
            this.name = restaurant.getName();
            this.description = restaurant.getDescription();
            this.address = restaurant.getAddress();
            this.phoneNumber = restaurant.getPhoneNumber();
            this.cuisineType = restaurant.getCuisineType();
            this.isOpen = restaurant.isOpen();
            this.deliveryFee = restaurant.getDeliveryFee();
            this.minimumOrder = restaurant.getMinimumOrder();
            this.averageRating = restaurant.getAverageRating();
            this.createdAt = restaurant.getCreatedAt();
        }
    }

    /**
     * DTO for restaurant details with menu items
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantDetailResponse {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private Restaurant.CuisineType cuisineType;
        private boolean isOpen;
        private BigDecimal deliveryFee;
        private BigDecimal minimumOrder;
        private BigDecimal averageRating;
        private LocalDateTime createdAt;
        private List<MenuItemDto.MenuItemResponse> menuItems;

        // Constructor from entity
        public RestaurantDetailResponse(Restaurant restaurant, List<MenuItemDto.MenuItemResponse> menuItems) {
            this.id = restaurant.getId();
            this.name = restaurant.getName();
            this.description = restaurant.getDescription();
            this.address = restaurant.getAddress();
            this.phoneNumber = restaurant.getPhoneNumber();
            this.cuisineType = restaurant.getCuisineType();
            this.isOpen = restaurant.isOpen();
            this.deliveryFee = restaurant.getDeliveryFee();
            this.minimumOrder = restaurant.getMinimumOrder();
            this.averageRating = restaurant.getAverageRating();
            this.createdAt = restaurant.getCreatedAt();
            this.menuItems = menuItems;
        }
    }

    /**
     * DTO for restaurant owner's view (includes owner-specific info)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerRestaurantResponse {
        private Long id;
        private String name;
        private String description;
        private String address;
        private String phoneNumber;
        private Restaurant.CuisineType cuisineType;
        private boolean isOpen;
        private BigDecimal deliveryFee;
        private BigDecimal minimumOrder;
        private BigDecimal averageRating;
        private Long ownerId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor from entity
        public OwnerRestaurantResponse(Restaurant restaurant) {
            this.id = restaurant.getId();
            this.name = restaurant.getName();
            this.description = restaurant.getDescription();
            this.address = restaurant.getAddress();
            this.phoneNumber = restaurant.getPhoneNumber();
            this.cuisineType = restaurant.getCuisineType();
            this.isOpen = restaurant.isOpen();
            this.deliveryFee = restaurant.getDeliveryFee();
            this.minimumOrder = restaurant.getMinimumOrder();
            this.averageRating = restaurant.getAverageRating();
            this.ownerId = restaurant.getOwnerId();
            this.createdAt = restaurant.getCreatedAt();
            this.updatedAt = restaurant.getUpdatedAt();
        }
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