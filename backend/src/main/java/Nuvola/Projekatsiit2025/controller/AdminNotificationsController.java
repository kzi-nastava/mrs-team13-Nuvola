package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.model.Notification;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import Nuvola.Projekatsiit2025.repositories.NotificationRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminNotificationsController {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public AdminNotificationsController(UserRepository userRepository,
                                        NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/panic-notifications/{adminId}")
    public List<Notification> getPanicNotifications(@PathVariable Long adminId,
                                                    @RequestParam(defaultValue = "false") boolean unreadOnly) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (unreadOnly) {
            return notificationRepository.findByUserAndTypeAndIsReadFalseOrderByTimeDesc(admin, NotificationType.PANIC);
        }
        return notificationRepository.findByUserAndTypeOrderByTimeDesc(admin, NotificationType.PANIC);
    }
}
