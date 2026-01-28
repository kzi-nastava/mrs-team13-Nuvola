package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.PanicCreateRequest;
import Nuvola.Projekatsiit2025.dto.PanicEventDto;
import Nuvola.Projekatsiit2025.model.PanicEvent;
import Nuvola.Projekatsiit2025.model.Ride;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.model.enums.PanicStatus;
import Nuvola.Projekatsiit2025.repositories.PanicEventRepository;
import Nuvola.Projekatsiit2025.repositories.RideRepository;
import Nuvola.Projekatsiit2025.repositories.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PanicService {

    private final PanicEventRepository panicRepo;
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate ws;

    public PanicService(PanicEventRepository panicRepo,
                        RideRepository rideRepo,
                        UserRepository userRepo,
                        SimpMessagingTemplate ws) {
        this.panicRepo = panicRepo;
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.ws = ws;
    }

    @Transactional
    public PanicEventDto trigger(PanicCreateRequest req) {

        // 1) Spreči dupli PANIC za istu vožnju
        if (panicRepo.existsByRideIdAndStatus(req.rideId(), PanicStatus.ACTIVE)) {
            throw new RuntimeException("PANIC already active for this ride");
        }

        // 2) Nađi vožnju
        Ride ride = rideRepo.findById(req.rideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // 3) Obeleži vožnju kao panic
        ride.setPanic(true);
        rideRepo.save(ride);

        // 4) Ko je ulogovan
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getName(); // username ili email (zavisi od auth-a)

        User user = findUserByPrincipal(principal);

        // 5) Kreiraj PanicEvent
        PanicEvent e = new PanicEvent();
        e.setRide(ride);
        e.setTriggeredBy(user);
        e.setStatus(PanicStatus.ACTIVE);
        e.setCreatedAt(Instant.now());

        panicRepo.save(e);

        // 6) DTO za admin notifikaciju
        PanicEventDto dto = new PanicEventDto(
                e.getId(),
                ride.getId(),
                user.getId(),
                e.getStatus(),
                e.getCreatedAt()
        );

        // 7) Realtime notifikacija adminima
        ws.convertAndSend("/topic/panic", dto);

        return dto;
    }

    @Transactional
    public void resolve(UUID panicId) {

        PanicEvent e = panicRepo.findById(panicId)
                .orElseThrow(() -> new RuntimeException("Panic event not found"));

        if (e.getStatus() != PanicStatus.ACTIVE) return;

        // 1) Resolve event
        e.setStatus(PanicStatus.RESOLVED);
        e.setResolvedAt(Instant.now());
        panicRepo.save(e);

        // 2) Vrati ride panic flag na false
        Ride ride = e.getRide();
        if (ride != null) {
            ride.setPanic(false);
            rideRepo.save(ride);
        }

        // 3) Obavesti admin UI
        ws.convertAndSend("/topic/panic-resolved", panicId);
    }

    /**
     * Pokušaj da pronađeš user-a po username ili email (zavisno kako ti je auth name podešen).
     * Prilagodi ovde ako tvoj UserRepository ima drugačije metode.
     */
    private User findUserByPrincipal(String principal) {
        // Ako imaš findByUsername
        try {
            var m = userRepo.getClass().getMethod("findByUsername", String.class);
            Object res = m.invoke(userRepo, principal);
            if (res instanceof java.util.Optional<?> opt && opt.isPresent()) {
                return (User) opt.get();
            }
        } catch (Exception ignored) {}

        // Ako imaš findByEmail
        try {
            var m = userRepo.getClass().getMethod("findByEmail", String.class);
            Object res = m.invoke(userRepo, principal);
            if (res instanceof java.util.Optional<?> opt && opt.isPresent()) {
                return (User) opt.get();
            }
        } catch (Exception ignored) {}

        throw new RuntimeException("User not found for principal: " + principal);
    }
}
