package olim.com.restaurantservice.repository;

import olim.com.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant entity
 * Spring Data JPA automatically implements these methods
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     * Find restaurants by owner ID
     * Used when restaurant owners want to see their restaurants
     */
    List<Restaurant> findByOwnerId(Long ownerId);

    /**
     * Find restaurants by cuisine type
     */
    List<Restaurant> findByCuisineType(Restaurant.CuisineType cuisineType);

    /**
     * Find restaurants that are currently open
     */
    List<Restaurant> findByIsOpenTrue();

    /**
     * Search restaurants by name (case-insensitive)
     */
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Restaurant> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Search restaurants by address (case-insensitive)
     */
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.address) LIKE LOWER(CONCAT('%', :address, '%'))")
    List<Restaurant> findByAddressContainingIgnoreCase(@Param("address") String address);

    /**
     * Find restaurants by cuisine type and open status
     */
    List<Restaurant> findByCuisineTypeAndIsOpenTrue(Restaurant.CuisineType cuisineType);

    /**
     * Check if a restaurant belongs to a specific owner
     */
    boolean existsByIdAndOwnerId(Long restaurantId, Long ownerId);

    /**
     * Count restaurants by owner
     */
    long countByOwnerId(Long ownerId);
}