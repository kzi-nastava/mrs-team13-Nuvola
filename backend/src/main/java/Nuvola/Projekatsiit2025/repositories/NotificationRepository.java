package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
