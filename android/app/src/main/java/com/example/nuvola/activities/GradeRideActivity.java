package com.example.nuvola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ReviewApi;
import com.google.android.material.button.MaterialButton;

import dto.RatingRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeRideActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_ID = "grade_ride_id";

    private Spinner spinnerVehicleRating, spinnerDriverRating;
    private EditText etComment;
    private TextView tvError, tvSuccess;
    private MaterialButton btnSubmit;

    private long rideId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_ride);

        rideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1);
        if (rideId == -1) {
            Toast.makeText(this, "Invalid ride ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        username = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getString("USERNAME", "");

        bindViews();
        setupSpinners();
    }

    private void bindViews() {
        spinnerVehicleRating = findViewById(R.id.spinnerVehicleRating);
        spinnerDriverRating = findViewById(R.id.spinnerDriverRating);
        etComment = findViewById(R.id.etComment);
        tvError = findViewById(R.id.tvError);
        tvSuccess = findViewById(R.id.tvSuccess);
        btnSubmit = findViewById(R.id.btnSubmit);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submit());
    }

    private void setupSpinners() {
        String[] ratings = {"1", "2", "3", "4", "5"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleRating.setAdapter(adapter);
        spinnerDriverRating.setAdapter(adapter);
    }

    private void submit() {
        tvError.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.GONE);

        if (username.isEmpty()) {
            showError("You must be logged in to submit a rating.");
            return;
        }

        int vehicleRating = spinnerVehicleRating.getSelectedItemPosition() + 1;
        int driverRating = spinnerDriverRating.getSelectedItemPosition() + 1;
        String comment = etComment.getText().toString().trim();

        RatingRequestDTO dto = new RatingRequestDTO(vehicleRating, driverRating, comment, rideId, username);

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Sending...");

        ReviewApi api = ApiClient.getRetrofit().create(ReviewApi.class);
        api.submitReview(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Rating");

                if (response.isSuccessful()) {
                    tvSuccess.setText("Rating submitted successfully.");
                    tvSuccess.setVisibility(View.VISIBLE);
                    etComment.setText("");
                } else {
                    String msg;
                    switch (response.code()) {
                        case 400: msg = "Bad request."; break;
                        case 401: msg = "You do not have permission to send a review."; break;
                        case 403: msg = "You can't rate this ride (time limit is 3 days)."; break;
                        case 409: msg = "You have already submitted a review for this ride."; break;
                        default:  msg = "Error sending review (" + response.code() + "). Please try again."; break;
                    }
                    showError(msg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Submit Rating");
                showError("Network error. Please try again.");
            }
        });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}