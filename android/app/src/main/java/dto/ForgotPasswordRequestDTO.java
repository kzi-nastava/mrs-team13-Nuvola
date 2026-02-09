package dto;

public class ForgotPasswordRequestDTO {
    private String email;

    public ForgotPasswordRequestDTO(String email) { this.email = email; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
