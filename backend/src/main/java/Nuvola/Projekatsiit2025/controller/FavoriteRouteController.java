package Nuvola.Projekatsiit2025.controller;

import Nuvola.Projekatsiit2025.dto.FavoriteRouteDTO;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.model.User;
import Nuvola.Projekatsiit2025.services.FavoriteRouteService;
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

    @GetMapping
    public List<FavoriteRouteDTO> getFavorites(
            @AuthenticationPrincipal User user) {

        return favoriteRouteService.getFavorites((RegisteredUser) user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        favoriteRouteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
