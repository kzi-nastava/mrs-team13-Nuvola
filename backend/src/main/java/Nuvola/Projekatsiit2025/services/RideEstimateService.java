package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.RideEstimateRequestDTO;
import Nuvola.Projekatsiit2025.dto.RideEstimateResponseDTO;

public interface RideEstimateService {
    RideEstimateResponseDTO estimateRide(RideEstimateRequestDTO request);
}
