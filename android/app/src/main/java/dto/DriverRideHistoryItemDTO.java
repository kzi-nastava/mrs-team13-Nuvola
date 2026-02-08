package dto;

public class DriverRideHistoryItemDTO {
    public long id;
    public double price;
    public LocationDTO pickup;
    public LocationDTO dropoff;
    public String startingTime;
    public String driver;
    public boolean favouriteRoute;
}
