package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.enums.RideStatus;
import java.util.List;

public class AdminRideHistoryItemDTO {
	private Long id;
	private String startLocation;
	private String destination;
	private String startTime;
	private String endTime;
	private String creationDate;
	private boolean canceled;
	private String canceledBy;
	private double price;
	private boolean panic;
	private RideStatus status;

	// Constructors
	public AdminRideHistoryItemDTO() {}

	public AdminRideHistoryItemDTO(Long id, String startLocation, String destination,
								   String startTime, String endTime, String creationDate,
								   boolean canceled, String canceledBy, double price,
								   boolean panic, RideStatus status) {
		this.id = id;
		this.startLocation = startLocation;
		this.destination = destination;
		this.startTime = startTime;
		this.endTime = endTime;
		this.creationDate = creationDate;
		this.canceled = canceled;
		this.canceledBy = canceledBy;
		this.price = price;
		this.panic = panic;
		this.status = status;
	}

	// Getters & Setters
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getStartLocation() { return startLocation; }
	public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

	public String getDestination() { return destination; }
	public void setDestination(String destination) { this.destination = destination; }

	public String getStartTime() { return startTime; }
	public void setStartTime(String startTime) { this.startTime = startTime; }

	public String getEndTime() { return endTime; }
	public void setEndTime(String endTime) { this.endTime = endTime; }

	public String getCreationDate() { return creationDate; }
	public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

	public boolean isCanceled() { return canceled; }
	public void setCanceled(boolean canceled) { this.canceled = canceled; }

	public String getCanceledBy() { return canceledBy; }
	public void setCanceledBy(String canceledBy) { this.canceledBy = canceledBy; }

	public double getPrice() { return price; }
	public void setPrice(double price) { this.price = price; }

	public boolean isPanic() { return panic; }
	public void setPanic(boolean panic) { this.panic = panic; }

	public RideStatus getStatus() { return status; }
	public void setStatus(RideStatus status) { this.status = status; }
}
