package olim.com.orderservice.client;

import olim.com.orderservice.dto.RestaurantDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback implementation for Restaurant Service
 *
 * Provides graceful degradation when Restaurant Service is unavailable
 * Returns default responses instead of failing completely
 */
@Component
public class RestaurantFallback implements RestaurantClient {

    @Override
    public RestaurantDto.ApiResponse<RestaurantDto.RestaurantResponse> getRestaurant(Long restaurantId) {
        // Return a fallback restaurant response wrapped in API response
        RestaurantDto.RestaurantResponse fallbackRestaurant = new RestaurantDto.RestaurantResponse();
        fallbackRestaurant.setId(restaurantId);
        fallbackRestaurant.setName("Restaurant Temporarily Unavailable");
        fallbackRestaurant.setAddress("Unknown Address");
        fallbackRestaurant.setOpen(false); // Mark as closed during fallback
        fallbackRestaurant.setDeliveryFee(BigDecimal.valueOf(5.00));
        fallbackRestaurant.setMinimumOrder(BigDecimal.valueOf(15.00));

        return new RestaurantDto.ApiResponse<>(false, "Restaurant service unavailable", fallbackRestaurant);
    }

    @Override
    public RestaurantDto.ApiResponse<RestaurantDto.MenuItemResponse> getMenuItem(Long restaurantId, Long menuItemId) {
        // Return a fallback menu item response wrapped in API response
        RestaurantDto.MenuItemResponse fallbackMenuItem = new RestaurantDto.MenuItemResponse();
        fallbackMenuItem.setId(menuItemId);
        fallbackMenuItem.setName("Item Temporarily Unavailable");
        fallbackMenuItem.setPrice(BigDecimal.valueOf(10.00));
        fallbackMenuItem.setAvailable(false); // Mark as unavailable during fallback

        return new RestaurantDto.ApiResponse<>(false, "Restaurant service unavailable", fallbackMenuItem);
    }
}