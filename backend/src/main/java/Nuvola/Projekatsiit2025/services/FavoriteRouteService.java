package Nuvola.Projekatsiit2025.services;

import Nuvola.Projekatsiit2025.dto.FavoriteRouteDTO;
import Nuvola.Projekatsiit2025.model.RegisteredUser;

import java.util.List;

public interface FavoriteRouteService {
    List<FavoriteRouteDTO> getFavorites(RegisteredUser user);
    void delete(Long id);
}
