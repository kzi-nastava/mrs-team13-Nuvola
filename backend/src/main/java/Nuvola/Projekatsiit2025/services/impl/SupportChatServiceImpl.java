package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.chat.AdminInboxItemDTO;
import Nuvola.Projekatsiit2025.dto.chat.AdminSendChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.ChatMessageDTO;
import Nuvola.Projekatsiit2025.dto.chat.SendChatMessageDTO;
import Nuvola.Projekatsiit2025.exceptions.UserNotFoundException;
import Nuvola.Projekatsiit2025.model.Admin;
import Nuvola.Projekatsiit2025.model.Chat;
import Nuvola.Projekatsiit2025.model.ChatMessage;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.AdminRepository;
import Nuvola.Projekatsiit2025.repositories.ChatMessageRepository;
import Nuvola.Projekatsiit2025.repositories.ChatRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import Nuvola.Projekatsiit2025.services.SupportChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupportChatServiceImpl implements SupportChatService {

    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AdminRepository adminRepository;


    public void sendMessage(SendChatMessageDTO dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new UserNotFoundException("User " + dto.getSenderId() + " not found"));

        Chat chat = sender.getChat();
        if (chat == null) {
            chat = new Chat();
            sender.setChat(chat);
            chat.setOwner(sender);
            userRepository.save(sender);
            chat = sender.getChat();
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChat(chat);
        chatMessage.setSender(sender);
        chatMessage.setContent(dto.getContent());
        chatMessage.setSentAt(LocalDateTime.now());

        chatMessage = chatMessageRepository.save(chatMessage);

        // if needed
        //chat.getMessages().add(chatMessage);

        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setId(chatMessage.getId());
        chatMessageDTO.setContent(chatMessage.getContent());
        chatMessageDTO.setSenderId(sender.getId());
        chatMessageDTO.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        chatMessageDTO.setChatId(chat.getId());

        template.convertAndSend("/topic/chats/users/" + sender.getId(), chatMessageDTO);
        template.convertAndSend("/topic/chats/users/all", chatMessageDTO);

    }

    public void adminSendMessage(AdminSendChatMessageDTO dto) {
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("User " + dto.getReceiverId() + " not found"));

        if (receiver.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new IllegalArgumentException("Receiver cannot be an admin");
        }

        Admin sender = adminRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new UserNotFoundException("User " + dto.getSenderId() + " not found"));

        Chat chat = receiver.getChat();
        if (chat == null) {
            chat = new Chat();
            receiver.setChat(chat);
            chat.setOwner(receiver);
            userRepository.save(receiver);
            chat = receiver.getChat();
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChat(chat);
        chatMessage.setSender(sender);
        chatMessage.setContent(dto.getContent());
        chatMessage.setSentAt(LocalDateTime.now());

        chatMessage = chatMessageRepository.save(chatMessage);

        // if needed
        //chat.getMessages().add(chatMessage);

        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setId(chatMessage.getId());
        chatMessageDTO.setContent(chatMessage.getContent());
        chatMessageDTO.setSenderId(sender.getId());
        chatMessageDTO.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        chatMessageDTO.setChatId(chat.getId());

        template.convertAndSend("/topic/chats/users/" + receiver.getId(), chatMessageDTO);
        template.convertAndSend("/topic/chats/users/all", chatMessageDTO);
    }

    public List<ChatMessageDTO> getMessages(Long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User " + userId + " not found"));

        if (me.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new IllegalArgumentException("Admin does not have the one chat related to them, but has access to all of them");
        }

        Chat chat = me.getChat();
        if (chat == null) return List.of();

        return chatMessageRepository.findByChatIdOrderBySentAtAsc(chat.getId())
                .stream()
                .map(m -> new ChatMessageDTO(
                        m.getId(),
                        chat.getId(),
                        m.getContent(),
                        m.getSentAt(),
                        m.getSender().getId(),
                        m.getSender().getFirstName() + " " + m.getSender().getLastName()
                ))
                .toList();
    }



    public Page<AdminInboxItemDTO> getInboxForAdmin(Long adminId, Pageable pageable) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin " + adminId + " not found"));

        return chatRepository.findAdminInbox(pageable);
    }
}
