package com.example.nuvola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ReviewApi;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;

import dto.RatingRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeRideActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_ID =
            "grade_ride_id";

    private Spinner spinnerVehicleRating;
    private Spinner spinnerDriverRating;

    private EditText etComment;

    private TextView tvError;
    private TextView tvSuccess;

    private MaterialButton btnSubmit;

    private long rideId;
    private String username;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_grade_ride
        );

        rideId =
                getIntent().getLongExtra(
                        EXTRA_RIDE_ID,
                        -1
                );

        if (rideId < 0) {
            Toast.makeText(
                    this,
                    "Invalid ride ID.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        username = TokenStorage.getUserEmail(this);

        bindViews();
        setupSpinners();
        setupListeners();
    }

    private void bindViews() {
        spinnerVehicleRating =
                findViewById(
                        R.id.spinnerVehicleRating
                );

        spinnerDriverRating =
                findViewById(
                        R.id.spinnerDriverRating
                );

        etComment =
                findViewById(
                        R.id.etComment
                );

        tvError =
                findViewById(
                        R.id.tvError
                );

        tvSuccess =
                findViewById(
                        R.id.tvSuccess
                );

        btnSubmit =
                findViewById(
                        R.id.btnSubmit
                );
    }

    private void setupListeners() {
        View backButton =
                findViewById(
                        R.id.btnBack
                );

        if (backButton != null) {
            backButton.setOnClickListener(
                    view -> finish()
            );
        }

        btnSubmit.setOnClickListener(
                view -> submit()
        );
    }

    private void setupSpinners() {
        String[] ratings = {
                "1",
                "2",
                "3",
                "4",
                "5"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_spinner_item,
                        ratings
                );

        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );

        spinnerVehicleRating.setAdapter(
                adapter
        );

        spinnerDriverRating.setAdapter(
                adapter
        );

        /*
         * Podrazumevano postavljamo ocenu 5.
         */
        spinnerVehicleRating.setSelection(4);
        spinnerDriverRating.setSelection(4);
    }

    private void submit() {
        hideMessages();

        if (username == null
                || username.trim().isEmpty()) {

            showError(
                    "You must be logged in to submit a rating."
            );

            return;
        }

        if (spinnerVehicleRating
                .getSelectedItemPosition() < 0
                || spinnerDriverRating
                .getSelectedItemPosition() < 0) {

            showError(
                    "Select both ratings."
            );

            return;
        }

        int vehicleRating =
                spinnerVehicleRating
                        .getSelectedItemPosition()
                        + 1;

        int driverRating =
                spinnerDriverRating
                        .getSelectedItemPosition()
                        + 1;

        String comment =
                etComment.getText()
                        .toString()
                        .trim();

        RatingRequestDTO request =
                new RatingRequestDTO(
                        vehicleRating,
                        driverRating,
                        comment,
                        rideId,
                        username.trim()
                );

        setLoading(true);

        ReviewApi api =
                ApiClient.getRetrofit()
                        .create(
                                ReviewApi.class
                        );

        api.submitReview(request)
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Response<Void> response
                            ) {
                                setLoading(false);

                                if (response.isSuccessful()) {
                                    showSuccess(
                                            "Rating submitted successfully."
                                    );

                                    etComment.setText("");

                                } else {
                                    showReviewError(
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);

                                String message =
                                        throwable == null
                                                || throwable
                                                .getMessage() == null
                                                || throwable
                                                .getMessage()
                                                .trim()
                                                .isEmpty()
                                                ? "Unknown network error"
                                                : throwable.getMessage();

                                showError(
                                        "Network error: "
                                                + message
                                );
                            }
                        }
                );
    }

    private void showReviewError(
            int statusCode
    ) {
        String message;

        switch (statusCode) {
            case 400:
                message =
                        "Entered rating information is invalid.";
                break;

            case 401:
                message =
                        "You do not have permission to send a review.";
                break;

            case 403:
                message =
                        "You cannot rate this ride. "
                                + "The rating period may have expired.";
                break;

            case 404:
                message =
                        "The selected ride was not found.";
                break;

            case 409:
                message =
                        "You have already submitted a review for this ride.";
                break;

            default:
                message =
                        "Error sending review ("
                                + statusCode
                                + "). Please try again.";
                break;
        }

        showError(message);
    }

    private void setLoading(
            boolean loading
    ) {
        btnSubmit.setEnabled(
                !loading
        );

        btnSubmit.setText(
                loading
                        ? "Sending..."
                        : "Submit Rating"
        );
    }

    private void hideMessages() {
        tvError.setVisibility(
                View.GONE
        );

        tvSuccess.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {
        tvError.setText(message);

        tvError.setVisibility(
                View.VISIBLE
        );

        tvSuccess.setVisibility(
                View.GONE
        );
    }

    private void showSuccess(
            String message
    ) {
        tvSuccess.setText(message);

        tvSuccess.setVisibility(
                View.VISIBLE
        );

        tvError.setVisibility(
                View.GONE
        );
    }
}