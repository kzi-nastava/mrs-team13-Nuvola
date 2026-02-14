package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.chat.AdminInboxItemDTO;
import Nuvola.Projekatsiit2025.dto.chat.AdminSendChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.ChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.SendChatMessageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupportChatService {
    void sendMessage(SendChatMessageDTO dto);
    void adminSendMessage(AdminSendChatMessageDTO dto);
    List<ChatMessageDTO> getMessages(Long userId);
    Page<AdminInboxItemDTO> getInboxForAdmin(Long adminId, Pageable pageable);
}
