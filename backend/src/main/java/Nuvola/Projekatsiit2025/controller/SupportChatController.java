package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.chat.AdminInboxItemDTO;
import Nuvola.Projekatsiit2025.dto.chat.ChatMessageDTO;
import Nuvola.Projekatsiit2025.services.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "http://localhost:4200")
public class SupportChatController {

    @Autowired
    private SupportChatService supportChatService;

    @GetMapping(value = "/chat/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable Long userId) {
        List<ChatMessageDTO> messages =  supportChatService.getMessages(userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/inbox")
    public ResponseEntity<Page<AdminInboxItemDTO>> inbox(Pageable pageable, @RequestParam Long adminId) {
        return ResponseEntity.ok(supportChatService.getInboxForAdmin(adminId, pageable));
    }

}
