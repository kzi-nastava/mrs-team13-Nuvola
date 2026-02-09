package dto;

public class UpdateProfileDTO {
    public String firstName;
    public String lastName;
    public String phone;
    public String address;

    public UpdateProfileDTO(String firstName, String lastName,
                            String phone, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
    }
}