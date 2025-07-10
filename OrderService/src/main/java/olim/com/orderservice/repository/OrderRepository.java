package olim.com.orderservice.repository;

import olim.com.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by customer ID
     * Used when customers want to see their order history
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find orders by restaurant ID
     * Used when restaurant owners want to see orders for their restaurant
     */
    List<Order> findByRestaurantId(Long restaurantId);

    /**
     * Find orders by customer ID and status
     */
    List<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status);

    /**
     * Find orders by restaurant ID and status
     */
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status);

    /**
     * Find active orders for a customer (not delivered or cancelled)
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Order> findActiveOrdersByCustomer(@Param("customerId") Long customerId);

    /**
     * Find pending orders for a restaurant
     */
    List<Order> findByRestaurantIdAndStatusOrderByCreatedAtAsc(Long restaurantId, Order.OrderStatus status);

    /**
     * Check if an order belongs to a specific customer
     */
    boolean existsByIdAndCustomerId(Long orderId, Long customerId);

    /**
     * Count orders by customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Count orders by restaurant
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Find recent orders by customer (for order history)
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByCustomer(@Param("customerId") Long customerId);
}