package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;

public class Ride {

    private Long id;
    private double price;
    private RideStatus status;
    private String startLocation;
    private String destination;
    private String startTime;
    private String endTime;
    private String creationDate;
    private boolean isPanic;

    public Ride() {
        this.isPanic = false; // default vrednost
    }

    public Ride(Long id, double price, RideStatus status, String startLocation,
                String destination, String startTime, String endTime,
                String creationDate, boolean isPanic) {
        this.id = id;
        this.price = price;
        this.status = status;
        this.startLocation = startLocation;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creationDate = creationDate;
        this.isPanic = isPanic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
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

    public boolean isPanic() {
        return isPanic;
    }

    public void setPanic(boolean panic) {
        isPanic = panic;
    }
}
