package Nuvola.Projekatsiit2025.dto;

public class RegisterResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String picture;
    private String message;

    public RegisterResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
