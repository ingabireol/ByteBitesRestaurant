package olim.com.notificationservice.controller;

import olim.com.notificationservice.entity.Notification;
import olim.com.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notification Controller
 * 
 * Provides endpoints to view notification history and statistics
 * Mainly for monitoring and debugging purposes
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Get all notifications (for admin/debugging)
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getNotificationsByCustomer(@PathVariable Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by order ID
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Notification>> getNotificationsByOrder(@PathVariable Long orderId) {
        List<Notification> notifications = notificationRepository.findByOrderId(orderId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by status
        stats.put("total", notificationRepository.count());
        stats.put("sent", notificationRepository.countByStatus(Notification.NotificationStatus.SENT));
        stats.put("failed", notificationRepository.countByStatus(Notification.NotificationStatus.FAILED));
        stats.put("pending", notificationRepository.countByStatus(Notification.NotificationStatus.PENDING));
        
        // Count by type
        stats.put("orderConfirmations", notificationRepository.countByType(Notification.NotificationType.ORDER_CONFIRMATION));
        stats.put("statusUpdates", notificationRepository.countByType(Notification.NotificationType.ORDER_STATUS_UPDATE));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get failed notifications (for retry)
     */
    @GetMapping("/failed")
    public ResponseEntity<List<Notification>> getFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findByStatus(Notification.NotificationStatus.FAILED);
        return ResponseEntity.ok(failedNotifications);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-service");
        response.put("message", "Notification service is running and listening for events");
        return ResponseEntity.ok(response);
    }
}