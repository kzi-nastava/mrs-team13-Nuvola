package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.NotificationDTO;
import Nuvola.Projekatsiit2025.exceptions.UserNotFoundException;
import Nuvola.Projekatsiit2025.model.Notification;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import Nuvola.Projekatsiit2025.repositories.NotificationRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class NotificationServiceImpl implements NotificationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendNotification(Long recipientId, String title, String message, NotificationType notificationType) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + recipientId + " not found"));
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(notificationType);
        notification.setUser(recipient);
        notificationRepository.save(notification);

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setMessage(message);
        notificationDTO.setType(notificationType.toString());

        simpMessagingTemplate.convertAndSend("/topic/notifications/" + recipientId, notificationDTO);
    }
}
