package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.PanicStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class PanicEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    // ko je triggerovao (može Driver ili RegisteredUser)
    // Ako ti je lakše: stavi User (bazni) ili RegisteredUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_id", nullable = false)
    private User triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PanicStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    public PanicEvent() {
        this.createdAt = Instant.now();
        this.status = PanicStatus.ACTIVE;
    }
}
