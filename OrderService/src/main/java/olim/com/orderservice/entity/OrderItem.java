package olim.com.orderservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Menu item ID is required")
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId; // References MenuItem from Restaurant Service

    @NotBlank(message = "Menu item name is required")
    @Column(name = "menu_item_name", nullable = false)
    private String menuItemName; // Cached for display

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Total price for this item (quantity * unit price)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Order relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // JPA lifecycle callbacks
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Simple constructor
    public OrderItem(Long menuItemId, String menuItemName, Integer quantity, BigDecimal price) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
}