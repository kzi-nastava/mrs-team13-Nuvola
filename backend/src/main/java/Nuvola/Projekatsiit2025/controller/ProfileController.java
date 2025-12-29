package Nuvola.Projekatsiit2025.controller;


import Nuvola.Projekatsiit2025.dto.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    //2.3
    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile() {

        ProfileResponseDTO response = new ProfileResponseDTO();
        response.setId(1L);
        response.setEmail("user@mail.com");
        response.setFirstName("Petar");
        response.setLastName("Petrovic");
        response.setPhone("061234567");
        response.setAddress("Novi Sad");

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(@RequestBody UpdateProfileDTO dto) {

        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDTO dto) {

        return ResponseEntity.ok().build();
    }
}
