package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.DriverStatus;

public class ChangeDriverStatusDTO {
	private DriverStatus status;
	
	public DriverStatus getStatus() {
		return status;
	}

	public void setStatus(DriverStatus status) {
		this.status = status;
	}

	public ChangeDriverStatusDTO() {}
	
}