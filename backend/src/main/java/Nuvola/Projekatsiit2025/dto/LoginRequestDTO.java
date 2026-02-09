package Nuvola.Projekatsiit2025.dto;

public class LoginRequestDTO {
	private String email;
	private String password;
	
	public LoginRequestDTO() {}
	
	public String getEmail() {return email; }
	public void setEmail(String email ) { this.email = email; }

    public String getUsername() {return email; }
    public void setUsername(String email) { this.email = email; }
	
	public String getPassword() { return password; }
	public void setPassword(String password) {this.password = password; }
}