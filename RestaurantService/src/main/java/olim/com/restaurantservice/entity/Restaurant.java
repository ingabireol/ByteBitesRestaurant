package olim.com.restaurantservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Restaurant entity for ByteBites
 */
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Restaurant name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Cuisine type is required")
    private CuisineType cuisineType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // References User ID from Auth Service

    @Column(name = "is_open")
    private boolean isOpen = true;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.valueOf(2.99);

    @Column(name = "minimum_order", precision = 10, scale = 2)
    private BigDecimal minimumOrder = BigDecimal.valueOf(10.00);

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.valueOf(4.0);

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Menu items relationship
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems = new ArrayList<>();

    /**
     * Simple cuisine types
     */
    public enum CuisineType {
        ITALIAN, CHINESE, INDIAN, MEXICAN, AMERICAN,
        FAST_FOOD, PIZZA, OTHER
    }

    // JPA lifecycle callbacks
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    // Simple constructor for creating new restaurants
    public Restaurant(String name, String description, String address,
                      String phoneNumber, CuisineType cuisineType, Long ownerId) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.cuisineType = cuisineType;
        this.ownerId = ownerId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}