
package olim.com.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime orderTime;
    private List<OrderItemInfo> items;
    
    /**
     * Order item information for the event
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String itemName;
        private Integer quantity;
        private BigDecimal price;
    }
}