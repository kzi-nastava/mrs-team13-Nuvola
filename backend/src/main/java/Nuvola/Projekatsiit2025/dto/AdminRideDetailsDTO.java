package Nuvola.Projekatsiit2025.dto;

import java.util.List;

public class AdminRideDetailsDTO {
    private Long id;

    private String startLocation;
    private String destination;
    private String startTime;
    private String endTime;
    private String creationDate;

    private double price;
    private boolean panic;

    // map
    private List<String> routeCoordinates;

    // driver/passengers 
    private String driverName;
    private List<String> passengerNames;

    // added
    private List<String> inconsistencyReports;
    
    private Double driverRating;
    private Double passengersRating;

    // reorder (stub)
    private boolean canReorderNow;
    private boolean canReorderLater;

    public AdminRideDetailsDTO() {}
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean isPanic() {
		return panic;
	}

	public void setPanic(boolean panic) {
		this.panic = panic;
	}

	public List<String> getRouteCoordinates() {
		return routeCoordinates;
	}

	public void setRouteCoordinates(List<String> routeCoordinates) {
		this.routeCoordinates = routeCoordinates;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public List<String> getPassengerNames() {
		return passengerNames;
	}

	public void setPassengerNames(List<String> passengerNames) {
		this.passengerNames = passengerNames;
	}

	public List<String> getInconsistencyReports() {
		return inconsistencyReports;
	}

	public void setInconsistencyReports(List<String> inconsistencyReports) {
		this.inconsistencyReports = inconsistencyReports;
	}

	public Double getDriverRating() {
		return driverRating;
	}

	public void setDriverRating(Double driverRating) {
		this.driverRating = driverRating;
	}

	public Double getPassengersRating() {
		return passengersRating;
	}

	public void setPassengersRating(Double passengersRating) {
		this.passengersRating = passengersRating;
	}

	public boolean isCanReorderNow() {
		return canReorderNow;
	}

	public void setCanReorderNow(boolean canReorderNow) {
		this.canReorderNow = canReorderNow;
	}

	public boolean isCanReorderLater() {
		return canReorderLater;
	}

	public void setCanReorderLater(boolean canReorderLater) {
		this.canReorderLater = canReorderLater;
	}
    
}
