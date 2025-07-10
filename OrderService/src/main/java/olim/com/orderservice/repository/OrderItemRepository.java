package olim.com.orderservice.repository;

import olim.com.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all order items for a specific order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find order items by menu item ID
     * Useful for analytics - which menu items are popular
     */
    List<OrderItem> findByMenuItemId(Long menuItemId);

    /**
     * Count total quantity ordered for a specific menu item
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.menuItemId = :menuItemId")
    Long getTotalQuantityOrderedForMenuItem(@Param("menuItemId") Long menuItemId);

    /**
     * Delete all order items for an order
     */
    void deleteByOrderId(Long orderId);
}