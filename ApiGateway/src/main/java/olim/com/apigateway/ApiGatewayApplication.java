package olim.com.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
		System.out.println("🚀 API Gateway is running!");
		System.out.println("🌐 Gateway URL: http://localhost:8765");
		System.out.println("📍 All requests should go through this gateway");
	}

}
