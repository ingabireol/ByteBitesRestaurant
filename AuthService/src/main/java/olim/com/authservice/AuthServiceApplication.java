package olim.com.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
		System.out.println("🚀 Auth Service is running!");
		System.out.println("🔐 Authentication endpoints available at: http://localhost:8080");
		System.out.println("📊 H2 Console (dev only): http://localhost:8080/h2-console");

	}

}
