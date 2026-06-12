package dto;

import com.google.gson.annotations.SerializedName;

public class ChangeDriverStatusDTO {
    @SerializedName("status")
    private String status;

    public ChangeDriverStatusDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}