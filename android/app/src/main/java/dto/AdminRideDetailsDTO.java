package dto;

import java.util.List;

public class AdminRideDetailsDTO {
    public long id;
    public String startLocation;
    public String destination;
    public String startTime;
    public String endTime;
    public String creationDate;
    public double price;
    public boolean panic;
    public List<String> routeCoordinates;
    public String driverName;
    public List<String> passengerNames;
    public List<String> inconsistencyReports;
    public Double driverRating;
    public Double passengersRating;
    public boolean canReorderNow;
    public boolean canReorderLater;
}