package olim.com.restaurantservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestaurantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantServiceApplication.class, args);
		System.out.println("🚀 Restaurant Service is running!");
		System.out.println("🍽️ Restaurant management endpoints available at: http://localhost:8081");
		System.out.println("📊 H2 Console (dev only): http://localhost:8081/h2-console");
	}

}
