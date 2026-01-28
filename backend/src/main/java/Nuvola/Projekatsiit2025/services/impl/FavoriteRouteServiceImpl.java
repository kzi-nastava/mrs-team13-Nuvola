package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.FavoriteRouteDTO;
import Nuvola.Projekatsiit2025.model.FavoriteRoute;
import Nuvola.Projekatsiit2025.model.Location;
import Nuvola.Projekatsiit2025.model.RegisteredUser;
import Nuvola.Projekatsiit2025.repositories.FavoriteRouteRepository;
import Nuvola.Projekatsiit2025.services.FavoriteRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteRouteServiceImpl implements FavoriteRouteService {

    @Autowired
    private FavoriteRouteRepository favoriteRouteRepository;

    @Override
    public List<FavoriteRouteDTO> getFavorites(RegisteredUser user) {
        return favoriteRouteRepository.findByUserId(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        favoriteRouteRepository.deleteById(id);
    }

    private FavoriteRouteDTO toDto(FavoriteRoute fr) {
        FavoriteRouteDTO dto = new FavoriteRouteDTO();
        dto.setId(fr.getId());
        dto.setStartLocation(fr.getFrom().getTitle());
        dto.setDestination(fr.getTo().getTitle());
        dto.setStops(
                fr.getStops().stream()
                        .map(Location::getTitle)
                        .toList()
        );
        return dto;
    }
}
