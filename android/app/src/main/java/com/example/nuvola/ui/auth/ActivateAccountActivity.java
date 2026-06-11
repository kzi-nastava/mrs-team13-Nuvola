package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivateAccountActivity extends AppCompatActivity {

    private String activationToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient.init(this);

        activationToken = extractTokenFromIntent(getIntent());

        if (activationToken == null || activationToken.trim().isEmpty()) {
            Toast.makeText(this, "Missing activation token.", Toast.LENGTH_LONG).show();
            goToLogin();
            return;
        }

        AuthService.api()
                .activateAccount(activationToken)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ActivateAccountActivity.this,
                                    "Account activated. You can now log in.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ActivateAccountActivity.this,
                                    "Activation failed: " + response.code(),
                                    Toast.LENGTH_LONG).show();
                        }

                        goToLogin();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(ActivateAccountActivity.this,
                                "Network error: " + (t.getMessage() == null ? "unknown" : t.getMessage()),
                                Toast.LENGTH_LONG).show();

                        goToLogin();
                    }
                });
    }

    private String extractTokenFromIntent(Intent intent) {
        if (intent == null) return null;

        Uri data = intent.getData();
        if (data != null) {
            String token = data.getQueryParameter("token");
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        }

        return null;
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}