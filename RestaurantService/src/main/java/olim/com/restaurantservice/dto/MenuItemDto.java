package olim.com.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import olim.com.restaurantservice.entity.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Objects for MenuItem operations
 */
public class MenuItemDto {

    /**
     * DTO for creating a new menu item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateMenuItemRequest {
        @NotBlank(message = "Item name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @NotNull(message = "Category is required")
        private MenuItem.Category category;

        private boolean vegetarian = false;
    }

    /**
     * DTO for updating menu item details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMenuItemRequest {
        @NotBlank(message = "Item name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        @NotNull(message = "Category is required")
        private MenuItem.Category category;

        private boolean vegetarian = false;
    }

    /**
     * DTO for menu item response
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private MenuItem.Category category;
        private boolean available;
        private boolean vegetarian;
        private Long restaurantId;
        private String restaurantName;
        private LocalDateTime createdAt;

        // Constructor from entity
        public MenuItemResponse(MenuItem menuItem) {
            this.id = menuItem.getId();
            this.name = menuItem.getName();
            this.description = menuItem.getDescription();
            this.price = menuItem.getPrice();
            this.category = menuItem.getCategory();
            this.available = menuItem.isAvailable();
            this.vegetarian = menuItem.isVegetarian();
            this.restaurantId = menuItem.getRestaurant().getId();
            this.restaurantName = menuItem.getRestaurant().getName();
            this.createdAt = menuItem.getCreatedAt();
        }
    }

    /**
     * DTO for simple menu item response (without restaurant details)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleMenuItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private MenuItem.Category category;
        private boolean available;
        private boolean vegetarian;

        // Constructor from entity
        public SimpleMenuItemResponse(MenuItem menuItem) {
            this.id = menuItem.getId();
            this.name = menuItem.getName();
            this.description = menuItem.getDescription();
            this.price = menuItem.getPrice();
            this.category = menuItem.getCategory();
            this.available = menuItem.isAvailable();
            this.vegetarian = menuItem.isVegetarian();
        }
    }

    /**
     * DTO for menu item owner view (includes more details)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerMenuItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private MenuItem.Category category;
        private boolean available;
        private boolean vegetarian;
        private Long restaurantId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructor from entity
        public OwnerMenuItemResponse(MenuItem menuItem) {
            this.id = menuItem.getId();
            this.name = menuItem.getName();
            this.description = menuItem.getDescription();
            this.price = menuItem.getPrice();
            this.category = menuItem.getCategory();
            this.available = menuItem.isAvailable();
            this.vegetarian = menuItem.isVegetarian();
            this.restaurantId = menuItem.getRestaurant().getId();
            this.createdAt = menuItem.getCreatedAt();
            this.updatedAt = menuItem.getUpdatedAt();
        }
    }
}