package olim.com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTOs for Restaurant Service communication
 * These mirror the DTOs from Restaurant Service but only include fields we need
 */
public class RestaurantDto {

    /**
     * Restaurant response DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantResponse {
        private Long id;
        private String name;
        private String address;
        private boolean isOpen;
        private BigDecimal deliveryFee;
        private BigDecimal minimumOrder;
    }

    /**
     * Menu item response DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class
    MenuItemResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private boolean available;
    }

    /**
     * API Response wrapper for Restaurant Service calls
     */
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
    }
}