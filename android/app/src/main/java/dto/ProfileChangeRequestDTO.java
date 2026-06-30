package dto;

public class ProfileChangeRequestDTO {

    public Long id;

    public String driverName;
    public String driverEmail;


    public String currentFirstName;
    public String currentLastName;
    public String currentPhone;
    public String currentAddress;

    public String currentModel;
    public String currentType;

    public Integer currentNumOfSeats;

    public Boolean currentBabyFriendly;
    public Boolean currentPetFriendly;


    public String firstName;
    public String lastName;
    public String phone;
    public String address;

    public String model;
    public String type;

    public Integer numOfSeats;

    public Boolean babyFriendly;
    public Boolean petFriendly;

    public String status;
    public String createdAt;

    public ProfileChangeRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public String getCurrentFirstName() {
        return currentFirstName;
    }

    public String getCurrentLastName() {
        return currentLastName;
    }

    public String getCurrentPhone() {
        return currentPhone;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public String getCurrentType() {
        return currentType;
    }

    public Integer getCurrentNumOfSeats() {
        return currentNumOfSeats;
    }

    public Boolean getCurrentBabyFriendly() {
        return currentBabyFriendly;
    }

    public Boolean getCurrentPetFriendly() {
        return currentPetFriendly;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public Integer getNumOfSeats() {
        return numOfSeats;
    }

    public Boolean getBabyFriendly() {
        return babyFriendly;
    }

    public Boolean getPetFriendly() {
        return petFriendly;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean hasFirstNameChanged() {
        return different(
                currentFirstName,
                firstName
        );
    }

    public boolean hasLastNameChanged() {
        return different(
                currentLastName,
                lastName
        );
    }

    public boolean hasPhoneChanged() {
        return different(
                currentPhone,
                phone
        );
    }

    public boolean hasAddressChanged() {
        return different(
                currentAddress,
                address
        );
    }

    public boolean hasModelChanged() {
        return different(
                currentModel,
                model
        );
    }

    public boolean hasTypeChanged() {
        return different(
                currentType,
                type
        );
    }

    public boolean hasNumberOfSeatsChanged() {
        return different(
                currentNumOfSeats,
                numOfSeats
        );
    }

    public boolean hasBabyFriendlyChanged() {
        return different(
                currentBabyFriendly,
                babyFriendly
        );
    }

    public boolean hasPetFriendlyChanged() {
        return different(
                currentPetFriendly,
                petFriendly
        );
    }

    private boolean different(
            Object currentValue,
            Object requestedValue
    ) {
        if (currentValue == null
                && requestedValue == null) {

            return false;
        }

        if (currentValue == null
                || requestedValue == null) {

            return true;
        }

        return !currentValue.equals(
                requestedValue
        );
    }
}