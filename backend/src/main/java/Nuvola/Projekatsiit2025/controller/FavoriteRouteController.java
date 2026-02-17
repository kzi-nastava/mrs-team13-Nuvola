package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.FavoriteRouteDTO;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.repositories.RegisteredUserRepository;
import Nuvola.Projekatsiit2025.services.FavoriteRouteService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteRouteController {
    @Autowired
    private FavoriteRouteService favoriteRouteService;

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @GetMapping
    @Transactional
    public List<FavoriteRouteDTO> getFavorites(@AuthenticationPrincipal User user) {
        RegisteredUser ru = registeredUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ru.getFavoriteRoutes().stream()
                .map(route -> {
                    FavoriteRouteDTO dto = new FavoriteRouteDTO();
                    dto.setId(route.getId());
                    dto.setStartLocation(route.getPickup() != null ? route.getPickup().getAddress() : "");
                    dto.setDestination(route.getDropoff() != null ? route.getDropoff().getAddress() : "");
                    dto.setStops(
                            route.getStops() != null
                                    ? route.getStops().stream()
                                    .map(s -> s.getAddress())
                                    .toList()
                                    : List.of()
                    );
                    return dto;
                })
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteRouteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
