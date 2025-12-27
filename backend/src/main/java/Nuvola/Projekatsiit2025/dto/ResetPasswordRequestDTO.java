package Nuvola.Projekatsiit2025.dto;

public class ResetPasswordRequestDTO {
	private String newPassword;
	
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}

	private String confirmNewPassword;
	
	public ResetPasswordRequestDTO() {}
	
	
}