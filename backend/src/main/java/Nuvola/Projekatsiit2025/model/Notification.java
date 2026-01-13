package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.NotificationType;
import lombok.Data;

@Data
public class Notification {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
}
