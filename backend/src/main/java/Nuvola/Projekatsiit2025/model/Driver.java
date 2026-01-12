package Nuvola.Projekatsiit2025.model;

import Nuvola.Projekatsiit2025.model.enums.DriverStatus;

public class Driver extends User {
    private DriverStatus status;
    private String loginTime;
    private Vehicle vehicle;

    public Driver() {
        super();
    }

    public Driver(Long id, String email, String password, String firstName,
                  String lastName, String address, String phone, String picture,
                  DriverStatus status, String loginTime, Vehicle vehicle) {

        super(id, email, password, firstName, lastName, address, phone, picture);
        this.status = status;
        this.loginTime = loginTime;
        this.vehicle = vehicle;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
