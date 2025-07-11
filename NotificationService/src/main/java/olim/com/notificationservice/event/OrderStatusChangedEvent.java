package olim.com.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event received when order status changes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {
    
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private Long restaurantId;
    private String restaurantName;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
    private String changedBy; // "customer", "restaurant", "system"
}