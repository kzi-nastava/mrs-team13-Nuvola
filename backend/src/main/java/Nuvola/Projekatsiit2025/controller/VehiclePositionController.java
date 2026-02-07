package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.ActiveVehicleDTO;
import Nuvola.Projekatsiit2025.dto.VehiclePositionDTO;
import Nuvola.Projekatsiit2025.exceptions.ResourceConflictException;
import Nuvola.Projekatsiit2025.model.Driver;
import Nuvola.Projekatsiit2025.model.enums.DriverStatus;
import Nuvola.Projekatsiit2025.repositories.DriverRepository;
import Nuvola.Projekatsiit2025.util.VehicleLocationStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Map;

@Controller
public class VehiclePositionController {

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private VehicleLocationStore store;



    @MessageMapping("/vehicle/position")
    public void receiveVehiclePosition(VehiclePositionDTO vehiclePosition) {
        Driver driver = driverRepository.findByVehicleId(vehiclePosition.getVehicleId()).orElse(null);
        if (driver == null) return;

        DriverStatus status = driver.getStatus();
        if (status == DriverStatus.INACTIVE) {
            //throw new ResourceConflictException(driver.getUsername(), "Driver status is INACTIVE, cannot update position");
            return;
        }

        ActiveVehicleDTO activeVehicleDTO = new ActiveVehicleDTO();
        activeVehicleDTO.setVehicleId(vehiclePosition.getVehicleId());
        activeVehicleDTO.setLatitude(vehiclePosition.getLatitude());
        activeVehicleDTO.setLongitude(vehiclePosition.getLongitude());
        activeVehicleDTO.setOccupied(status == DriverStatus.BUSY);

        store.update(vehiclePosition);

        // 1) Ride tracking - sends vehicle's position to its subscribers
        simpMessagingTemplate.convertAndSend("/topic/position/" + vehiclePosition.getVehicleId(), vehiclePosition);

        // 2) Active vehicles and their status - send snapshot of all active vehicles to subscribers

        simpMessagingTemplate.convertAndSend("/topic/position/all", activeVehicleDTO);
    }

}
