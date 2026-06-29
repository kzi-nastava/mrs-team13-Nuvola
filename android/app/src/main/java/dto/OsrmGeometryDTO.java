package dto;

import java.util.List;

public class OsrmGeometryDTO {

    public String type;

    /*
     * GeoJSON koordinate dolaze ovim redosledom:
     * [longitude, latitude]
     */
    public List<List<Double>> coordinates;

    public String getType() {
        return type;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }
}