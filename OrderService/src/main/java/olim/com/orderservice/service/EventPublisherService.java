package olim.com.orderservice.service;

import olim.com.orderservice.entity.Order;
import olim.com.orderservice.entity.OrderItem;
import olim.com.orderservice.event.OrderPlacedEvent;
import olim.com.orderservice.event.OrderStatusChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Publisher Service
 * 
 * Publishes events to RabbitMQ when important order actions happen
 */
@Service
public class EventPublisherService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${bytebites.messaging.exchange}")
    private String exchange;

    @Value("${bytebites.messaging.routing-keys.order-placed}")
    private String orderPlacedRoutingKey;

    @Value("${bytebites.messaging.routing-keys.order-status-changed}")
    private String orderStatusChangedRoutingKey;

    /**
     * Publish OrderPlacedEvent when a new order is created
     */
    public void publishOrderPlacedEvent(Order order, String customerEmail, String customerName) {
        try {
            // Convert order items to event format
            List<OrderPlacedEvent.OrderItemInfo> items = order.getOrderItems().stream()
                    .map(item -> new OrderPlacedEvent.OrderItemInfo(
                            item.getMenuItemName(),
                            item.getQuantity(),
                            item.getPrice()
                    ))
                    .collect(Collectors.toList());

            // Create the event
            OrderPlacedEvent event = new OrderPlacedEvent(
                    order.getId(),
                    order.getCustomerId(),
                    customerEmail,
                    customerName,
                    order.getRestaurantId(),
                    order.getRestaurantName(),
                    order.getTotalAmount(),
                    order.getDeliveryAddress(),
                    order.getCreatedAt(),
                    items
            );

            // Publish the event
            rabbitTemplate.convertAndSend(exchange, orderPlacedRoutingKey, event);
            
            System.out.println("📨 Published OrderPlacedEvent for order ID: " + order.getId());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to publish OrderPlacedEvent: " + e.getMessage());
            // Don't throw exception - event publishing failure shouldn't break order creation
        }
    }

    /**
     * Publish OrderStatusChangedEvent when order status is updated
     */
    public void publishOrderStatusChangedEvent(Order order, String oldStatus, String newStatus, 
                                             String changedBy, String customerEmail) {
        try {
            // Create the event
            OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                    order.getId(),
                    order.getCustomerId(),
                    customerEmail,
                    order.getRestaurantId(),
                    order.getRestaurantName(),
                    oldStatus,
                    newStatus,
                    LocalDateTime.now(),
                    changedBy
            );

            // Publish the event
            rabbitTemplate.convertAndSend(exchange, orderStatusChangedRoutingKey, event);
            
            System.out.println("📨 Published OrderStatusChangedEvent for order ID: " + order.getId() + 
                             " (" + oldStatus + " → " + newStatus + ")");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to publish OrderStatusChangedEvent: " + e.getMessage());
            // Don't throw exception - event publishing failure shouldn't break status update
        }
    }

    /**
     * Test method to verify RabbitMQ connection
     */
    public void testConnection() {
        try {
            rabbitTemplate.convertAndSend(exchange, "test", "Test message from Order Service");
            System.out.println("✅ RabbitMQ connection test successful");
        } catch (Exception e) {
            System.err.println("❌ RabbitMQ connection test failed: " + e.getMessage());
        }
    }
}