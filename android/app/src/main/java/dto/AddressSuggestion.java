package dto;

public class AddressSuggestion {

    private final String address;
    private final double latitude;
    private final double longitude;

    public AddressSuggestion(
            String address,
            double latitude,
            double longitude
    ) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public CoordinateDTO toCoordinateDTO() {
        return new CoordinateDTO(
                latitude,
                longitude,
                address
        );
    }

    @Override
    public String toString() {
        return address;
    }
}