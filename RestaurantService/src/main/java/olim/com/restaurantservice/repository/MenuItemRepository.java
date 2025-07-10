package olim.com.restaurantservice.repository;

import olim.com.restaurantservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MenuItem entity
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Find all menu items for a specific restaurant
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     * Find available menu items for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    /**
     * Find menu items by category for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndCategory(Long restaurantId, MenuItem.Category category);

    /**
     * Find vegetarian items for a restaurant
     */
    List<MenuItem> findByRestaurantIdAndVegetarianTrue(Long restaurantId);

    /**
     * Search menu items by name in a restaurant
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MenuItem> findByRestaurantIdAndNameContainingIgnoreCase(@Param("restaurantId") Long restaurantId, @Param("name") String name);

    /**
     * Check if a menu item belongs to a specific restaurant owner
     */
    @Query("SELECT COUNT(m) > 0 FROM MenuItem m WHERE m.id = :menuItemId AND m.restaurant.ownerId = :ownerId")
    boolean existsByIdAndRestaurantOwnerId(@Param("menuItemId") Long menuItemId, @Param("ownerId") Long ownerId);

    /**
     * Count menu items by restaurant
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Delete all menu items for a restaurant
     */
    void deleteByRestaurantId(Long restaurantId);
}