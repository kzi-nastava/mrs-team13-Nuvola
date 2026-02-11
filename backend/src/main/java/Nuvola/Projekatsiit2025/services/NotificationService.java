package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.model.enums.NotificationType;

public interface NotificationService {
        void sendNotification(Long recipientId, String title, String message, NotificationType notificationType);
}
