package olim.com.restaurantservice.config;

import olim.com.restaurantservice.entity.MenuItem;
import olim.com.restaurantservice.entity.Restaurant;
import olim.com.restaurantservice.repository.MenuItemRepository;
import olim.com.restaurantservice.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class RestaurantDataInitializer implements CommandLineRunner {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRestaurants();
    }

    private void initializeRestaurants() {
        try {
            // Check if data already exists
            if (restaurantRepository.count() > 0) {
                System.out.println("🍽️ Restaurant data already exists, skipping initialization");
                return;
            }

            // Create sample restaurants
            Restaurant italianPlace = createRestaurant(
                "Mama Mia's Italian",
                "Authentic Italian cuisine with fresh ingredients",
                "Kimisagara, Food District",
                "+2507884850",
                Restaurant.CuisineType.ITALIAN,
                1L, // This should match a restaurant owner user ID
                BigDecimal.valueOf(3.99),
                BigDecimal.valueOf(15.00)
            );

            Restaurant pizzaHouse = createRestaurant(
                "Tony's Pizza House",
                "Best pizza in town with wood-fired oven",
                "Kigali, Downtown",
                "+025788680834",
                Restaurant.CuisineType.PIZZA,
                1L,
                BigDecimal.valueOf(2.50),
                BigDecimal.valueOf(12.00)
            );

            Restaurant chineseGarden = createRestaurant(
                "Golden Dragon",
                "Traditional Chinese dishes and modern fusion",
                "789 Asia Street, Chinatown",
                "+1-555-0103",
                Restaurant.CuisineType.CHINESE,
                1L, // Same owner for demo
                BigDecimal.valueOf(4.50),
                BigDecimal.valueOf(20.00)
            );

            Restaurant fastBurger = createRestaurant(
                "Quick Bite Burgers",
                "Fast, fresh burgers and fries",
                "KG 311 Ave, Fast Food District",
                "+250785550104",
                Restaurant.CuisineType.FAST_FOOD,
                1L, // Same owner for demo
                BigDecimal.valueOf(1.99),
                BigDecimal.valueOf(8.00)
            );

            // Save restaurants
            List<Restaurant> restaurants = Arrays.asList(italianPlace, pizzaHouse, chineseGarden, fastBurger);
            restaurantRepository.saveAll(restaurants);

            // Create menu items for each restaurant
            createMenuItems(italianPlace);
            createMenuItems(pizzaHouse);
            createMenuItems(chineseGarden);
            createMenuItems(fastBurger);

            System.out.println("✅ Created " + restaurants.size() + " sample restaurants");
            System.out.println("✅ Created sample menu items for each restaurant");
            System.out.println("🎯 Restaurant service data initialization completed!");

        } catch (Exception e) {
            System.err.println("❌ Error during restaurant data initialization: " + e.getMessage());
        }
    }

    private Restaurant createRestaurant(String name, String description, String address,
                                      String phoneNumber, Restaurant.CuisineType cuisineType,
                                      Long ownerId, BigDecimal deliveryFee, BigDecimal minimumOrder) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setAddress(address);
        restaurant.setPhoneNumber(phoneNumber);
        restaurant.setCuisineType(cuisineType);
        restaurant.setOwnerId(ownerId);
        restaurant.setDeliveryFee(deliveryFee);
        restaurant.setMinimumOrder(minimumOrder);
        restaurant.setOpen(true);
        return restaurant;
    }

    private void createMenuItems(Restaurant restaurant) {
        List<MenuItem> menuItems;

        switch (restaurant.getCuisineType()) {
            case ITALIAN:
                menuItems = Arrays.asList(
                    createMenuItem("Spaghetti Carbonara", "Classic pasta with eggs, cheese, and pancetta", 
                                 BigDecimal.valueOf(18.99), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("Margherita Pizza", "Fresh tomatoes, mozzarella, and basil", 
                                 BigDecimal.valueOf(16.50), MenuItem.Category.MAIN_COURSE, true, restaurant),
                    createMenuItem("Caesar Salad", "Crispy romaine with parmesan and croutons", 
                                 BigDecimal.valueOf(12.99), MenuItem.Category.APPETIZER, true, restaurant),
                    createMenuItem("Tiramisu", "Classic Italian dessert with coffee and mascarpone", 
                                 BigDecimal.valueOf(8.99), MenuItem.Category.DESSERT, true, restaurant)
                );
                break;

            case PIZZA:
                menuItems = Arrays.asList(
                    createMenuItem("Pepperoni Pizza", "Classic pepperoni with mozzarella cheese", 
                                 BigDecimal.valueOf(15.99), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("Veggie Supreme", "Bell peppers, mushrooms, onions, olives", 
                                 BigDecimal.valueOf(17.99), MenuItem.Category.MAIN_COURSE, true, restaurant),
                    createMenuItem("Garlic Bread", "Fresh bread with garlic butter and herbs", 
                                 BigDecimal.valueOf(6.99), MenuItem.Category.SIDES, true, restaurant),
                    createMenuItem("Coke", "Refreshing cola drink", 
                                 BigDecimal.valueOf(2.99), MenuItem.Category.BEVERAGE, true, restaurant)
                );
                break;

            case CHINESE:
                menuItems = Arrays.asList(
                    createMenuItem("Sweet and Sour Pork", "Crispy pork with pineapple and bell peppers", 
                                 BigDecimal.valueOf(19.99), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("Vegetable Spring Rolls", "Fresh vegetables in crispy wrapper", 
                                 BigDecimal.valueOf(8.99), MenuItem.Category.APPETIZER, true, restaurant),
                    createMenuItem("Kung Pao Chicken", "Spicy chicken with peanuts and vegetables", 
                                 BigDecimal.valueOf(18.50), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("Green Tea", "Traditional Chinese green tea", 
                                 BigDecimal.valueOf(3.50), MenuItem.Category.BEVERAGE, true, restaurant)
                );
                break;

            case FAST_FOOD:
                menuItems = Arrays.asList(
                    createMenuItem("Classic Burger", "Beef patty with lettuce, tomato, and special sauce", 
                                 BigDecimal.valueOf(9.99), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("Chicken Nuggets", "Crispy chicken pieces with dipping sauce", 
                                 BigDecimal.valueOf(7.99), MenuItem.Category.MAIN_COURSE, false, restaurant),
                    createMenuItem("French Fries", "Golden crispy potato fries", 
                                 BigDecimal.valueOf(4.99), MenuItem.Category.SIDES, true, restaurant),
                    createMenuItem("Milkshake", "Creamy vanilla milkshake", 
                                 BigDecimal.valueOf(5.99), MenuItem.Category.BEVERAGE, true, restaurant)
                );
                break;

            default:
                menuItems = Arrays.asList();
        }

        menuItemRepository.saveAll(menuItems);
    }

    private MenuItem createMenuItem(String name, String description, BigDecimal price,
                                  MenuItem.Category category, boolean vegetarian, Restaurant restaurant) {
        MenuItem menuItem = new MenuItem();
        menuItem.setName(name);
        menuItem.setDescription(description);
        menuItem.setPrice(price);
        menuItem.setCategory(category);
        menuItem.setVegetarian(vegetarian);
        menuItem.setAvailable(true);
        menuItem.setRestaurant(restaurant);
        return menuItem;
    }
}