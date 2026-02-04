package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.RideEstimateRequestDTO;
import Nuvola.Projekatsiit2025.dto.RideEstimateResponseDTO;
import Nuvola.Projekatsiit2025.services.RideEstimateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//2.1.2
@RestController
@RequestMapping("/api/rides")
public class RideEstimateController {

    private final RideEstimateService rideEstimateService;

    public RideEstimateController(RideEstimateService rideEstimateService) {
        this.rideEstimateService = rideEstimateService;
    }

    @PostMapping("/estimate")
    public RideEstimateResponseDTO estimate(@RequestBody RideEstimateRequestDTO request) {
        return rideEstimateService.estimateRide(request);
    }
}

