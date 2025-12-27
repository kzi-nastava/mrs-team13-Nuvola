package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.RideStatus;

public class CreatedRideDTO {
    private Long id;
    private RideStatus status;
    private Double price;
    private String message;

    public CreatedRideDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
