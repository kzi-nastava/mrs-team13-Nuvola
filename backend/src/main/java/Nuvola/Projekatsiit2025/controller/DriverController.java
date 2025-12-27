package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.CreateDriverDTO;
import Nuvola.Projekatsiit2025.dto.CreatedDriverDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    //2.2.3
    @PostMapping
    public ResponseEntity<CreatedDriverDTO> createDriver(@RequestBody CreateDriverDTO driverDTO) {

        CreatedDriverDTO response = new CreatedDriverDTO();
        response.setId(1L);
        response.setEmail(driverDTO.getEmail());
        response.setFirstName(driverDTO.getFirstName());
        response.setLastName(driverDTO.getLastName());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
