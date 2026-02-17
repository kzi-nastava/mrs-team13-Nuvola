package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.chat.AdminSendChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.ChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.SendChatMessageDTO;
import Nuvola.Projekatsiit2025.services.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SupportChatWsController {

    @Autowired
    private SupportChatService supportChatService;

    @MessageMapping("/chats/send")
    public void send(SendChatMessageDTO dto) {
        supportChatService.sendMessage(dto);
    }

    @MessageMapping("/admin/chats/send")
    public void adminSend(AdminSendChatMessageDTO dto) {
        supportChatService.adminSendMessage(dto);
    }
}
