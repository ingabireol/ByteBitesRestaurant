package olim.com.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("🚀 Config Server is running!");
        System.out.println("📍 Config Server URL: http://localhost:8888");
        System.out.println("📂 Configuration endpoint: http://localhost:8888/{application}/{profile}");
    }

}
