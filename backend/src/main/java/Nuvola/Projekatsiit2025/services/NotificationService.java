package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.NotificationDTO;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;

import java.util.List;

public interface NotificationService {
        void sendNotification(Long recipientId, String title, String message, NotificationType notificationType);
        List<NotificationDTO> getNotifications(Long recipientId);
}
