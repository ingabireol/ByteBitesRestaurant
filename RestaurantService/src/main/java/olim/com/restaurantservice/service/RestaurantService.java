package olim.com.restaurantservice.service;

import olim.com.restaurantservice.entity.Restaurant;
import olim.com.restaurantservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public List<Restaurant> getOpenRestaurants() {
        return restaurantRepository.findByIsOpenTrue();
    }

    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public List<Restaurant> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    public List<Restaurant> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Restaurant> searchRestaurantsByAddress(String address) {
        return restaurantRepository.findByAddressContainingIgnoreCase(address);
    }

    public List<Restaurant> getRestaurantsByCuisine(Restaurant.CuisineType cuisineType) {
        return restaurantRepository.findByCuisineTypeAndIsOpenTrue(cuisineType);
    }

    public Restaurant createRestaurant(Restaurant restaurant, Long ownerId) {
        // Set the owner ID
        restaurant.setOwnerId(ownerId);
        
        // Save the restaurant
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(Long restaurantId, Restaurant updatedRestaurant, Long ownerId) {
        // Check if restaurant exists and belongs to the owner
        Restaurant existingRestaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!existingRestaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only update your own restaurant");
        }

        // Update the restaurant details
        existingRestaurant.setName(updatedRestaurant.getName());
        existingRestaurant.setDescription(updatedRestaurant.getDescription());
        existingRestaurant.setAddress(updatedRestaurant.getAddress());
        existingRestaurant.setPhoneNumber(updatedRestaurant.getPhoneNumber());
        existingRestaurant.setCuisineType(updatedRestaurant.getCuisineType());
        existingRestaurant.setDeliveryFee(updatedRestaurant.getDeliveryFee());
        existingRestaurant.setMinimumOrder(updatedRestaurant.getMinimumOrder());

        return restaurantRepository.save(existingRestaurant);
    }

    public Restaurant toggleRestaurantStatus(Long restaurantId, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only update your own restaurant");
        }

        restaurant.setOpen(!restaurant.isOpen());
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(Long restaurantId, Long ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only delete your own restaurant");
        }

        restaurantRepository.delete(restaurant);
    }

    public boolean isRestaurantOwner(Long restaurantId, Long ownerId) {
        return restaurantRepository.existsByIdAndOwnerId(restaurantId, ownerId);
    }
    public RestaurantStats getOwnerStats(Long ownerId) {
        long totalRestaurants = restaurantRepository.countByOwnerId(ownerId);
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(ownerId);
        long openRestaurants = restaurants.stream()
                .mapToLong(r -> r.isOpen() ? 1 : 0)
                .sum();

        return new RestaurantStats(totalRestaurants, openRestaurants);
    }

    /**
     * Restaurant statistics data class
     */
    public static class RestaurantStats {
        private final long totalRestaurants;
        private final long openRestaurants;

        public RestaurantStats(long totalRestaurants, long openRestaurants) {
            this.totalRestaurants = totalRestaurants;
            this.openRestaurants = openRestaurants;
        }

        public long getTotalRestaurants() { return totalRestaurants; }
        public long getOpenRestaurants() { return openRestaurants; }
        public long getClosedRestaurants() { return totalRestaurants - openRestaurants; }
    }
}