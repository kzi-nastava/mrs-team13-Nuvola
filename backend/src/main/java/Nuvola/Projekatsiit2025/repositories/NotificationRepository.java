package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Notification;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndTypeOrderByTimeDesc(User user, NotificationType type);
    List<Notification> findByUserAndTypeAndIsReadFalseOrderByTimeDesc(User user, NotificationType type);

}
