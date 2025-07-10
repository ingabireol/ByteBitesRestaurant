package olim.com.restaurantservice.service;

import olim.com.restaurantservice.entity.MenuItem;
import olim.com.restaurantservice.entity.Restaurant;
import olim.com.restaurantservice.repository.MenuItemRepository;
import olim.com.restaurantservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MenuItem Service - Business logic for menu item operations
 */
@Service
@Transactional
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<MenuItem> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public List<MenuItem> getAvailableMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    public List<MenuItem> getMenuItemsByCategory(Long restaurantId, MenuItem.Category category) {
        return menuItemRepository.findByRestaurantIdAndCategory(restaurantId, category);
    }

    public List<MenuItem> getVegetarianMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndVegetarianTrue(restaurantId);
    }

    public List<MenuItem> searchMenuItems(Long restaurantId, String name) {
        return menuItemRepository.findByRestaurantIdAndNameContainingIgnoreCase(restaurantId, name);
    }

    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }

    public MenuItem createMenuItem(Long restaurantId, MenuItem menuItem, Long ownerId) {
        // Check if restaurant exists and belongs to the owner
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only add items to your own restaurant");
        }

        // Set the restaurant relationship
        menuItem.setRestaurant(restaurant);
        
        // Save the menu item
        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(Long menuItemId, MenuItem updatedMenuItem, Long ownerId) {
        // Check if menu item exists and belongs to the owner's restaurant
        MenuItem existingMenuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!existingMenuItem.getRestaurant().getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only update items in your own restaurant");
        }

        // Update the menu item details
        existingMenuItem.setName(updatedMenuItem.getName());
        existingMenuItem.setDescription(updatedMenuItem.getDescription());
        existingMenuItem.setPrice(updatedMenuItem.getPrice());
        existingMenuItem.setCategory(updatedMenuItem.getCategory());
        existingMenuItem.setVegetarian(updatedMenuItem.isVegetarian());

        return menuItemRepository.save(existingMenuItem);
    }

    public MenuItem toggleMenuItemAvailability(Long menuItemId, Long ownerId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!menuItem.getRestaurant().getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only update items in your own restaurant");
        }

        menuItem.setAvailable(!menuItem.isAvailable());
        return menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(Long menuItemId, Long ownerId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        if (!menuItem.getRestaurant().getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only delete items from your own restaurant");
        }

        menuItemRepository.delete(menuItem);
    }

    public boolean isMenuItemOwner(Long menuItemId, Long ownerId) {
        return menuItemRepository.existsByIdAndRestaurantOwnerId(menuItemId, ownerId);
    }

    public MenuStats getMenuStats(Long restaurantId, Long ownerId) {
        // Verify ownership
        if (!restaurantRepository.existsByIdAndOwnerId(restaurantId, ownerId)) {
            throw new RuntimeException("You can only view stats for your own restaurant");
        }

        long totalItems = menuItemRepository.countByRestaurantId(restaurantId);
        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);
        
        long availableItems = items.stream()
                .mapToLong(item -> item.isAvailable() ? 1 : 0)
                .sum();
        
        long vegetarianItems = items.stream()
                .mapToLong(item -> item.isVegetarian() ? 1 : 0)
                .sum();

        return new MenuStats(totalItems, availableItems, vegetarianItems);
    }

    public static class MenuStats {
        private final long totalItems;
        private final long availableItems;
        private final long vegetarianItems;

        public MenuStats(long totalItems, long availableItems, long vegetarianItems) {
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.vegetarianItems = vegetarianItems;
        }

        public long getTotalItems() { return totalItems; }
        public long getAvailableItems() { return availableItems; }
        public long getUnavailableItems() { return totalItems - availableItems; }
        public long getVegetarianItems() { return vegetarianItems; }
    }
}