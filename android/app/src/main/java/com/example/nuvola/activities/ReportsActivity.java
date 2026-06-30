package com.example.nuvola.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ReportApi;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.views.ReportChartView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dto.RideReportDayDTO;
import dto.RideReportResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private static final String TARGET_ALL_DRIVERS =
            "ALL_DRIVERS";

    private static final String TARGET_ALL_CUSTOMERS =
            "ALL_CUSTOMERS";

    private static final String TARGET_ONE_DRIVER =
            "ONE_DRIVER";

    private static final String TARGET_ONE_CUSTOMER =
            "ONE_CUSTOMER";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextInputLayout startDateLayout;
    private TextInputLayout endDateLayout;
    private TextInputLayout adminTargetLayout;
    private TextInputLayout targetEmailLayout;

    private TextInputEditText startDateInput;
    private TextInputEditText endDateInput;
    private TextInputEditText targetEmailInput;

    private AutoCompleteTextView adminTargetInput;

    private LinearLayout adminFiltersContainer;
    private LinearLayout reportResultsContainer;

    private MaterialButton generateButton;
    private ProgressBar progressBar;
    private TextView errorText;

    private TextView totalRidesText;
    private TextView totalKmText;
    private TextView totalMoneyText;

    private TextView averageRidesText;
    private TextView averageKmText;
    private TextView averageMoneyText;

    private ReportChartView ridesChart;
    private ReportChartView kmChart;
    private ReportChartView moneyChart;

    private ReportApi reportApi;

    private Calendar selectedStartDate;
    private Calendar selectedEndDate;

    private boolean admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        reportApi = ApiClient
                .getRetrofit()
                .create(ReportApi.class);

        bindViews();
        setupDrawer();
        setupRoleSpecificUi();
        setupDateDefaults();
        setupListeners();
    }

    private void bindViews() {
        drawerLayout =
                findViewById(R.id.drawerLayout);

        navigationView =
                findViewById(R.id.navView);

        startDateLayout =
                findViewById(R.id.layoutStartDate);

        endDateLayout =
                findViewById(R.id.layoutEndDate);

        adminTargetLayout =
                findViewById(R.id.layoutAdminTarget);

        targetEmailLayout =
                findViewById(R.id.layoutTargetEmail);

        startDateInput =
                findViewById(R.id.etStartDate);

        endDateInput =
                findViewById(R.id.etEndDate);

        targetEmailInput =
                findViewById(R.id.etTargetEmail);

        adminTargetInput =
                findViewById(R.id.actAdminTarget);

        adminFiltersContainer =
                findViewById(
                        R.id.adminFiltersContainer
                );

        reportResultsContainer =
                findViewById(
                        R.id.reportResultsContainer
                );

        generateButton =
                findViewById(R.id.btnGenerateReport);

        progressBar =
                findViewById(R.id.progressReports);

        errorText =
                findViewById(R.id.tvReportsError);

        totalRidesText =
                findViewById(R.id.tvTotalRides);

        totalKmText =
                findViewById(R.id.tvTotalKm);

        totalMoneyText =
                findViewById(R.id.tvTotalMoney);

        averageRidesText =
                findViewById(R.id.tvAverageRides);

        averageKmText =
                findViewById(R.id.tvAverageKm);

        averageMoneyText =
                findViewById(R.id.tvAverageMoney);

        ridesChart =
                findViewById(R.id.chartRides);

        kmChart =
                findViewById(R.id.chartKm);

        moneyChart =
                findViewById(R.id.chartMoney);
    }

    private void setupDrawer() {
        View menuButton =
                findViewById(R.id.ivMenu);

        if (menuButton != null) {
            menuButton.setOnClickListener(
                    view -> drawerLayout.openDrawer(
                            GravityCompat.START
                    )
            );
        }

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void setupRoleSpecificUi() {
        String role =
                TokenStorage.getUserRole(this);

        admin =
                "ADMIN".equalsIgnoreCase(role);

        adminFiltersContainer.setVisibility(
                admin
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!admin) {
            return;
        }

        String[] visibleOptions = {
                "All drivers",
                "All customers",
                "One driver",
                "One customer"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        visibleOptions
                );

        adminTargetInput.setAdapter(adapter);

        adminTargetInput.setText(
                visibleOptions[0],
                false
        );

        adminTargetInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    boolean oneUser =
                            position == 2
                                    || position == 3;

                    targetEmailLayout.setVisibility(
                            oneUser
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    if (!oneUser) {
                        targetEmailInput.setText("");
                        targetEmailLayout.setError(null);
                    }
                }
        );
    }

    private void setupDateDefaults() {
        selectedEndDate =
                Calendar.getInstance();

        selectedStartDate =
                Calendar.getInstance();

        selectedStartDate.add(
                Calendar.DAY_OF_MONTH,
                -30
        );

        updateDateInputs();
    }

    private void setupListeners() {
        startDateInput.setOnClickListener(
                view -> showDatePicker(true)
        );

        endDateInput.setOnClickListener(
                view -> showDatePicker(false)
        );

        generateButton.setOnClickListener(
                view -> generateReport()
        );
    }

    private void showDatePicker(
            boolean startDate
    ) {
        Calendar current =
                startDate
                        ? selectedStartDate
                        : selectedEndDate;

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (picker, year, month, day) -> {
                            Calendar selected =
                                    Calendar.getInstance();

                            selected.set(
                                    year,
                                    month,
                                    day,
                                    0,
                                    0,
                                    0
                            );

                            selected.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (startDate) {
                                selectedStartDate = selected;

                            } else {
                                selectedEndDate = selected;
                            }

                            updateDateInputs();
                            clearErrors();
                        },
                        current.get(Calendar.YEAR),
                        current.get(Calendar.MONTH),
                        current.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }

    private void updateDateInputs() {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                );

        startDateInput.setText(
                formatter.format(
                        selectedStartDate.getTime()
                )
        );

        endDateInput.setText(
                formatter.format(
                        selectedEndDate.getTime()
                )
        );
    }

    private void generateReport() {
        clearErrors();
        hideError();

        if (!validateFilters()) {
            return;
        }

        setLoading(true);

        String startDate =
                textOf(startDateInput);

        String endDate =
                textOf(endDateInput);

        Call<RideReportResponse> call;

        if (admin) {
            String target =
                    getSelectedAdminTarget();

            String email =
                    requiresEmail(target)
                            ? textOf(targetEmailInput)
                            : null;

            call = reportApi.getAdminReport(
                    startDate,
                    endDate,
                    target,
                    email
            );

        } else {
            call = reportApi.getMyReport(
                    startDate,
                    endDate
            );
        }

        call.enqueue(
                new Callback<RideReportResponse>() {

                    @Override
                    public void onResponse(
                            Call<RideReportResponse> call,
                            Response<RideReportResponse> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showReportError(
                                    response.code()
                            );

                            return;
                        }

                        displayReport(
                                response.body()
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<RideReportResponse> call,
                            Throwable throwable
                    ) {
                        setLoading(false);

                        String message =
                                throwable == null
                                        || throwable.getMessage() == null
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

    private boolean validateFilters() {
        boolean valid = true;

        if (selectedStartDate == null) {
            startDateLayout.setError(
                    "Select start date"
            );

            valid = false;
        }

        if (selectedEndDate == null) {
            endDateLayout.setError(
                    "Select end date"
            );

            valid = false;
        }

        if (selectedStartDate != null
                && selectedEndDate != null
                && selectedStartDate.after(
                selectedEndDate
        )) {
            endDateLayout.setError(
                    "End date must be after start date"
            );

            valid = false;
        }

        if (admin) {
            String target =
                    getSelectedAdminTarget();

            if (requiresEmail(target)) {
                String email =
                        textOf(targetEmailInput);

                if (email.isEmpty()) {
                    targetEmailLayout.setError(
                            "Email is required"
                    );

                    valid = false;

                } else if (!Patterns.EMAIL_ADDRESS
                        .matcher(email)
                        .matches()) {

                    targetEmailLayout.setError(
                            "Enter a valid email"
                    );

                    valid = false;
                }
            }
        }

        return valid;
    }

    private String getSelectedAdminTarget() {
        String selected =
                adminTargetInput
                        .getText()
                        .toString()
                        .trim();

        switch (selected) {
            case "All customers":
                return TARGET_ALL_CUSTOMERS;

            case "One driver":
                return TARGET_ONE_DRIVER;

            case "One customer":
                return TARGET_ONE_CUSTOMER;

            case "All drivers":
            default:
                return TARGET_ALL_DRIVERS;
        }
    }

    private boolean requiresEmail(
            String target
    ) {
        return TARGET_ONE_DRIVER.equals(target)
                || TARGET_ONE_CUSTOMER.equals(target);
    }

    private void displayReport(
            RideReportResponse report
    ) {
        reportResultsContainer.setVisibility(
                View.VISIBLE
        );

        totalRidesText.setText(
                String.valueOf(
                        report.getTotalRides()
                )
        );

        totalKmText.setText(
                String.format(
                        Locale.US,
                        "%.2f km",
                        report.getTotalKm()
                )
        );

        totalMoneyText.setText(
                String.format(
                        Locale.US,
                        "%.2f RSD",
                        report.getTotalMoney()
                )
        );

        averageRidesText.setText(
                String.format(
                        Locale.US,
                        "Average rides: %.2f",
                        report.getAvgRides()
                )
        );

        averageKmText.setText(
                String.format(
                        Locale.US,
                        "Average distance: %.2f km",
                        report.getAvgKm()
                )
        );

        averageMoneyText.setText(
                String.format(
                        Locale.US,
                        "Average money: %.2f RSD",
                        report.getAvgMoney()
                )
        );

        List<String> labels =
                new ArrayList<>();

        List<Double> rideValues =
                new ArrayList<>();

        List<Double> kmValues =
                new ArrayList<>();

        List<Double> moneyValues =
                new ArrayList<>();

        List<RideReportDayDTO> data =
                report.getData();

        if (data != null) {
            for (RideReportDayDTO day : data) {
                labels.add(
                        day.getDate()
                );

                rideValues.add(
                        (double) day.getRideCount()
                );

                kmValues.add(
                        day.getTotalKm()
                );

                moneyValues.add(
                        day.getTotalMoney()
                );
            }
        }

        ridesChart.setBarColor(
                Color.rgb(
                        33,
                        76,
                        120
                )
        );

        kmChart.setBarColor(
                Color.rgb(
                        34,
                        139,
                        94
                )
        );

        moneyChart.setBarColor(
                Color.rgb(
                        217,
                        119,
                        6
                )
        );

        ridesChart.setData(
                "Rides per day",
                labels,
                rideValues
        );

        kmChart.setData(
                "Kilometres per day",
                labels,
                kmValues
        );

        moneyChart.setData(
                "Money per day",
                labels,
                moneyValues
        );
    }

    private void showReportError(
            int code
    ) {
        switch (code) {
            case 400:
                showError(
                        "Invalid date range or report target."
                );
                break;

            case 403:
                showError(
                        "You are not allowed to view this report."
                );
                break;

            case 404:
                showError(
                        "The selected user was not found."
                );
                break;

            default:
                showError(
                        "Report could not be generated. "
                                + "Error code: "
                                + code
                );
                break;
        }
    }

    private void clearErrors() {
        startDateLayout.setError(null);
        endDateLayout.setError(null);
        adminTargetLayout.setError(null);
        targetEmailLayout.setError(null);
    }

    private void setLoading(
            boolean loading
    ) {
        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        generateButton.setEnabled(
                !loading
        );
    }

    private void showError(
            String message
    ) {
        errorText.setText(message);

        errorText.setVisibility(
                View.VISIBLE
        );

        reportResultsContainer.setVisibility(
                View.GONE
        );
    }

    private void hideError() {
        errorText.setVisibility(
                View.GONE
        );
    }

    private String textOf(
            TextInputEditText input
    ) {
        if (input == null
                || input.getText() == null) {

            return "";
        }

        return input.getText()
                .toString()
                .trim();
    }
}