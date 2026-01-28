package Nuvola.Projekatsiit2025.repositories;

import Nuvola.Projekatsiit2025.model.PanicEvent;
import Nuvola.Projekatsiit2025.model.enums.PanicStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PanicEventRepository extends JpaRepository<PanicEvent, UUID> {
    boolean existsByRideIdAndStatus(Long rideId, PanicStatus status);
    List<PanicEvent> findByStatusOrderByCreatedAtDesc(PanicStatus status);
}
