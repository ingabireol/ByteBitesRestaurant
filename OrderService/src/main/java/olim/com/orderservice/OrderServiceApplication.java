package olim.com.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
		System.out.println("🚀 Order Service is running!");
		System.out.println("🛒 Order management endpoints available at: http://localhost:8082");
		System.out.println("📊 H2 Console (dev only): http://localhost:8082/h2-console");
		System.out.println("⚡ Circuit Breaker metrics: http://localhost:8082/actuator/circuitbreakers");
	}

}
