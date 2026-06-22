package dto;

import com.google.gson.annotations.SerializedName;

public class AdminUserDTO {

    @SerializedName("id")
    public long id;

    @SerializedName("firstName")
    public String firstName;

    @SerializedName("lastName")
    public String lastName;

    @SerializedName("email")
    public String email;

    @SerializedName("address")
    public String address;

    @SerializedName("phone")
    public String phone;

    @SerializedName("picture")
    public String picture;

    @SerializedName("blocked")
    public boolean blocked;

    @SerializedName("blockingReason")
    public String blockingReason;
}