package Nuvola.Projekatsiit2025.dto;

import Nuvola.Projekatsiit2025.model.enums.VehicleType;

public class ProfileChangeRequestDTO {
    private Long id;
    private String driverName;
    private String driverEmail;

    // CURRENT VALUES (from driver)
    private String currentFirstName;
    private String currentLastName;
    private String currentPhone;
    private String currentAddress;
    private String currentModel;
    private String currentType;
    private Integer currentNumOfSeats;
    private Boolean currentBabyFriendly;
    private Boolean currentPetFriendly;

    // REQUESTED VALUES (from change request)
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String model;
    private String type;
    private Integer numOfSeats;
    private Boolean babyFriendly;
    private Boolean petFriendly;

    private String status;
    private String createdAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getCurrentFirstName() {
        return currentFirstName;
    }

    public void setCurrentFirstName(String currentFirstName) {
        this.currentFirstName = currentFirstName;
    }

    public String getCurrentLastName() {
        return currentLastName;
    }

    public void setCurrentLastName(String currentLastName) {
        this.currentLastName = currentLastName;
    }

    public String getCurrentPhone() {
        return currentPhone;
    }

    public void setCurrentPhone(String currentPhone) {
        this.currentPhone = currentPhone;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(String currentModel) {
        this.currentModel = currentModel;
    }

    public String getCurrentType() {
        return currentType;
    }

    public void setCurrentType(String currentType) {
        this.currentType = currentType;
    }

    public Integer getCurrentNumOfSeats() {
        return currentNumOfSeats;
    }

    public void setCurrentNumOfSeats(Integer currentNumOfSeats) {
        this.currentNumOfSeats = currentNumOfSeats;
    }

    public Boolean getCurrentBabyFriendly() {
        return currentBabyFriendly;
    }

    public void setCurrentBabyFriendly(Boolean currentBabyFriendly) {
        this.currentBabyFriendly = currentBabyFriendly;
    }

    public Boolean getCurrentPetFriendly() {
        return currentPetFriendly;
    }

    public void setCurrentPetFriendly(Boolean currentPetFriendly) {
        this.currentPetFriendly = currentPetFriendly;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getNumOfSeats() {
        return numOfSeats;
    }

    public void setNumOfSeats(Integer numOfSeats) {
        this.numOfSeats = numOfSeats;
    }

    public Boolean getBabyFriendly() {
        return babyFriendly;
    }

    public void setBabyFriendly(Boolean babyFriendly) {
        this.babyFriendly = babyFriendly;
    }

    public Boolean getPetFriendly() {
        return petFriendly;
    }

    public void setPetFriendly(Boolean petFriendly) {
        this.petFriendly = petFriendly;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}