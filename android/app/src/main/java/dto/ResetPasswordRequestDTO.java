package dto;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequestDTO {

    @SerializedName("newPassword")
    private String newPassword;

    @SerializedName("confirmNewPassword")
    private String confirmNewPassword;

    public ResetPasswordRequestDTO(String newPassword, String confirmNewPassword) {
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

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
}