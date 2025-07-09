package olim.com.authservice.config;

import olim.com.authservice.entity.User;
import olim.com.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Initializer
 * 
 * Creates initial data for development and testing
 * Runs after the application starts up
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    /**
     * Initialize sample users for development
     */
    private void initializeUsers() {
        try {
            // Create admin user if not exists
            if (!userService.emailExists("admin@bytebites.com")) {
                userService.registerUser(
                    "admin@bytebites.com",
                    "Admin",
                    "User",
                    "admin123",
                    User.Role.ROLE_ADMIN
                );
                System.out.println("✅ Created admin user: admin@bytebites.com / admin123");
            }

            // Create sample customer
            if (!userService.emailExists("customer@example.com")) {
                userService.registerUser(
                    "customer@example.com",
                    "John",
                    "Doe",
                    "customer123",
                    User.Role.ROLE_CUSTOMER
                );
                System.out.println("✅ Created customer user: customer@example.com / customer123");
            }

            // Create sample restaurant owner
            if (!userService.emailExists("owner@restaurant.com")) {
                userService.registerUser(
                    "owner@restaurant.com",
                    "Jane",
                    "Smith",
                    "owner123",
                    User.Role.ROLE_RESTAURANT_OWNER
                );
                System.out.println("✅ Created restaurant owner: owner@restaurant.com / owner123");
            }

            System.out.println("🎯 Data initialization completed!");

        } catch (Exception e) {
            System.err.println("❌ Error during data initialization: " + e.getMessage());
        }
    }
}