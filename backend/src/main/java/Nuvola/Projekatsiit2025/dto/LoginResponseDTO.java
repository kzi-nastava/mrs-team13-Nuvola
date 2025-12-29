package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.DriverStatus;

public class LoginResponseDTO {
	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	
	private String userType; 
	private String token;
	private DriverStatus driverStatus;
	private String message;
	
	public LoginResponseDTO() {}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DriverStatus getDriverStatus() {
		return driverStatus;
	}

	public void setDriverStatus(DriverStatus driverStatus) {
		this.driverStatus = driverStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}	
	
}