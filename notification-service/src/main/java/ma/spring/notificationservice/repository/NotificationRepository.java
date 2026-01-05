package ma.spring.notificationservice.repository;

import ma.spring.notificationservice.model.Notification;
import ma.spring.notificationservice.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByStatusAndCreatedAtBefore(NotificationStatus status, LocalDateTime createdBefore);
    Long countByUserIdAndStatus(Long userId, NotificationStatus status);
}