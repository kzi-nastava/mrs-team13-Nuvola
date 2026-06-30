package com.example.nuvola.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.nuvola.navigation.NavigationMenuManager;

import com.example.nuvola.R;
import com.example.nuvola.adapters.AddressSuggestionAdapter;
import com.example.nuvola.adapters.ScheduledTimeAdapter;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.FavoriteRouteApi;
import com.example.nuvola.network.OsrmApi;
import com.example.nuvola.network.PricingApi;
import com.example.nuvola.network.ProfileApi;
import com.example.nuvola.network.RideApi;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import dto.ActiveRideResponse;
import dto.AddressSuggestion;
import dto.CoordinateDTO;
import dto.CreateRideDTO;
import dto.CreatedRideDTO;
import dto.FavoriteRouteDTO;
import dto.OsrmGeometryDTO;
import dto.OsrmRouteDTO;
import dto.OsrmRouteResponse;
import dto.ProfileResponseDTO;
import dto.VehicleTypePricingDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RideOrderActivity extends AppCompatActivity {

    private static final GeoPoint NOVI_SAD =
            new GeoPoint(45.2671, 19.8335);

    private static final double PRICE_PER_KILOMETRE =
            120.0;

    private static final long SUGGESTION_DELAY_MS =
            450L;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MapView mapView;
    private MapEventsOverlay mapEventsOverlay;
    private LinearLayout formContainer;
    private LinearLayout blockedOverlay;
    private LinearLayout stopsContainer;
    private LinearLayout passengersContainer;
    private LinearLayout estimateContainer;

    private TextInputLayout fromLayout;
    private TextInputLayout toLayout;
    private TextInputLayout stopLayout;
    private TextInputLayout passengerLayout;

    private MaterialAutoCompleteTextView fromInput;
    private MaterialAutoCompleteTextView toInput;
    private MaterialAutoCompleteTextView stopInput;
    private TextInputEditText passengerInput;
    private RadioGroup vehicleTypeGroup;
    private RadioGroup rideTimeGroup;
    private RadioButton standardRadioButton;
    private RadioButton nowRadioButton;
    private RadioButton scheduledRadioButton;
    private CheckBox babyTransportCheckBox;
    private CheckBox petTransportCheckBox;
    private MaterialButton favoriteRoutesButton;
    private MaterialButton addStopButton;
    private MaterialButton addPassengerButton;
    private MaterialButton calculateButton;
    private MaterialButton orderButton;
    private MaterialButton clearButton;
    private ProgressBar progressBar;
    private TextView distanceText;
    private TextView priceText;
    private TextView scheduleHint;
    private TextView scheduledLabel;
    private TextView blockedIcon;
    private TextView blockedTitle;
    private TextView blockedMessage;
    private Spinner scheduledTimeSpinner;
    private RideApi rideApi;
    private FavoriteRouteApi favoriteRouteApi;
    private ProfileApi profileApi;
    private PricingApi pricingApi;
    private OsrmApi osrmApi;
    private AddressSuggestionAdapter fromSuggestionAdapter;
    private AddressSuggestionAdapter toSuggestionAdapter;
    private AddressSuggestionAdapter stopSuggestionAdapter;
    private final Handler suggestionHandler =
            new Handler(Looper.getMainLooper());
    private Runnable fromSuggestionRunnable;
    private Runnable toSuggestionRunnable;
    private Runnable stopSuggestionRunnable;
    private final AtomicInteger fromSuggestionRequest =
            new AtomicInteger();
    private final AtomicInteger toSuggestionRequest =
            new AtomicInteger();
    private final AtomicInteger stopSuggestionRequest =
            new AtomicInteger();
    private final List<String> stopAddresses =
            new ArrayList<>();
    private final List<String> passengerEmails =
            new ArrayList<>();
    private final List<Calendar> scheduledTimeValues =
            new ArrayList<>();
    private final Map<String, Double> vehicleBasePrices =
            new HashMap<>();
    private final Map<String, CoordinateDTO> knownCoordinates =
            new HashMap<>();
    private CoordinateDTO selectedFromCoordinate;
    private CoordinateDTO selectedToCoordinate;
    private CoordinateDTO selectedStopCoordinate;
    private CoordinateDTO calculatedFrom;
    private CoordinateDTO calculatedTo;
    private final List<CoordinateDTO> calculatedStops =
            new ArrayList<>();
    private double calculatedDistanceKm;
    private Double calculatedPrice;
    private boolean orderingDisabled;
    private boolean pricesLoaded;
    private boolean updatingAddressProgrammatically;
    private int mapSelectionStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance()
                .setUserAgentValue(getPackageName());

        Configuration.getInstance()
                .setOsmdroidTileCache(getCacheDir());

        setContentView(R.layout.activity_ride_order);

        rideApi = ApiClient.getRetrofit()
                .create(RideApi.class);

        favoriteRouteApi = ApiClient.getRetrofit()
                .create(FavoriteRouteApi.class);

        profileApi = ApiClient.getRetrofit()
                .create(ProfileApi.class);

        pricingApi = ApiClient.getRetrofit()
                .create(PricingApi.class);

        Retrofit osrmRetrofit =
                new Retrofit.Builder()
                        .baseUrl("https://router.project-osrm.org/")
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        )
                        .build();

        osrmApi = osrmRetrofit.create(
                OsrmApi.class
        );

        bindViews();
        setupMap();
        setupAddressSuggestions();
        setupDrawer();
        setupListeners();
        setupScheduledTimeOptions();
        loadVehiclePrices();

        showFormTemporarilyDisabled();
        checkBlockedStatus();
    }

    private void bindViews() {
        drawerLayout =
                findViewById(R.id.drawerLayout);

        navigationView =
                findViewById(R.id.navView);

        mapView =
                findViewById(R.id.mapView);

        formContainer =
                findViewById(R.id.formContainer);

        blockedOverlay =
                findViewById(R.id.blockedOverlay);

        stopsContainer =
                findViewById(R.id.stopsContainer);

        passengersContainer =
                findViewById(R.id.passengersContainer);

        estimateContainer =
                findViewById(R.id.estimateContainer);

        fromLayout =
                findViewById(R.id.layoutFrom);

        toLayout =
                findViewById(R.id.layoutTo);

        stopLayout =
                findViewById(R.id.layoutStop);

        passengerLayout =
                findViewById(R.id.layoutPassenger);

        fromInput =
                findViewById(R.id.etFrom);

        toInput =
                findViewById(R.id.etTo);

        stopInput =
                findViewById(R.id.etStop);

        passengerInput =
                findViewById(R.id.etPassenger);

        vehicleTypeGroup =
                findViewById(R.id.vehicleTypeGroup);

        rideTimeGroup =
                findViewById(R.id.rideTimeGroup);

        standardRadioButton =
                findViewById(R.id.rbStandard);

        nowRadioButton =
                findViewById(R.id.rbNow);

        scheduledRadioButton =
                findViewById(R.id.rbScheduled);

        babyTransportCheckBox =
                findViewById(R.id.cbBabyTransport);

        petTransportCheckBox =
                findViewById(R.id.cbPetTransport);

        favoriteRoutesButton =
                findViewById(R.id.btnFavoriteRoutes);

        addStopButton =
                findViewById(R.id.btnAddStop);

        addPassengerButton =
                findViewById(R.id.btnAddPassenger);

        calculateButton =
                findViewById(R.id.btnCalculateRoute);

        orderButton =
                findViewById(R.id.btnOrderRide);

        clearButton =
                findViewById(R.id.btnClear);

        progressBar =
                findViewById(R.id.progressRideOrder);

        distanceText =
                findViewById(R.id.tvEstimatedDistance);

        priceText =
                findViewById(R.id.tvEstimatedPrice);

        scheduleHint =
                findViewById(R.id.tvScheduleHint);

        scheduledLabel =
                findViewById(R.id.tvScheduledLabel);

        scheduledTimeSpinner =
                findViewById(R.id.spinnerScheduledTime);

        blockedIcon =
                findViewById(R.id.tvBlockedIcon);

        blockedTitle =
                findViewById(R.id.tvBlockedTitle);

        blockedMessage =
                findViewById(R.id.tvBlockedMessage);
    }

    private void setupMap() {
        mapView.setTileSource(
                TileSourceFactory.MAPNIK
        );

        mapView.setMultiTouchControls(true);

        mapView.getController()
                .setZoom(13.0);

        mapView.getController()
                .setCenter(NOVI_SAD);

        MapEventsReceiver receiver =
                new MapEventsReceiver() {

                    @Override
                    public boolean singleTapConfirmedHelper(
                            GeoPoint point
                    ) {
                        reverseGeocodeMapPoint(point);
                        return true;
                    }

                    @Override
                    public boolean longPressHelper(
                            GeoPoint point
                    ) {
                        return false;
                    }
                };

        mapEventsOverlay =
                new MapEventsOverlay(receiver);

        mapView.getOverlays()
                .add(mapEventsOverlay);
    }

    private void setupAddressSuggestions() {
        fromSuggestionAdapter =
                new AddressSuggestionAdapter(this);

        toSuggestionAdapter =
                new AddressSuggestionAdapter(this);

        stopSuggestionAdapter =
                new AddressSuggestionAdapter(this);

        fromInput.setAdapter(
                fromSuggestionAdapter
        );

        toInput.setAdapter(
                toSuggestionAdapter
        );

        stopInput.setAdapter(
                stopSuggestionAdapter
        );

        configureSuggestionDropdown(fromInput);
        configureSuggestionDropdown(toInput);
        configureSuggestionDropdown(stopInput);

        fromInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    AddressSuggestion suggestion =
                            fromSuggestionAdapter
                                    .getSuggestion(position);

                    if (suggestion == null) {
                        return;
                    }

                    invalidateSuggestionRequest(
                            AddressField.FROM
                    );

                    fromSuggestionAdapter
                            .clearSuggestions();

                    fromInput.dismissDropDown();

                    selectedFromCoordinate =
                            suggestion.toCoordinateDTO();

                    rememberCoordinate(
                            selectedFromCoordinate
                    );

                    setAddressText(
                            fromInput,
                            suggestion.getAddress()
                    );

                    fromLayout.setError(null);

                    invalidateCalculatedRoute();
                    refreshSelectedMapMarkers();
                }
        );

        toInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    AddressSuggestion suggestion =
                            toSuggestionAdapter
                                    .getSuggestion(position);

                    if (suggestion == null) {
                        return;
                    }

                    invalidateSuggestionRequest(
                            AddressField.TO
                    );

                    toSuggestionAdapter
                            .clearSuggestions();

                    toInput.dismissDropDown();

                    selectedToCoordinate =
                            suggestion.toCoordinateDTO();

                    rememberCoordinate(
                            selectedToCoordinate
                    );

                    setAddressText(
                            toInput,
                            suggestion.getAddress()
                    );

                    toLayout.setError(null);

                    invalidateCalculatedRoute();
                    refreshSelectedMapMarkers();
                }
        );

        stopInput.setOnItemClickListener(
                (parent, view, position, id) -> {
                    AddressSuggestion suggestion =
                            stopSuggestionAdapter
                                    .getSuggestion(position);

                    if (suggestion == null) {
                        return;
                    }

                    invalidateSuggestionRequest(
                            AddressField.STOP
                    );

                    stopSuggestionAdapter
                            .clearSuggestions();

                    stopInput.dismissDropDown();

                    selectedStopCoordinate =
                            suggestion.toCoordinateDTO();

                    rememberCoordinate(
                            selectedStopCoordinate
                    );

                    setAddressText(
                            stopInput,
                            suggestion.getAddress()
                    );

                    stopLayout.setError(null);
                }
        );
    }

    private void configureSuggestionDropdown(
            MaterialAutoCompleteTextView input
    ) {
        input.setDropDownHeight(
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        input.setDropDownVerticalOffset(
                dpToPx(6)
        );
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

    private void setupListeners() {
        addStopButton.setOnClickListener(
                view -> addStop()
        );

        stopInput.setOnEditorActionListener(
                (view, actionId, event) -> {
                    addStop();
                    return true;
                }
        );

        addPassengerButton.setOnClickListener(
                view -> addPassenger()
        );

        passengerInput.setOnEditorActionListener(
                (view, actionId, event) -> {
                    addPassenger();
                    return true;
                }
        );

        rideTimeGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    boolean scheduled =
                            checkedId == R.id.rbScheduled;

                    scheduledLabel.setVisibility(
                            scheduled
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    scheduledTimeSpinner.setVisibility(
                            scheduled
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    scheduleHint.setVisibility(
                            scheduled
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    if (scheduled) {
                        setupScheduledTimeOptions();
                    }
                }
        );

        calculateButton.setOnClickListener(
                view -> calculateRoute()
        );

        orderButton.setOnClickListener(
                view -> orderRide()
        );

        clearButton.setOnClickListener(
                view -> clearForm()
        );

        favoriteRoutesButton.setOnClickListener(
                view -> loadFavoriteRoutes()
        );

        vehicleTypeGroup.setOnCheckedChangeListener(
                (group, checkedId) ->
                        invalidateCalculatedRoute()
        );

        fromInput.addTextChangedListener(
                createAddressWatcher(
                        fromInput,
                        fromSuggestionAdapter,
                        AddressField.FROM
                )
        );

        toInput.addTextChangedListener(
                createAddressWatcher(
                        toInput,
                        toSuggestionAdapter,
                        AddressField.TO
                )
        );

        stopInput.addTextChangedListener(
                createAddressWatcher(
                        stopInput,
                        stopSuggestionAdapter,
                        AddressField.STOP
                )
        );
    }

    private TextWatcher createAddressWatcher(
            MaterialAutoCompleteTextView input,
            AddressSuggestionAdapter adapter,
            AddressField field
    ) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
            }

            @Override
            public void afterTextChanged(
                    Editable editable
            ) {
                if (updatingAddressProgrammatically) {
                    return;
                }

                String query =
                        editable == null
                                ? ""
                                : editable.toString().trim();

                if (field == AddressField.FROM) {
                    selectedFromCoordinate = null;
                    invalidateCalculatedRoute();
                    refreshSelectedMapMarkers();

                } else if (field == AddressField.TO) {
                    selectedToCoordinate = null;
                    invalidateCalculatedRoute();
                    refreshSelectedMapMarkers();

                } else {
                    selectedStopCoordinate = null;
                }

                scheduleAddressSuggestions(
                        input,
                        adapter,
                        field,
                        query
                );
            }
        };
    }

    private void scheduleAddressSuggestions(
            MaterialAutoCompleteTextView input,
            AddressSuggestionAdapter adapter,
            AddressField field,
            String query
    ) {
        cancelSuggestionRunnable(field);

        int requestId =
                incrementSuggestionRequest(field);

        if (query.length() < 3) {
            adapter.clearSuggestions();
            input.dismissDropDown();
            return;
        }

        Runnable runnable =
                () -> loadAddressSuggestions(
                        input,
                        adapter,
                        field,
                        query,
                        requestId
                );

        setSuggestionRunnable(
                field,
                runnable
        );

        suggestionHandler.postDelayed(
                runnable,
                SUGGESTION_DELAY_MS
        );
    }

    private void loadAddressSuggestions(
            MaterialAutoCompleteTextView input,
            AddressSuggestionAdapter adapter,
            AddressField field,
            String originalQuery,
            int requestId
    ) {
        new Thread(() -> {
            List<AddressSuggestion> suggestions =
                    searchAddresses(originalQuery);

            runOnUiThread(() -> {
                if (requestId
                        != getSuggestionRequest(field)) {

                    return;
                }

                if (updatingAddressProgrammatically) {
                    return;
                }

                String currentText =
                        input.getText() == null
                                ? ""
                                : input.getText()
                                .toString()
                                .trim();

                if (!currentText.equals(originalQuery)
                        || currentText.length() < 3) {

                    return;
                }

                adapter.setSuggestions(suggestions);

                if (!suggestions.isEmpty()
                        && input.hasFocus()) {

                    input.post(() -> {
                        if (requestId
                                == getSuggestionRequest(field)
                                && input.hasFocus()) {

                            input.showDropDown();
                        }
                    });

                } else {
                    input.dismissDropDown();
                }
            });
        }).start();
    }

    private List<AddressSuggestion> searchAddresses(
            String query
    ) {
        List<AddressSuggestion> result =
                new ArrayList<>();

        try {
            Geocoder geocoder =
                    new Geocoder(
                            this,
                            Locale.getDefault()
                    );

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            query + ", Novi Sad",
                            6,
                            45.15,
                            19.65,
                            45.40,
                            20.10
                    );

            if (addresses == null) {
                return result;
            }

            Set<String> usedAddresses =
                    new LinkedHashSet<>();

            for (Address address : addresses) {
                String addressText =
                        formatAddress(address);

                if (addressText.isEmpty()
                        || usedAddresses.contains(
                        addressText
                )) {
                    continue;
                }

                usedAddresses.add(addressText);

                result.add(
                        new AddressSuggestion(
                                addressText,
                                address.getLatitude(),
                                address.getLongitude()
                        )
                );
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    private String formatAddress(
            Address address
    ) {
        if (address == null) {
            return "";
        }

        String addressLine =
                address.getAddressLine(0);

        if (addressLine != null
                && !addressLine.trim().isEmpty()) {

            return addressLine.trim();
        }

        List<String> parts =
                new ArrayList<>();

        if (address.getThoroughfare() != null) {
            parts.add(
                    address.getThoroughfare()
            );
        }

        if (address.getSubThoroughfare() != null) {
            parts.add(
                    address.getSubThoroughfare()
            );
        }

        if (address.getLocality() != null) {
            parts.add(
                    address.getLocality()
            );
        }

        return String.join(", ", parts);
    }

    private void cancelSuggestionRunnable(
            AddressField field
    ) {
        Runnable runnable =
                getSuggestionRunnable(field);

        if (runnable != null) {
            suggestionHandler.removeCallbacks(
                    runnable
            );
        }
    }

    private Runnable getSuggestionRunnable(
            AddressField field
    ) {
        if (field == AddressField.FROM) {
            return fromSuggestionRunnable;
        }

        if (field == AddressField.TO) {
            return toSuggestionRunnable;
        }

        return stopSuggestionRunnable;
    }

    private void setSuggestionRunnable(
            AddressField field,
            Runnable runnable
    ) {
        if (field == AddressField.FROM) {
            fromSuggestionRunnable = runnable;

        } else if (field == AddressField.TO) {
            toSuggestionRunnable = runnable;

        } else {
            stopSuggestionRunnable = runnable;
        }
    }

    private int incrementSuggestionRequest(
            AddressField field
    ) {
        if (field == AddressField.FROM) {
            return fromSuggestionRequest
                    .incrementAndGet();
        }

        if (field == AddressField.TO) {
            return toSuggestionRequest
                    .incrementAndGet();
        }

        return stopSuggestionRequest
                .incrementAndGet();
    }

    private int getSuggestionRequest(
            AddressField field
    ) {
        if (field == AddressField.FROM) {
            return fromSuggestionRequest.get();
        }

        if (field == AddressField.TO) {
            return toSuggestionRequest.get();
        }

        return stopSuggestionRequest.get();
    }

    private void invalidateSuggestionRequest(
            AddressField field
    ) {
        cancelSuggestionRunnable(field);
        incrementSuggestionRequest(field);
    }

    private void reverseGeocodeMapPoint(
            GeoPoint point
    ) {
        new Thread(() -> {
            String addressText =
                    String.format(
                            Locale.US,
                            "%.6f, %.6f",
                            point.getLatitude(),
                            point.getLongitude()
                    );

            try {
                Geocoder geocoder =
                        new Geocoder(
                                this,
                                Locale.getDefault()
                        );

                List<Address> results =
                        geocoder.getFromLocation(
                                point.getLatitude(),
                                point.getLongitude(),
                                1
                        );

                if (results != null
                        && !results.isEmpty()) {

                    String formatted =
                            formatAddress(
                                    results.get(0)
                            );

                    if (!formatted.isEmpty()) {
                        addressText = formatted;
                    }
                }

            } catch (Exception ignored) {
            }

            String finalAddressText =
                    addressText;

            runOnUiThread(() ->
                    applyMapSelectedPoint(
                            point,
                            finalAddressText
                    )
            );
        }).start();
    }

    private void applyMapSelectedPoint(
            GeoPoint point,
            String address
    ) {
        CoordinateDTO coordinate =
                new CoordinateDTO(
                        point.getLatitude(),
                        point.getLongitude(),
                        address
                );

        rememberCoordinate(coordinate);

        if (mapSelectionStep == 0) {
            invalidateSuggestionRequest(
                    AddressField.FROM
            );

            selectedFromCoordinate =
                    coordinate;

            setAddressText(
                    fromInput,
                    address
            );

            fromSuggestionAdapter
                    .clearSuggestions();

            fromLayout.setError(null);

            Toast.makeText(
                    this,
                    "Starting location selected.",
                    Toast.LENGTH_SHORT
            ).show();

            mapSelectionStep = 1;

        } else {
            invalidateSuggestionRequest(
                    AddressField.TO
            );

            selectedToCoordinate =
                    coordinate;

            setAddressText(
                    toInput,
                    address
            );

            toSuggestionAdapter
                    .clearSuggestions();

            toLayout.setError(null);

            Toast.makeText(
                    this,
                    "Destination selected.",
                    Toast.LENGTH_SHORT
            ).show();

            mapSelectionStep = 0;
        }

        invalidateCalculatedRoute();
        refreshSelectedMapMarkers();
    }

    private void setAddressText(
            MaterialAutoCompleteTextView input,
            String value
    ) {
        updatingAddressProgrammatically = true;

        input.setText(
                value == null ? "" : value,
                false
        );

        input.setSelection(
                input.length()
        );

        input.dismissDropDown();

        updatingAddressProgrammatically = false;
    }

    private void rememberCoordinate(
            CoordinateDTO coordinate
    ) {
        if (coordinate == null
                || coordinate.getAddress() == null) {

            return;
        }

        knownCoordinates.put(
                normalizeAddress(
                        coordinate.getAddress()
                ),
                coordinate
        );
    }

    private CoordinateDTO findKnownCoordinate(
            String address
    ) {
        return knownCoordinates.get(
                normalizeAddress(address)
        );
    }

    private String normalizeAddress(
            String address
    ) {
        if (address == null) {
            return "";
        }

        return address.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private void refreshSelectedMapMarkers() {
        clearMapOverlaysKeepingEvents();

        if (selectedFromCoordinate != null) {
            addRouteMarker(
                    selectedFromCoordinate,
                    "From"
            );
        }

        if (selectedToCoordinate != null) {
            addRouteMarker(
                    selectedToCoordinate,
                    "To"
            );
        }

        mapView.invalidate();
    }

    private void clearMapOverlaysKeepingEvents() {
        mapView.getOverlays().clear();

        if (mapEventsOverlay != null) {
            mapView.getOverlays()
                    .add(mapEventsOverlay);
        }
    }

    private void loadVehiclePrices() {
        pricesLoaded = false;

        pricingApi.getAllVehicleTypePrices()
                .enqueue(
                        new Callback<List<VehicleTypePricingDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<VehicleTypePricingDTO>> call,

                                    @NonNull
                                    Response<List<VehicleTypePricingDTO>> response
                            ) {
                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    pricesLoaded = false;
                                    return;
                                }

                                vehicleBasePrices.clear();

                                for (VehicleTypePricingDTO pricing
                                        : response.body()) {

                                    if (pricing == null
                                            || pricing.vehicleType == null
                                            || pricing.basePrice == null) {

                                        continue;
                                    }

                                    Double parsedPrice =
                                            parseBasePrice(
                                                    pricing.basePrice
                                            );

                                    if (parsedPrice != null) {
                                        vehicleBasePrices.put(
                                                pricing.vehicleType
                                                        .trim()
                                                        .toUpperCase(
                                                                Locale.ROOT
                                                        ),
                                                parsedPrice
                                        );
                                    }
                                }

                                pricesLoaded =
                                        !vehicleBasePrices.isEmpty();
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<VehicleTypePricingDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                pricesLoaded = false;
                            }
                        }
                );
    }

    private Double parseBasePrice(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        String normalized =
                value.trim()
                        .replace("RSD", "")
                        .replace("rsd", "")
                        .replace(" ", "")
                        .replace(",", ".");

        try {
            return Double.parseDouble(
                    normalized
            );

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void setupScheduledTimeOptions() {
        scheduledTimeValues.clear();

        Calendar now =
                Calendar.getInstance();

        now.set(
                Calendar.SECOND,
                0
        );

        now.set(
                Calendar.MILLISECOND,
                0
        );

        int currentMinute =
                now.get(Calendar.MINUTE);

        int remainder =
                currentMinute % 5;

        int minutesToNextStep =
                remainder == 0
                        ? 5
                        : 5 - remainder;

        Calendar firstOption =
                (Calendar) now.clone();

        firstOption.add(
                Calendar.MINUTE,
                minutesToNextStep
        );

        Calendar maximum =
                (Calendar) now.clone();

        maximum.add(
                Calendar.HOUR_OF_DAY,
                5
        );

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        List<String> labels =
                new ArrayList<>();

        Calendar current =
                (Calendar) firstOption.clone();

        while (!current.after(maximum)) {
            scheduledTimeValues.add(
                    (Calendar) current.clone()
            );

            labels.add(
                    formatter.format(
                            current.getTime()
                    )
            );

            current.add(
                    Calendar.MINUTE,
                    5
            );
        }

        ScheduledTimeAdapter adapter =
                new ScheduledTimeAdapter(
                        this,
                        labels
                );

        scheduledTimeSpinner.setAdapter(
                adapter
        );
    }

    private void showFormTemporarilyDisabled() {
        formContainer.setVisibility(
                View.GONE
        );

        blockedOverlay.setVisibility(
                View.GONE
        );
    }

    private void showOrderingForm() {
        orderingDisabled = false;

        blockedOverlay.setVisibility(
                View.GONE
        );

        formContainer.setVisibility(
                View.VISIBLE
        );
    }

    private void addStop() {
        String address =
                textOf(stopInput);

        if (address.isEmpty()) {
            stopLayout.setError(
                    "Enter a stop address"
            );

            return;
        }

        stopLayout.setError(null);

        if (selectedStopCoordinate != null) {
            rememberCoordinate(
                    selectedStopCoordinate
            );
        }

        stopAddresses.add(address);

        selectedStopCoordinate = null;

        invalidateSuggestionRequest(
                AddressField.STOP
        );

        stopSuggestionAdapter
                .clearSuggestions();

        setAddressText(
                stopInput,
                ""
        );

        rebuildStops();
        invalidateCalculatedRoute();
    }

    private void rebuildStops() {
        stopsContainer.removeAllViews();

        for (int index = 0;
             index < stopAddresses.size();
             index++) {

            final int currentIndex = index;

            View item =
                    LayoutInflater.from(this)
                            .inflate(
                                    R.layout.item_route_chip,
                                    stopsContainer,
                                    false
                            );

            TextView text =
                    item.findViewById(
                            R.id.tvChipText
                    );

            ImageButton removeButton =
                    item.findViewById(
                            R.id.btnRemoveChip
                    );

            text.setText(
                    (index + 1)
                            + ". "
                            + stopAddresses.get(index)
            );

            removeButton.setOnClickListener(
                    view -> {
                        stopAddresses.remove(
                                currentIndex
                        );

                        rebuildStops();
                        invalidateCalculatedRoute();
                    }
            );

            stopsContainer.addView(item);
        }
    }

    private void addPassenger() {
        String email =
                textOf(passengerInput);

        if (email.isEmpty()) {
            passengerLayout.setError(
                    "Enter passenger email"
            );

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            passengerLayout.setError(
                    "Enter a valid email address"
            );

            return;
        }

        if (passengerEmails.contains(email)) {
            passengerLayout.setError(
                    "Passenger is already added"
            );

            return;
        }

        passengerLayout.setError(null);

        passengerEmails.add(email);

        passengerInput.setText("");

        rebuildPassengers();
    }

    private void rebuildPassengers() {
        passengersContainer.removeAllViews();

        for (int index = 0;
             index < passengerEmails.size();
             index++) {

            final int currentIndex = index;

            View item =
                    LayoutInflater.from(this)
                            .inflate(
                                    R.layout.item_route_chip,
                                    passengersContainer,
                                    false
                            );

            TextView text =
                    item.findViewById(
                            R.id.tvChipText
                    );

            ImageButton removeButton =
                    item.findViewById(
                            R.id.btnRemoveChip
                    );

            text.setText(
                    passengerEmails.get(index)
            );

            removeButton.setOnClickListener(
                    view -> {
                        passengerEmails.remove(
                                currentIndex
                        );

                        rebuildPassengers();
                    }
            );

            passengersContainer.addView(item);
        }
    }

    private void calculateRoute() {
        clearInputErrors();

        if (!validateRouteInputs()) {
            return;
        }

        if (!pricesLoaded) {
            loadVehiclePrices();

            Toast.makeText(
                    this,
                    "Price list is still loading. Try again in a moment.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoading(true);

        String fromAddress =
                textOf(fromInput);

        String toAddress =
                textOf(toInput);

        List<String> currentStops =
                new ArrayList<>(
                        stopAddresses
                );

        new Thread(() -> {
            try {
                CoordinateDTO from =
                        resolveCoordinate(
                                fromAddress,
                                selectedFromCoordinate
                        );

                CoordinateDTO to =
                        resolveCoordinate(
                                toAddress,
                                selectedToCoordinate
                        );

                List<CoordinateDTO> stops =
                        new ArrayList<>();

                for (String address : currentStops) {
                    CoordinateDTO known =
                            findKnownCoordinate(address);

                    stops.add(
                            resolveCoordinate(
                                    address,
                                    known
                            )
                    );
                }

                runOnUiThread(() ->
                        requestRoadRoute(
                                from,
                                stops,
                                to
                        )
                );

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setLoading(false);

                    String message =
                            exception.getMessage();

                    if (message == null
                            || message.trim().isEmpty()) {

                        message =
                                "Route could not be calculated.";
                    }

                    Toast.makeText(
                            RideOrderActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        }).start();
    }

    private CoordinateDTO resolveCoordinate(
            String address,
            CoordinateDTO selectedCoordinate
    ) throws Exception {

        if (selectedCoordinate != null
                && normalizeAddress(
                selectedCoordinate.getAddress()
        ).equals(
                normalizeAddress(address)
        )) {

            return selectedCoordinate;
        }

        CoordinateDTO known =
                findKnownCoordinate(address);

        if (known != null) {
            return known;
        }

        CoordinateDTO geocoded =
                geocodeAddress(address);

        rememberCoordinate(geocoded);

        return geocoded;
    }

    private void requestRoadRoute(
            CoordinateDTO from,
            List<CoordinateDTO> stops,
            CoordinateDTO to
    ) {
        String coordinates =
                buildOsrmCoordinates(
                        from,
                        stops,
                        to
                );

        osrmApi.getRoute(
                coordinates,
                "full",
                "geojson"
        ).enqueue(
                new Callback<OsrmRouteResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<OsrmRouteResponse> call,

                            @NonNull
                            Response<OsrmRouteResponse> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body()
                                .getRoutes() == null
                                || response.body()
                                .getRoutes()
                                .isEmpty()) {

                            Toast.makeText(
                                    RideOrderActivity.this,
                                    "A road route could not be found.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        OsrmRouteDTO route =
                                response.body()
                                        .getRoutes()
                                        .get(0);

                        if (route.getGeometry() == null
                                || route.getGeometry()
                                .getCoordinates() == null
                                || route.getGeometry()
                                .getCoordinates()
                                .isEmpty()) {

                            Toast.makeText(
                                    RideOrderActivity.this,
                                    "The routing service returned no route geometry.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        calculatedFrom = from;
                        calculatedTo = to;

                        selectedFromCoordinate = from;
                        selectedToCoordinate = to;

                        rememberCoordinate(from);
                        rememberCoordinate(to);

                        calculatedStops.clear();
                        calculatedStops.addAll(stops);

                        calculatedDistanceKm =
                                Math.round(
                                        route.getDistance()
                                                / 10.0
                                ) / 100.0;

                        calculatedPrice =
                                calculateEstimatedPrice();

                        showCalculatedRoadRoute(
                                route.getGeometry()
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<OsrmRouteResponse> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        setLoading(false);

                        Toast.makeText(
                                RideOrderActivity.this,
                                "Routing service error: "
                                        + throwable.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String buildOsrmCoordinates(
            CoordinateDTO from,
            List<CoordinateDTO> stops,
            CoordinateDTO to
    ) {
        StringBuilder builder =
                new StringBuilder();

        appendOsrmCoordinate(
                builder,
                from
        );

        for (CoordinateDTO stop : stops) {
            builder.append(";");

            appendOsrmCoordinate(
                    builder,
                    stop
            );
        }

        builder.append(";");

        appendOsrmCoordinate(
                builder,
                to
        );

        return builder.toString();
    }

    private void appendOsrmCoordinate(
            StringBuilder builder,
            CoordinateDTO coordinate
    ) {
        builder.append(
                String.format(
                        Locale.US,
                        "%.7f,%.7f",
                        coordinate.getLongitude(),
                        coordinate.getLatitude()
                )
        );
    }

    private Double calculateEstimatedPrice() {
        Double basePrice =
                vehicleBasePrices.get(
                        getVehicleType()
                );

        if (basePrice == null
                || calculatedDistanceKm <= 0) {

            return null;
        }

        return basePrice
                + calculatedDistanceKm
                * PRICE_PER_KILOMETRE;
    }

    private void showCalculatedRoadRoute(
            OsrmGeometryDTO geometry
    ) {
        estimateContainer.setVisibility(
                View.VISIBLE
        );

        distanceText.setText(
                String.format(
                        Locale.US,
                        "Estimated distance: %.2f km",
                        calculatedDistanceKm
                )
        );

        if (calculatedPrice == null) {
            priceText.setText(
                    "Estimated price: unavailable"
            );

        } else {
            priceText.setText(
                    String.format(
                            Locale.US,
                            "Estimated price: %.2f RSD",
                            calculatedPrice
                    )
            );
        }

        drawOsrmRouteOnMap(geometry);
    }

    private void drawOsrmRouteOnMap(
            OsrmGeometryDTO geometry
    ) {
        clearMapOverlaysKeepingEvents();

        List<GeoPoint> roadPoints =
                new ArrayList<>();

        for (List<Double> coordinate
                : geometry.getCoordinates()) {

            if (coordinate == null
                    || coordinate.size() < 2) {

                continue;
            }

            double longitude =
                    coordinate.get(0);

            double latitude =
                    coordinate.get(1);

            roadPoints.add(
                    new GeoPoint(
                            latitude,
                            longitude
                    )
            );
        }

        if (roadPoints.isEmpty()) {
            return;
        }

        Polyline polyline =
                new Polyline();

        polyline.setPoints(roadPoints);

        polyline.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.dark_blue
                )
        );

        polyline.setWidth(9f);

        mapView.getOverlays()
                .add(polyline);

        addRouteMarker(
                calculatedFrom,
                "From"
        );

        for (int index = 0;
             index < calculatedStops.size();
             index++) {

            addRouteMarker(
                    calculatedStops.get(index),
                    "Stop " + (index + 1)
            );
        }

        addRouteMarker(
                calculatedTo,
                "To"
        );

        BoundingBox boundingBox =
                BoundingBox.fromGeoPoints(
                        roadPoints
                );

        mapView.post(() -> {
            mapView.zoomToBoundingBox(
                    boundingBox,
                    true,
                    dpToPx(45)
            );

            mapView.invalidate();
        });
    }

    private void addRouteMarker(
            CoordinateDTO coordinate,
            String title
    ) {
        if (coordinate == null) {
            return;
        }

        Marker marker =
                new Marker(mapView);

        marker.setPosition(
                toGeoPoint(coordinate)
        );

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        marker.setIcon(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_map_pin
                )
        );

        marker.setTitle(title);

        marker.setSnippet(
                coordinate.getAddress()
        );

        mapView.getOverlays()
                .add(marker);
    }

    private void orderRide() {
        if (orderingDisabled) {
            return;
        }

        clearInputErrors();

        if (!validateRouteInputs()) {
            return;
        }

        if (calculatedFrom == null
                || calculatedTo == null
                || calculatedDistanceKm <= 0) {

            Toast.makeText(
                    this,
                    "Calculate the route before ordering.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Calendar selectedScheduledTime =
                null;

        if (scheduledRadioButton.isChecked()) {
            int selectedPosition =
                    scheduledTimeSpinner
                            .getSelectedItemPosition();

            if (selectedPosition < 0
                    || selectedPosition
                    >= scheduledTimeValues.size()) {

                Toast.makeText(
                        this,
                        "Select scheduled time.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            selectedScheduledTime =
                    scheduledTimeValues.get(
                            selectedPosition
                    );
        }

        CreateRideDTO request =
                new CreateRideDTO();

        request.setFrom(calculatedFrom);
        request.setTo(calculatedTo);

        request.setStops(
                new ArrayList<>(
                        calculatedStops
                )
        );

        request.setPassengerEmails(
                new ArrayList<>(
                        passengerEmails
                )
        );

        request.setVehicleType(
                getVehicleType()
        );

        request.setBabyTransport(
                babyTransportCheckBox
                        .isChecked()
        );

        request.setPetTransport(
                petTransportCheckBox
                        .isChecked()
        );

        request.setDistanceKm(
                calculatedDistanceKm
        );

        if (selectedScheduledTime != null) {
            request.setScheduledTime(
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.US
                    ).format(
                            selectedScheduledTime
                                    .getTime()
                    )
            );

        } else {
            request.setScheduledTime(null);
        }

        setLoading(true);

        rideApi.createRide(request)
                .enqueue(
                        new Callback<CreatedRideDTO>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<CreatedRideDTO> call,

                                    @NonNull
                                    Response<CreatedRideDTO> response
                            ) {
                                setLoading(false);

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    showRideOrderError(
                                            response.code()
                                    );

                                    return;
                                }

                                clearForm();
                                checkActiveRide();
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<CreatedRideDTO> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);

                                Toast.makeText(
                                        RideOrderActivity.this,
                                        "Network error: "
                                                + throwable.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void loadFavoriteRoutes() {
        setLoading(true);

        favoriteRouteApi.getFavorites()
                .enqueue(
                        new Callback<List<FavoriteRouteDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<FavoriteRouteDTO>> call,

                                    @NonNull
                                    Response<List<FavoriteRouteDTO>> response
                            ) {
                                setLoading(false);

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    Toast.makeText(
                                            RideOrderActivity.this,
                                            "Favorite routes could not be loaded.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                showFavoriteRoutesDialog(
                                        response.body()
                                );
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<FavoriteRouteDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);

                                Toast.makeText(
                                        RideOrderActivity.this,
                                        "Network error: "
                                                + throwable.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void showFavoriteRoutesDialog(
            List<FavoriteRouteDTO> routes
    ) {
        if (routes == null
                || routes.isEmpty()) {

            Toast.makeText(
                    this,
                    "You do not have favorite routes.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_favorite_routes,
                                null,
                                false
                        );

        LinearLayout routesContainer =
                dialogView.findViewById(
                        R.id.favoriteRoutesContainer
                );

        TextView closeButton =
                dialogView.findViewById(
                        R.id.btnCloseFavoriteRoutes
                );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();

        for (FavoriteRouteDTO route : routes) {
            View routeItem =
                    LayoutInflater.from(this)
                            .inflate(
                                    R.layout.item_favorite_route,
                                    routesContainer,
                                    false
                            );

            TextView fromText =
                    routeItem.findViewById(
                            R.id.tvFavoriteFrom
                    );

            TextView toText =
                    routeItem.findViewById(
                            R.id.tvFavoriteTo
                    );

            TextView stopsText =
                    routeItem.findViewById(
                            R.id.tvFavoriteStops
                    );

            LinearLayout stopsSection =
                    routeItem.findViewById(
                            R.id.favoriteStopsSection
                    );

            MaterialButton useRouteButton =
                    routeItem.findViewById(
                            R.id.btnUseFavoriteRoute
                    );

            fromText.setText(
                    valueOrFallback(
                            route.getStartLocation(),
                            "Starting location"
                    )
            );

            toText.setText(
                    valueOrFallback(
                            route.getDestination(),
                            "Destination"
                    )
            );

            List<String> stops =
                    route.getStops();

            if (stops != null
                    && !stops.isEmpty()) {

                stopsSection.setVisibility(
                        View.VISIBLE
                );

                StringBuilder stopsBuilder =
                        new StringBuilder();

                for (int index = 0;
                     index < stops.size();
                     index++) {

                    if (index > 0) {
                        stopsBuilder.append("\n");
                    }

                    stopsBuilder.append("• ")
                            .append(
                                    stops.get(index)
                            );
                }

                stopsText.setText(
                        stopsBuilder.toString()
                );

            } else {
                stopsSection.setVisibility(
                        View.GONE
                );
            }

            useRouteButton.setOnClickListener(
                    view -> {
                        applyFavoriteRoute(route);
                        dialog.dismiss();
                    }
            );

            routesContainer.addView(
                    routeItem
            );
        }

        closeButton.setOnClickListener(
                view -> dialog.dismiss()
        );

        dialog.setOnShowListener(listener -> {
            Window window =
                    dialog.getWindow();

            if (window == null) {
                return;
            }

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            int width =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.92f
                    );

            window.setLayout(
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setDimAmount(
                    0.60f
            );

            window.addFlags(
                    WindowManager.LayoutParams
                            .FLAG_DIM_BEHIND
            );
        });

        dialog.show();
    }

    private void applyFavoriteRoute(
            FavoriteRouteDTO route
    ) {
        invalidateSuggestionRequest(
                AddressField.FROM
        );

        invalidateSuggestionRequest(
                AddressField.TO
        );

        fromSuggestionAdapter
                .clearSuggestions();

        toSuggestionAdapter
                .clearSuggestions();

        selectedFromCoordinate = null;
        selectedToCoordinate = null;

        setAddressText(
                fromInput,
                route.getStartLocation()
        );

        setAddressText(
                toInput,
                route.getDestination()
        );

        stopAddresses.clear();

        if (route.getStops() != null) {
            stopAddresses.addAll(
                    route.getStops()
            );
        }

        rebuildStops();
        invalidateCalculatedRoute();
        refreshSelectedMapMarkers();

        Toast.makeText(
                this,
                "Favorite route selected.",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void checkBlockedStatus() {
        profileApi.getProfile()
                .enqueue(
                        new Callback<ProfileResponseDTO>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<ProfileResponseDTO> call,

                                    @NonNull
                                    Response<ProfileResponseDTO> response
                            ) {
                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    checkActiveRide();
                                    return;
                                }

                                ProfileResponseDTO profile =
                                        response.body();

                                if (profile.isBlocked()) {
                                    String reason =
                                            profile.getBlockingReason();

                                    String message =
                                            "You cannot order rides at this time.";

                                    if (reason != null
                                            && !reason.trim().isEmpty()) {

                                        message +=
                                                "\n\nReason:\n"
                                                        + reason.trim();
                                    }

                                    showOrderingDisabled(
                                            "🚫",
                                            "ACCOUNT BLOCKED",
                                            message
                                    );

                                    return;
                                }

                                checkActiveRide();
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<ProfileResponseDTO> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                checkActiveRide();
                            }
                        }
                );
    }

    private void checkActiveRide() {
        rideApi.activeRide()
                .enqueue(
                        new Callback<ActiveRideResponse>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<ActiveRideResponse> call,

                                    @NonNull
                                    Response<ActiveRideResponse> response
                            ) {
                                if (response.isSuccessful()
                                        && response.body() != null
                                        && response.body()
                                        .isHasActiveRide()) {

                                    showOrderingDisabled(
                                            "🚗",
                                            "ACTIVE RIDE",
                                            "You cannot order a new ride while "
                                                    + "your current ride is active."
                                    );

                                    return;
                                }

                                showOrderingForm();
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<ActiveRideResponse> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                showOrderingForm();
                            }
                        }
                );
    }

    private void showOrderingDisabled(
            String icon,
            String title,
            String message
    ) {
        orderingDisabled = true;

        formContainer.setVisibility(
                View.GONE
        );

        blockedIcon.setText(icon);
        blockedTitle.setText(title);
        blockedMessage.setText(message);

        blockedOverlay.setVisibility(
                View.VISIBLE
        );
    }

    private boolean validateRouteInputs() {
        boolean valid = true;

        if (textOf(fromInput).isEmpty()) {
            fromLayout.setError(
                    "Starting address is required"
            );

            valid = false;
        }

        if (textOf(toInput).isEmpty()) {
            toLayout.setError(
                    "Destination is required"
            );

            valid = false;
        }

        return valid;
    }

    private void clearInputErrors() {
        fromLayout.setError(null);
        toLayout.setError(null);
        stopLayout.setError(null);
        passengerLayout.setError(null);
    }

    private CoordinateDTO geocodeAddress(
            String addressText
    ) throws Exception {

        Geocoder geocoder =
                new Geocoder(
                        this,
                        Locale.getDefault()
                );

        List<Address> addresses =
                geocoder.getFromLocationName(
                        addressText + ", Novi Sad",
                        1,
                        45.15,
                        19.65,
                        45.40,
                        20.10
                );

        if (addresses == null
                || addresses.isEmpty()) {

            throw new Exception(
                    "Address not found: "
                            + addressText
            );
        }

        Address address =
                addresses.get(0);

        String formattedAddress =
                formatAddress(address);

        if (formattedAddress.isEmpty()) {
            formattedAddress = addressText;
        }

        return new CoordinateDTO(
                address.getLatitude(),
                address.getLongitude(),
                formattedAddress
        );
    }

    private String getVehicleType() {
        int checkedId =
                vehicleTypeGroup
                        .getCheckedRadioButtonId();

        if (checkedId == R.id.rbLuxury) {
            return "LUXURY";
        }

        if (checkedId == R.id.rbVan) {
            return "VAN";
        }

        return "STANDARD";
    }

    private GeoPoint toGeoPoint(
            CoordinateDTO coordinate
    ) {
        return new GeoPoint(
                coordinate.getLatitude(),
                coordinate.getLongitude()
        );
    }

    private void invalidateCalculatedRoute() {
        calculatedFrom = null;
        calculatedTo = null;

        calculatedStops.clear();

        calculatedDistanceKm = 0;
        calculatedPrice = null;

        if (estimateContainer != null) {
            estimateContainer.setVisibility(
                    View.GONE
            );
        }
    }

    private void clearForm() {
        invalidateSuggestionRequest(
                AddressField.FROM
        );

        invalidateSuggestionRequest(
                AddressField.TO
        );

        invalidateSuggestionRequest(
                AddressField.STOP
        );

        setAddressText(fromInput, "");
        setAddressText(toInput, "");
        setAddressText(stopInput, "");

        passengerInput.setText("");

        stopAddresses.clear();
        passengerEmails.clear();
        knownCoordinates.clear();

        selectedFromCoordinate = null;
        selectedToCoordinate = null;
        selectedStopCoordinate = null;

        mapSelectionStep = 0;

        fromSuggestionAdapter
                .clearSuggestions();

        toSuggestionAdapter
                .clearSuggestions();

        stopSuggestionAdapter
                .clearSuggestions();

        rebuildStops();
        rebuildPassengers();

        standardRadioButton.setChecked(true);
        nowRadioButton.setChecked(true);

        babyTransportCheckBox.setChecked(false);
        petTransportCheckBox.setChecked(false);

        scheduledLabel.setVisibility(
                View.GONE
        );

        scheduledTimeSpinner.setVisibility(
                View.GONE
        );

        scheduleHint.setVisibility(
                View.GONE
        );

        setupScheduledTimeOptions();

        clearInputErrors();
        invalidateCalculatedRoute();

        clearMapOverlaysKeepingEvents();

        mapView.getController()
                .setCenter(NOVI_SAD);

        mapView.getController()
                .setZoom(13.0);

        mapView.invalidate();
    }

    private void setLoading(
            boolean loading
    ) {
        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        calculateButton.setEnabled(!loading);
        orderButton.setEnabled(!loading);
        favoriteRoutesButton.setEnabled(!loading);
        addStopButton.setEnabled(!loading);
        addPassengerButton.setEnabled(!loading);
        clearButton.setEnabled(!loading);
    }

    private void showRideOrderError(
            int code
    ) {
        String message;

        if (code == 400) {
            message =
                    "Entered ride information is invalid.";

        } else if (code == 403) {
            message =
                    "You are not allowed to order a ride.";

        } else if (code == 409) {
            message =
                    "No suitable driver is currently available "
                            + "or you already have an active ride.";

        } else {
            message =
                    "Ride could not be ordered. "
                            + "Error code: "
                            + code;
        }

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private String textOf(
            TextView input
    ) {
        if (input == null
                || input.getText() == null) {

            return "";
        }

        return input.getText()
                .toString()
                .trim();
    }

    private String valueOrFallback(
            String value,
            String fallback
    ) {
        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        return value;
    }

    private int dpToPx(
            int dp
    ) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void logout() {
        TokenStorage.clear(this);

        Intent intent =
                new Intent(
                        this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        suggestionHandler.removeCallbacksAndMessages(
                null
        );

        if (mapView != null) {
            mapView.onDetach();
        }

        super.onDestroy();
    }

    private enum AddressField {
        FROM,
        TO,
        STOP
    }
}