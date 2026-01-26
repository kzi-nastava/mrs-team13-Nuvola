package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.Location;
import lombok.Data;

import java.util.List;

@Data
public class RouteDTO {
    private List<Location> stops;


}
