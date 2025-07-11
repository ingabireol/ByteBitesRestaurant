package olim.com.notificationservice.service;

import olim.com.notificationservice.event.OrderPlacedEvent;
import olim.com.notificationservice.event.OrderStatusChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Order Event Listener
 * 
 * Listens for order-related events from RabbitMQ and triggers appropriate notifications
 */
@Service
public class OrderEventListener {

    @Autowired
    private EmailNotificationService emailService;

    @Autowired
    private SmsNotificationService smsService;

    /**
     * Listen for OrderPlacedEvent from Order Service
     * Triggered when a new order is created
     */
    @RabbitListener(queues = "${bytebites.messaging.queues.order-placed}")
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        try {
            System.out.println("\n🎉 =============== ORDER PLACED EVENT ===============");
            System.out.println("📦 Order ID: " + event.getOrderId());
            System.out.println("👤 Customer: " + event.getCustomerName() + " (" + event.getCustomerEmail() + ")");
            System.out.println("🏪 Restaurant: " + event.getRestaurantName());
            System.out.println("💰 Total: $" + event.getTotalAmount());
            System.out.println("📍 Address: " + event.getDeliveryAddress());
            System.out.println("🕐 Time: " + event.getOrderTime());
            System.out.println("📋 Items: " + event.getItems().size() + " items");
            System.out.println("================================================\n");

            // Send confirmation notifications
            System.out.println("📨 Sending order confirmation notifications...");
            
            // Send email confirmation
            emailService.sendOrderConfirmationEmail(event);
            
            // Send SMS confirmation
            smsService.sendOrderConfirmationSms(event);
            
            System.out.println("✅ Order confirmation notifications processing completed\n");

        } catch (Exception e) {
            System.err.println("❌ Error processing OrderPlacedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Listen for OrderStatusChangedEvent from Order Service
     * Triggered when order status is updated
     */
    @RabbitListener(queues = "${bytebites.messaging.queues.order-status-changed}")
    public void handleOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        try {
            System.out.println("\n🔄 ============= ORDER STATUS CHANGED =============");
            System.out.println("📦 Order ID: " + event.getOrderId());
            System.out.println("👤 Customer: " + event.getCustomerEmail());
            System.out.println("🏪 Restaurant: " + event.getRestaurantName());
            System.out.println("📊 Status: " + event.getOldStatus() + " → " + event.getNewStatus());
            System.out.println("👨‍💼 Changed by: " + event.getChangedBy());
            System.out.println("🕐 Time: " + event.getChangedAt());
            System.out.println("================================================\n");

            // Only send notifications for significant status changes
            if (shouldNotifyForStatusChange(event.getNewStatus())) {
                System.out.println("📨 Sending status update notifications...");
                
                // Send email notification
                emailService.sendOrderStatusUpdateEmail(event);
                
                // Send SMS notification for important statuses
                if (isImportantStatusChange(event.getNewStatus())) {
                    smsService.sendOrderStatusUpdateSms(event);
                }
                
                System.out.println("✅ Status update notifications processing completed\n");
            } else {
                System.out.println("ℹ️ Status change doesn't require customer notification\n");
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing OrderStatusChangedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Determine if we should send notifications for this status change
     */
    private boolean shouldNotifyForStatusChange(String newStatus) {
        return switch (newStatus) {
            case "CONFIRMED", "PREPARING", "DELIVERED", "CANCELLED" -> true;
            case "PENDING" -> false; // Don't notify for initial pending status
            default -> true;
        };
    }

    /**
     * Determine if this is an important status change that warrants SMS
     */
    private boolean isImportantStatusChange(String newStatus) {
        return switch (newStatus) {
            case "DELIVERED", "CANCELLED" -> true; // Important final states
            case "CONFIRMED", "PREPARING" -> false; // Email only for these
            default -> false;
        };
    }
}