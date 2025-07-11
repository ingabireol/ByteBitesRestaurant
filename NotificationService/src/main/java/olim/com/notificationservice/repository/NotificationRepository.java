package olim.com.notificationservice.repository;

import olim.com.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by customer ID
     */
    List<Notification> findByCustomerId(Long customerId);

    /**
     * Find notifications by order ID
     */
    List<Notification> findByOrderId(Long orderId);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(Notification.NotificationStatus status);

    /**
     * Find notifications by type and customer
     */
    List<Notification> findByTypeAndCustomerId(Notification.NotificationType type, Long customerId);

    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < 3")
    List<Notification> findRetriableNotifications();

    /**
     * Find notifications sent in a date range
     */
    @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :startDate AND :endDate")
    List<Notification> findNotificationsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Count notifications by status
     */
    long countByStatus(Notification.NotificationStatus status);

    /**
     * Count notifications by type
     */
    long countByType(Notification.NotificationType type);
}