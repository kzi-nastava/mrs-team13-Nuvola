package dto;

public class OsrmRouteDTO {

    public double distance;
    public double duration;
    public OsrmGeometryDTO geometry;

    public double getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public OsrmGeometryDTO getGeometry() {
        return geometry;
    }
}