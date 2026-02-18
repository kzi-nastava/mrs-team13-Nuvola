package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationDTO {
    String title;
    String message;
    String type;

    public NotificationDTO(Notification notification) {
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.type = notification.getType().name();
    }
}
