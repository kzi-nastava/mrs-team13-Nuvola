package Nuvola.Projekatsiit2025.services.impl;

import Nuvola.Projekatsiit2025.dto.RideEstimateRequestDTO;
import Nuvola.Projekatsiit2025.dto.RideEstimateResponseDTO;
import Nuvola.Projekatsiit2025.services.RideEstimateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class RideEstimateServiceImpl implements RideEstimateService {

    private final RestClient restClient = RestClient.create();

    @Override
    public RideEstimateResponseDTO estimateRide(RideEstimateRequestDTO request) {

        Point start = geocode(request.getStartAddress());
        Point end = geocode(request.getDestinationAddress());

        String url = String.format(
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                start.lon, start.lat,
                end.lon, end.lat
        );

        Map<?, ?> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);

        List<?> routes = (List<?>) response.get("routes");
        if (routes == null || routes.isEmpty()) {
            throw new RuntimeException("Route not found");
        }

        Map<?, ?> route = (Map<?, ?>) routes.get(0);
        double durationSeconds = ((Number) route.get("duration")).doubleValue();

        int minutes = (int) Math.round(durationSeconds / 60.0);

        return new RideEstimateResponseDTO(
                request.getStartAddress(),
                request.getDestinationAddress(),
                minutes
        );
    }
    private String normalize(String address) {
        return java.text.Normalizer.normalize(address, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim();
    }



    // -------------------------
    // GEOCODING (Nominatim)
    // -------------------------
    private Point geocode(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is required");
        }

        // 1) probaj kako je uneto
        Point p = tryGeocode(address.trim());
        if (p != null) return p;

        // 2) fallback: dodaj grad i drzavu (za kratke adrese tipa "Futoška 11")
        p = tryGeocode(address.trim() + ", Novi Sad, Serbia");
        if (p != null) return p;

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address not found: " + address);
    }

    private Point tryGeocode(String query) {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);

        String url = "https://nominatim.openstreetmap.org/search"
                + "?q=" + q
                + "&format=jsonv2"
                + "&limit=1"
                + "&addressdetails=0";

        try {
            List<?> res = restClient.get()
                    .uri(url)
                    .header("User-Agent", "NuvolaApp/1.0 (local dev)")
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(List.class);

            if (res == null || res.isEmpty()) return null;

            Map<?, ?> item = (Map<?, ?>) res.get(0);
            double lat = Double.parseDouble(item.get("lat").toString());
            double lon = Double.parseDouble(item.get("lon").toString());
            return new Point(lat, lon);

        } catch (Exception e) {
            e.printStackTrace(); // ili logger
            return null;
        }

    }


    private record Point(double lat, double lon) {}
}
