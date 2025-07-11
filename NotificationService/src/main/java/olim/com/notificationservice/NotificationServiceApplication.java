package olim.com.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
        System.out.println("🚀 Notification Service is running!");
        System.out.println("📨 Listening for order events on: http://localhost:8083");
        System.out.println("📊 H2 Console (dev only): http://localhost:8083/h2-console");
        System.out.println("📬 Ready to send email and SMS notifications!");
    }

}
