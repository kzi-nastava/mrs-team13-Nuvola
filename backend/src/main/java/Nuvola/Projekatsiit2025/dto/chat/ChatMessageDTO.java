package Nuvola.Projekatsiit2025.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long chatId;
    private String content;
    private LocalDateTime sentAt;
    Long senderId;
    String senderName;
}
