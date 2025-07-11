package olim.com.notificationservice.service;

import olim.com.notificationservice.entity.Notification;
import olim.com.notificationservice.event.OrderPlacedEvent;
import olim.com.notificationservice.event.OrderStatusChangedEvent;
import olim.com.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Email Notification Service
 * 
 * Handles sending email notifications for order events
 * In a real application, this would integrate with email providers like SendGrid, AWS SES, etc.
 * For now, we'll simulate email sending with console output
 */
@Service
public class EmailNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.email.from:noreply@bytebites.com}")
    private String fromEmail;

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(OrderPlacedEvent event) {
        if (!emailEnabled) {
            System.out.println("📧 Email notifications disabled - skipping");
            return;
        }

        try {
            // Create notification record
            String subject = "Order Confirmation - ByteBites Order #" + event.getOrderId();
            String message = buildOrderConfirmationMessage(event);
            
            Notification notification = new Notification(
                event.getOrderId(),
                event.getCustomerId(),
                event.getCustomerEmail(),
                Notification.NotificationType.ORDER_CONFIRMATION,
                Notification.NotificationChannel.EMAIL,
                subject,
                message,
                event.getCustomerEmail()
            );

            // Save notification record
            notification = notificationRepository.save(notification);

            // Simulate email sending (in real app, use email service)
            boolean emailSent = simulateEmailSending(event.getCustomerEmail(), subject, message);

            if (emailSent) {
                notification.markAsSent();
                System.out.println("✅ Order confirmation email sent to: " + event.getCustomerEmail());
            } else {
                notification.markAsFailed("Simulated email failure");
                System.err.println("❌ Failed to send order confirmation email");
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("❌ Error sending order confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdateEmail(OrderStatusChangedEvent event) {
        if (!emailEnabled) {
            System.out.println("📧 Email notifications disabled - skipping");
            return;
        }

        try {
            // Create notification record
            String subject = "Order Status Update - ByteBites Order #" + event.getOrderId();
            String message = buildStatusUpdateMessage(event);
            
            Notification notification = new Notification(
                event.getOrderId(),
                event.getCustomerId(),
                event.getCustomerEmail(),
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                Notification.NotificationChannel.EMAIL,
                subject,
                message,
                event.getCustomerEmail()
            );

            // Save notification record
            notification = notificationRepository.save(notification);

            // Simulate email sending
            boolean emailSent = simulateEmailSending(event.getCustomerEmail(), subject, message);

            if (emailSent) {
                notification.markAsSent();
                System.out.println("✅ Status update email sent to: " + event.getCustomerEmail() + 
                                 " (" + event.getOldStatus() + " → " + event.getNewStatus() + ")");
            } else {
                notification.markAsFailed("Simulated email failure");
                System.err.println("❌ Failed to send status update email");
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("❌ Error sending status update email: " + e.getMessage());
        }
    }

    /**
     * Build order confirmation email message
     */
    private String buildOrderConfirmationMessage(OrderPlacedEvent event) {
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(event.getCustomerName()).append(",\n\n");
        message.append("Thank you for your order with ByteBites!\n\n");
        message.append("Order Details:\n");
        message.append("Order ID: #").append(event.getOrderId()).append("\n");
        message.append("Restaurant: ").append(event.getRestaurantName()).append("\n");
        message.append("Delivery Address: ").append(event.getDeliveryAddress()).append("\n");
        message.append("Order Time: ").append(event.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        message.append("Items Ordered:\n");
        for (OrderPlacedEvent.OrderItemInfo item : event.getItems()) {
            message.append("- ").append(item.getItemName())
                   .append(" (Qty: ").append(item.getQuantity()).append(")")
                   .append(" - $").append(item.getPrice()).append("\n");
        }
        
        message.append("\nTotal Amount: $").append(event.getTotalAmount()).append("\n\n");
        message.append("We'll notify you when your order status changes.\n\n");
        message.append("Thank you for choosing ByteBites!\n");
        message.append("The ByteBites Team");
        
        return message.toString();
    }

    /**
     * Build status update email message
     */
    private String buildStatusUpdateMessage(OrderStatusChangedEvent event) {
        StringBuilder message = new StringBuilder();
        message.append("Dear Customer,\n\n");
        message.append("Your ByteBites order status has been updated!\n\n");
        message.append("Order Details:\n");
        message.append("Order ID: #").append(event.getOrderId()).append("\n");
        message.append("Restaurant: ").append(event.getRestaurantName()).append("\n");
        message.append("Status: ").append(event.getOldStatus()).append(" → ").append(event.getNewStatus()).append("\n");
        message.append("Updated: ").append(event.getChangedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");
        
        // Add status-specific messages
        switch (event.getNewStatus()) {
            case "CONFIRMED":
                message.append("Great news! Your order has been confirmed by the restaurant and they're starting to prepare your food.\n");
                break;
            case "PREPARING":
                message.append("Your order is now being prepared. It won't be long now!\n");
                break;
            case "DELIVERED":
                message.append("Your order has been delivered! We hope you enjoy your meal. Please rate your experience!\n");
                break;
            case "CANCELLED":
                message.append("Your order has been cancelled. If you didn't request this, please contact support.\n");
                break;
            default:
                message.append("Your order status has been updated.\n");
        }
        
        message.append("\nThank you for choosing ByteBites!\n");
        message.append("The ByteBites Team");
        
        return message.toString();
    }

    /**
     * Simulate email sending (replace with real email service in production)
     */
    private boolean simulateEmailSending(String to, String subject, String message) {
        try {
            // Simulate processing time
            Thread.sleep(100);
            
            // Simulate 95% success rate
            boolean success = Math.random() > 0.05;
            
            if (success) {
                System.out.println("\n📧 ================== EMAIL SENT ==================");
                System.out.println("To: " + to);
                System.out.println("From: " + fromEmail);
                System.out.println("Subject: " + subject);
                System.out.println("Message:");
                System.out.println(message);
                System.out.println("================================================\n");
            }
            
            return success;
            
        } catch (Exception e) {
            return false;
        }
    }
}