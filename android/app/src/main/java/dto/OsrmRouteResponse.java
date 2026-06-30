package dto;

import java.util.List;

public class OsrmRouteResponse {

    public String code;
    public List<OsrmRouteDTO> routes;

    public String getCode() {
        return code;
    }

    public List<OsrmRouteDTO> getRoutes() {
        return routes;
    }
}