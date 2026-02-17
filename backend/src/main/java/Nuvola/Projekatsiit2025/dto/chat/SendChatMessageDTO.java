package Nuvola.Projekatsiit2025.dto.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class SendChatMessageDTO {
    private Long senderId;
    private String content;

}
