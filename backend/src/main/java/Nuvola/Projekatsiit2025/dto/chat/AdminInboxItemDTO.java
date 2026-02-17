package Nuvola.Projekatsiit2025.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminInboxItemDTO {
    private Long chatId;
    private Long userId;
    private String ownerName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private String lastMessageSenderName;

}
