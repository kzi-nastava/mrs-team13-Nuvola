package dto;

public class ResetPasswordRequestDTO {
    private String password;

    public ResetPasswordRequestDTO(String password) { this.password = password; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
