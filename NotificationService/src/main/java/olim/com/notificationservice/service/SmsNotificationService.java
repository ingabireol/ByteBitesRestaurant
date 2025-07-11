package olim.com.notificationservice.service;

import olim.com.notificationservice.entity.Notification;
import olim.com.notificationservice.event.OrderPlacedEvent;
import olim.com.notificationservice.event.OrderStatusChangedEvent;
import olim.com.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS Notification Service
 * 
 * Handles sending SMS notifications for order events
 * In a real application, this would integrate with SMS providers like Twilio, AWS SNS, etc.
 * For now, we'll simulate SMS sending with console output
 */
@Service
public class SmsNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${notification.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${notification.sms.provider:console}")
    private String smsProvider;

    /**
     * Send order confirmation SMS
     */
    public void sendOrderConfirmationSms(OrderPlacedEvent event) {
        if (!smsEnabled) {
            System.out.println("📱 SMS notifications disabled - skipping");
            return;
        }

        try {
            // For demo, we'll use a fake phone number
            String phoneNumber = "+1-555-" + String.format("%04d", event.getCustomerId().intValue() % 10000);
            
            String message = buildOrderConfirmationSms(event);
            
            Notification notification = new Notification(
                event.getOrderId(),
                event.getCustomerId(),
                event.getCustomerEmail(),
                Notification.NotificationType.ORDER_CONFIRMATION,
                Notification.NotificationChannel.SMS,
                "Order Confirmation", // SMS doesn't have subjects, but we store it
                message,
                phoneNumber
            );

            // Save notification record
            notification = notificationRepository.save(notification);

            // Simulate SMS sending
            boolean smsSent = simulateSmsSending(phoneNumber, message);

            if (smsSent) {
                notification.markAsSent();
                System.out.println("✅ Order confirmation SMS sent to: " + phoneNumber);
            } else {
                notification.markAsFailed("Simulated SMS failure");
                System.err.println("❌ Failed to send order confirmation SMS");
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("❌ Error sending order confirmation SMS: " + e.getMessage());
        }
    }

    /**
     * Send order status update SMS
     */
    public void sendOrderStatusUpdateSms(OrderStatusChangedEvent event) {
        if (!smsEnabled) {
            System.out.println("📱 SMS notifications disabled - skipping");
            return;
        }

        try {
            // For demo, we'll use a fake phone number
            String phoneNumber = "+1-555-" + String.format("%04d", event.getCustomerId().intValue() % 10000);
            
            String message = buildStatusUpdateSms(event);
            
            Notification notification = new Notification(
                event.getOrderId(),
                event.getCustomerId(),
                event.getCustomerEmail(),
                Notification.NotificationType.ORDER_STATUS_UPDATE,
                Notification.NotificationChannel.SMS,
                "Status Update",
                message,
                phoneNumber
            );

            // Save notification record
            notification = notificationRepository.save(notification);

            // Simulate SMS sending
            boolean smsSent = simulateSmsSending(phoneNumber, message);

            if (smsSent) {
                notification.markAsSent();
                System.out.println("✅ Status update SMS sent to: " + phoneNumber + 
                                 " (" + event.getOldStatus() + " → " + event.getNewStatus() + ")");
            } else {
                notification.markAsFailed("Simulated SMS failure");
                System.err.println("❌ Failed to send status update SMS");
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("❌ Error sending status update SMS: " + e.getMessage());
        }
    }

    /**
     * Build order confirmation SMS message (keep it short!)
     */
    private String buildOrderConfirmationSms(OrderPlacedEvent event) {
        return String.format(
            "ByteBites: Order #%d confirmed! %s is preparing your order for delivery to %s. Total: $%.2f. Track your order in the app!",
            event.getOrderId(),
            event.getRestaurantName(),
            event.getDeliveryAddress(),
            event.getTotalAmount()
        );
    }

    /**
     * Build status update SMS message (keep it short!)
     */
    private String buildStatusUpdateSms(OrderStatusChangedEvent event) {
        String statusMessage = switch (event.getNewStatus()) {
            case "CONFIRMED" -> "confirmed and being prepared";
            case "PREPARING" -> "being prepared by the restaurant";
            case "DELIVERED" -> "delivered! Enjoy your meal!";
            case "CANCELLED" -> "cancelled. Contact support if needed.";
            default -> "updated to " + event.getNewStatus();
        };

        return String.format(
            "ByteBites: Order #%d is now %s. Restaurant: %s",
            event.getOrderId(),
            statusMessage,
            event.getRestaurantName()
        );
    }

    /**
     * Simulate SMS sending (replace with real SMS service in production)
     */
    private boolean simulateSmsSending(String phoneNumber, String message) {
        try {
            // Simulate processing time
            Thread.sleep(50);
            
            // Simulate 98% success rate (SMS is usually more reliable than email)
            boolean success = Math.random() > 0.02;
            
            if (success) {
                System.out.println("\n📱 ================== SMS SENT ==================");
                System.out.println("To: " + phoneNumber);
                System.out.println("Provider: " + smsProvider);
                System.out.println("Message: " + message);
                System.out.println("Length: " + message.length() + " characters");
                System.out.println("===============================================\n");
            }
            
            return success;
            
        } catch (Exception e) {
            return false;
        }
    }
}