package com.example.nuvola.network;

import com.google.gson.annotations.SerializedName;

public class UserTokenState {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresIn")
    private int expiresIn;

    public String getAccessToken() { return accessToken; }
    public int getExpiresIn() { return expiresIn; }
}
