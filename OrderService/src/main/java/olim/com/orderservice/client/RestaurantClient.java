package olim.com.orderservice.client;

import olim.com.orderservice.dto.RestaurantDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for Restaurant Service
 *
 * Enables service-to-service communication with the Restaurant Service
 * Used to validate restaurants and menu items when creating orders
 */
@FeignClient(
        name = "restaurant-service",
        fallback = RestaurantFallback.class
)
public interface RestaurantClient {

    /**
     * Get restaurant details by ID
     * Returns the API response wrapper from Restaurant Service
     */
    @GetMapping("/api/restaurants/{id}")
    RestaurantDto.ApiResponse<RestaurantDto.RestaurantResponse> getRestaurant(@PathVariable("id") Long restaurantId);

    /**
     * Get menu item details by restaurant and menu item ID
     * Returns the API response wrapper from Restaurant Service
     */
    @GetMapping("/api/restaurants/{restaurantId}/menu/{itemId}")
    RestaurantDto.ApiResponse<RestaurantDto.MenuItemResponse> getMenuItem(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("itemId") Long menuItemId
    );
}