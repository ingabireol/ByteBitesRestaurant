package olim.com.restaurantservice.controller;

import jakarta.validation.Valid;
import olim.com.restaurantservice.dto.MenuItemDto.*;
import olim.com.restaurantservice.dto.RestaurantDto.ApiResponse;
import olim.com.restaurantservice.entity.MenuItem;
import olim.com.restaurantservice.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for MenuItem operations
 * 
 * Handles HTTP requests for menu item management
 * Follows the same security patterns as RestaurantController
 */
@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    /**
     * Get all menu items for a restaurant (public endpoint)
     */
    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<ApiResponse<List<SimpleMenuItemResponse>>> getRestaurantMenu(
            @PathVariable Long restaurantId) {
        try {
            List<MenuItem> menuItems = menuItemService.getAvailableMenuItems(restaurantId);
            List<SimpleMenuItemResponse> response = menuItems.stream()
                    .map(SimpleMenuItemResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Menu retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve menu: " + e.getMessage()));
        }
    }

    /**
     * Get menu items by category (public endpoint)
     */
    @GetMapping("/{restaurantId}/menu/category/{category}")
    public ResponseEntity<ApiResponse<List<SimpleMenuItemResponse>>> getMenuByCategory(
            @PathVariable Long restaurantId,
            @PathVariable MenuItem.Category category) {
        try {
            List<MenuItem> menuItems = menuItemService.getMenuItemsByCategory(restaurantId, category);
            List<SimpleMenuItemResponse> response = menuItems.stream()
                    .map(SimpleMenuItemResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Menu items retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve menu items: " + e.getMessage()));
        }
    }

    /**
     * Get vegetarian menu items (public endpoint)
     */
    @GetMapping("/{restaurantId}/menu/vegetarian")
    public ResponseEntity<ApiResponse<List<SimpleMenuItemResponse>>> getVegetarianMenu(
            @PathVariable Long restaurantId) {
        try {
            List<MenuItem> menuItems = menuItemService.getVegetarianMenuItems(restaurantId);
            List<SimpleMenuItemResponse> response = menuItems.stream()
                    .map(SimpleMenuItemResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Vegetarian items retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve vegetarian items: " + e.getMessage()));
        }
    }

    /**
     * Search menu items by name (public endpoint)
     */
    @GetMapping("/{restaurantId}/menu/search")
    public ResponseEntity<ApiResponse<List<SimpleMenuItemResponse>>> searchMenuItems(
            @PathVariable Long restaurantId,
            @RequestParam String query) {
        try {
            List<MenuItem> menuItems = menuItemService.searchMenuItems(restaurantId, query);
            List<SimpleMenuItemResponse> response = menuItems.stream()
                    .map(SimpleMenuItemResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Search completed successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    /**
     * Get owner's menu items for a restaurant (requires RESTAURANT_OWNER role)
     */
    @GetMapping("/{restaurantId}/menu/manage")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<List<OwnerMenuItemResponse>>> getOwnerMenu(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            
            // Verify ownership through service layer
            List<MenuItem> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);
            
            // Check if user owns the restaurant (will throw exception if not)
            if (!menuItems.isEmpty() && !menuItems.get(0).getRestaurant().getOwnerId().equals(ownerId)) {
                throw new RuntimeException("You can only manage your own restaurant's menu");
            }
            
            List<OwnerMenuItemResponse> response = menuItems.stream()
                    .map(OwnerMenuItemResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("Menu retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve menu: " + e.getMessage()));
        }
    }

    /**
     * Create a new menu item (requires RESTAURANT_OWNER role)
     */
    @PostMapping("/{restaurantId}/menu")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerMenuItemResponse>> createMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateMenuItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);

            // Create menu item entity from DTO
            MenuItem menuItem = new MenuItem();
            menuItem.setName(request.getName());
            menuItem.setDescription(request.getDescription());
            menuItem.setPrice(request.getPrice());
            menuItem.setCategory(request.getCategory());
            menuItem.setVegetarian(request.isVegetarian());

            MenuItem savedMenuItem = menuItemService.createMenuItem(restaurantId, menuItem, ownerId);
            OwnerMenuItemResponse response = new OwnerMenuItemResponse(savedMenuItem);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Menu item created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create menu item: " + e.getMessage()));
        }
    }

    /**
     * Update a menu item (requires RESTAURANT_OWNER role and ownership)
     */
    @PutMapping("/{restaurantId}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerMenuItemResponse>> updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateMenuItemRequest request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);

            // Create updated menu item entity from DTO
            MenuItem updatedMenuItem = new MenuItem();
            updatedMenuItem.setName(request.getName());
            updatedMenuItem.setDescription(request.getDescription());
            updatedMenuItem.setPrice(request.getPrice());
            updatedMenuItem.setCategory(request.getCategory());
            updatedMenuItem.setVegetarian(request.isVegetarian());

            MenuItem savedMenuItem = menuItemService.updateMenuItem(itemId, updatedMenuItem, ownerId);
            OwnerMenuItemResponse response = new OwnerMenuItemResponse(savedMenuItem);

            return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update menu item: " + e.getMessage()));
        }
    }

    /**
     * Toggle menu item availability (requires RESTAURANT_OWNER role)
     */
    @PatchMapping("/{restaurantId}/menu/{itemId}/toggle-availability")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<OwnerMenuItemResponse>> toggleMenuItemAvailability(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            MenuItem menuItem = menuItemService.toggleMenuItemAvailability(itemId, ownerId);
            OwnerMenuItemResponse response = new OwnerMenuItemResponse(menuItem);

            String status = menuItem.isAvailable() ? "available" : "unavailable";
            return ResponseEntity.ok(ApiResponse.success("Menu item marked as " + status, response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update menu item availability: " + e.getMessage()));
        }
    }

    /**
     * Delete a menu item (requires RESTAURANT_OWNER role and ownership)
     */
    @DeleteMapping("/{restaurantId}/menu/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<String>> deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            menuItemService.deleteMenuItem(itemId, ownerId);

            return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete menu item: " + e.getMessage()));
        }
    }

    /**
     * Get menu statistics for restaurant owner
     */
    @GetMapping("/{restaurantId}/menu/stats")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<ApiResponse<MenuItemService.MenuStats>> getMenuStats(
            @PathVariable Long restaurantId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            Long ownerId = Long.parseLong(userId);
            MenuItemService.MenuStats stats = menuItemService.getMenuStats(restaurantId, ownerId);

            return ResponseEntity.ok(ApiResponse.success("Menu statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve menu statistics: " + e.getMessage()));
        }
    }

    /**
     * Get a specific menu item (public endpoint)
     */
    @GetMapping("/{restaurantId}/menu/{itemId}")
    public ResponseEntity<ApiResponse<SimpleMenuItemResponse>> getMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long itemId) {
        try {
            MenuItem menuItem = menuItemService.getMenuItemById(itemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            // Verify the item belongs to the specified restaurant
            if (!menuItem.getRestaurant().getId().equals(restaurantId)) {
                throw new RuntimeException("Menu item does not belong to this restaurant");
            }

            SimpleMenuItemResponse response = new SimpleMenuItemResponse(menuItem);
            return ResponseEntity.ok(ApiResponse.success("Menu item retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve menu item: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint for menu item controller
     */
    @GetMapping("/menu/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Menu item controller is running"));
    }
}