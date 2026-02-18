package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatIdOrderBySentAtAsc(Long chatId);

}
