package dto;

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
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getPicture() { return picture; }
    public String getMessage() { return message; }
}
