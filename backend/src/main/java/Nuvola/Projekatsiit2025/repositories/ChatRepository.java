package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.dto.chat.AdminInboxItemDTO;
import Nuvola.Projekatsiit2025.model.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
    select new Nuvola.Projekatsiit2025.dto.chat.AdminInboxItemDTO(
      c.id,
      u.id,
      concat(u.firstName, ' ', u.lastName),
      lm.content,
      lm.sentAt,
      concat(s.firstName, ' ', s.lastName)
    )
    from Chat c
    join c.owner u
    left join ChatMessage lm
      on lm.chat = c
     and lm.sentAt = (
        select max(m2.sentAt)
        from ChatMessage m2
        where m2.chat = c
     )
    left join lm.sender s
    order by lm.sentAt desc nulls last, c.id desc
  """)
    Page<AdminInboxItemDTO> findAdminInbox(Pageable pageable);
}
