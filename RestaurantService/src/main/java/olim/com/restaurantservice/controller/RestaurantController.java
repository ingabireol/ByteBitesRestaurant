package olim.com.restaurantservice.controller;

import jakarta.validation.Valid;
import olim.com.restaurantservice.dto.RestaurantDto.*;
import olim.com.restaurantservice.dto.MenuItemDto;
import olim.com.restaurantservice.entity.Restaurant;
import olim.com.restaurantservice.entity.MenuItem;
import olim.com.restaurantservice.service.RestaurantService;
import olim.com.restaurantservice.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Restaurant operations
 * 
 * Handles HTTP requests for restaurant management
 * Uses method-level security for authorization
 */
@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Get all restaurants (public endpoint)
     * Anyone can view restaurants
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        try {
            List<Restaurant> restaurants = restaurantService.getOpenRestaurants();
            List<RestaurantResponse> response = restaurants.stream()
                    .map(RestaurantResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve restaurants: " + e.getMessage()));
        }
    }

    /**
     * Get restaurant by ID with menu items (public endpoint)
     */
    /**
     * Get restaurant by ID with menu items (public endpoint)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDetailResponse>> getRestaurantById(@PathVariable Long id) {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(id)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            List<MenuItem> menuItems = menuItemService.getAvailableMenuItems(id);

            List<MenuItemDto.MenuItemResponse> menuItemResponses = menuItems.stream()
                    .map(MenuItemDto.MenuItemResponse::new)
                    .collect(Collectors.toList());

            RestaurantDetailResponse response = new RestaurantDetailResponse(restaurant, menuItemResponses);

            return ResponseEntity.ok(ApiResponse.success("Restaurant retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve restaurant: " + e.getMessage()));
        }
    }

    /**
     * Search restaurants by name (public endpoint)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchRestaurants(@RequestParam String query) {
        try {
            List<Restaurant> restaurants = restaurantService.searchRestaurantsByName(query);
            List<RestaurantResponse> response = restaurants.stream()
                    .map(RestaurantResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Get restaurants by cuisine type (public endpoint)
     */
    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getRestaurantsByCuisine(
            @PathVariable Restaurant.CuisineType cuisineType) {
        try {
            List<Restaurant> restaurants = restaurantService.getRestaurantsByCuisine(cuisineType);
            List<RestaurantResponse> response = restaurants.stream()
                    .map(RestaurantResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Restaurants retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve restaurants: " + e.getMessage()));
        }
    }

    /**
     * Create a new restaurant (requires RESTAURANT_OWNER role)
     */
    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerRestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);

            // Create restaurant entity from DTO
            Restaurant restaurant = new Restaurant(
                    request.getName(),
                    request.getDescription(),
                    request.getAddress(),
                    request.getPhoneNumber(),
                    request.getCuisineType(),
                    ownerId
            );
            restaurant.setDeliveryFee(request.getDeliveryFee());
            restaurant.setMinimumOrder(request.getMinimumOrder());

            Restaurant savedRestaurant = restaurantService.createRestaurant(restaurant, ownerId);
            OwnerRestaurantResponse response = new OwnerRestaurantResponse(savedRestaurant);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Restaurant created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create restaurant: " + e.getMessage()));
        }
    }

    /**
     * Get owner's restaurants (requires RESTAURANT_OWNER role)
     */
    @GetMapping("/my-restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<List<OwnerRestaurantResponse>>> getMyRestaurants(
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
            List<OwnerRestaurantResponse> response = restaurants.stream()
                    .map(OwnerRestaurantResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Your restaurants retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve your restaurants: " + e.getMessage()));
        }
    }

    /**
     * Update restaurant (requires RESTAURANT_OWNER role and ownership)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerRestaurantResponse>> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);

            // Create updated restaurant entity from DTO
            Restaurant updatedRestaurant = new Restaurant();
            updatedRestaurant.setName(request.getName());
            updatedRestaurant.setDescription(request.getDescription());
            updatedRestaurant.setAddress(request.getAddress());
            updatedRestaurant.setPhoneNumber(request.getPhoneNumber());
            updatedRestaurant.setCuisineType(request.getCuisineType());
            updatedRestaurant.setDeliveryFee(request.getDeliveryFee());
            updatedRestaurant.setMinimumOrder(request.getMinimumOrder());

            Restaurant savedRestaurant = restaurantService.updateRestaurant(id, updatedRestaurant, ownerId);
            OwnerRestaurantResponse response = new OwnerRestaurantResponse(savedRestaurant);

            return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update restaurant: " + e.getMessage()));
        }
    }

    /**
     * Toggle restaurant status (open/closed)
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerRestaurantResponse>> toggleRestaurantStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            Restaurant restaurant = restaurantService.toggleRestaurantStatus(id, ownerId);
            OwnerRestaurantResponse response = new OwnerRestaurantResponse(restaurant);

            return ResponseEntity.ok(ApiResponse.success("Restaurant status updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update restaurant status: " + e.getMessage()));
        }
    }

    /**
     * Delete restaurant (requires RESTAURANT_OWNER role and ownership)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<String>> deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            restaurantService.deleteRestaurant(id, ownerId);

            return ResponseEntity.ok(ApiResponse.success("Restaurant deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete restaurant: " + e.getMessage()));
        }
    }

    /**
     * Get restaurant statistics for owner
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<RestaurantService.RestaurantStats>> getRestaurantStats(
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            RestaurantService.RestaurantStats stats = restaurantService.getOwnerStats(ownerId);

            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Restaurant service is running"));
    }
}