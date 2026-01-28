package Nuvola.Projekatsiit2025.dto;


import Nuvola.Projekatsiit2025.model.enums.PanicStatus;

import java.time.Instant;
import java.util.UUID;

public record PanicEventDto(

        UUID id,
        Long rideId,
        Long triggeredById,
        PanicStatus status,
        Instant createdAt
) {}
