package Nuvola.Projekatsiit2025.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public class UpdateProfileDTO {

    @NotNull(message = "First name cannot be null")
    @NotBlank(message = "First name cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "First name must start with a capital letter and contain only letters"
    )
    @Size(min = 3, max = 255)
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @NotBlank(message = "Last name cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "Last name must start with a capital letter and contain only letters"
    )
    @Size(min = 3, max = 255)
    private String lastName;

    @NotNull(message = "Phone number cannot be null")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9+\\s]+$",  message = "Phone must be of correct pattern")
    private String phone;

    @NotNull(message = "Address cannot be null")
    @NotBlank(message = "Address cannot be blank")
    @Pattern(
            regexp = "^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$",
            message = "Address must start with a capital letter and contain only letters, numbers and spaces"
    )
    @Size(min = 3, max = 255)
    private String address;


    public UpdateProfileDTO() {}

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


}
