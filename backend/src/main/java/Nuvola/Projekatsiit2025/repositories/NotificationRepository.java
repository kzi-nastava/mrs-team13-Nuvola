package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
