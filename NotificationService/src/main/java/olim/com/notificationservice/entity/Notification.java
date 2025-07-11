package olim.com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Notification entity to track sent notifications
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_email")
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(name = "subject")
    private String subject;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "recipient")
    private String recipient; // email address or phone number

    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * Types of notifications
     */
    public enum NotificationType {
        ORDER_CONFIRMATION,    // When order is placed
        ORDER_STATUS_UPDATE,   // When status changes
        ORDER_DELIVERED,       // When order is delivered
        ORDER_CANCELLED        // When order is cancelled
    }

    /**
     * Notification channels
     */
    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH
    }

    /**
     * Notification status
     */
    public enum NotificationStatus {
        PENDING,     // Waiting to be sent
        SENT,        // Successfully sent
        FAILED,      // Failed to send
        RETRYING     // Being retried
    }

    // JPA lifecycle callbacks
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Simple constructors
    public Notification(Long orderId, Long customerId, String customerEmail,
                       NotificationType type, NotificationChannel channel,
                       String subject, String message, String recipient) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.message = message;
        this.recipient = recipient;
        this.status = NotificationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    // Utility methods
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.status = NotificationStatus.RETRYING;
    }
}