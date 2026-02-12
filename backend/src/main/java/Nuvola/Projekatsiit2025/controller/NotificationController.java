package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.AdminRideDetailsDTO;
import Nuvola.Projekatsiit2025.dto.NotificationDTO;
import Nuvola.Projekatsiit2025.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NotificationDTO>> getUsersNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
}
