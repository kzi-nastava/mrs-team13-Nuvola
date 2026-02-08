package com.example.nuvola.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.nuvola.R;
import com.example.nuvola.adapters.DriversRideHistoryListAdapter;
import com.example.nuvola.apis.ApiClient;
import com.example.nuvola.apis.NuvolaApi;
import com.example.nuvola.databinding.FragmentDriversRideHistoryBinding;
import com.example.nuvola.model.Ride;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import dto.DriverRideHistoryItemDTO;
import dto.PageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriversRideHistoryFragment - newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriversRideHistoryFragment extends ListFragment {

    private DriversRideHistoryListAdapter adapter;
    private ArrayList<Ride> mRides;
    private FragmentDriversRideHistoryBinding binding;
//    private static final String ARG_PARAM1 = "param";

    private NuvolaApi api;
    private boolean isDateAscending = false;

    public DriversRideHistoryFragment() {
        // Required empty public constructor
    }

//    public static DriversRideHistoryFragment newInstance(ArrayList<Ride> rides) {
//        DriversRideHistoryFragment fragment = new DriversRideHistoryFragment();
//        Bundle args = new Bundle();
//        args.putParcelableArrayList(ARG_PARAM1, rides);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mRides = getArguments().getParcelableArrayList(ARG_PARAM1, Ride.class);
//            adapter = new DriversRideHistoryListAdapter(getActivity(), mRides);
//            setListAdapter(adapter);
//
//        }
        mRides = new ArrayList<>();
        api = ApiClient.create();

        adapter = new DriversRideHistoryListAdapter(getActivity(), mRides);
        setListAdapter(adapter);

        loadRidesFromBackend();
    }

    private void loadRidesFromBackend() {
        String username = "testuser"; // TODO take from auth service
        String sortOrder = isDateAscending ? "asc" : "desc";

        api.getDriverRideHistory(username, "startingTime", sortOrder, 0, 20)
                .enqueue(new Callback<PageResponse<DriverRideHistoryItemDTO>>() {
                    @Override
                    public void onResponse(Call<PageResponse<DriverRideHistoryItemDTO>> call,
                                           Response<PageResponse<DriverRideHistoryItemDTO>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e("RideHistory", "HTTP error: " + response.code());
                            return;
                        }

                        mRides.clear();

                        for (DriverRideHistoryItemDTO dto : response.body().content) {
                            mRides.add(mapDtoToRide(dto));
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<PageResponse<DriverRideHistoryItemDTO>> call, Throwable t) {
                        Log.e("RideHistory", "Network fail", t);
                    }
                });
    }

    private Ride mapDtoToRide(DriverRideHistoryItemDTO dto) {
        Ride r = new Ride();
        r.id = dto.id;
        r.price = dto.price;
        r.driver = dto.driver;
        r.isFavouriteRoute = dto.favouriteRoute;
        r.pickup = dto.pickup;
        r.dropoff = dto.dropoff;
        try {
            r.startingTime = java.time.LocalDateTime.parse(dto.startingTime);
        } catch (Exception e) {
            r.startingTime = java.time.LocalDateTime.now(); // fallback
        }

        return r;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =FragmentDriversRideHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button sortByDateButton = root.findViewById(R.id.sort_by_date_button);
        sortByDateButton.setOnClickListener(v -> sortByDate(sortByDateButton));
        return root;
    }

    private void sortByDate(Button button) {
//        isDateAscending = !isDateAscending;
//
//        Collections.sort(mRides, new Comparator<Ride>() {
//            @Override
//            public int compare(Ride r1, Ride r2) {
//                if (isDateAscending) {
//                    return r1.startingTime.compareTo(r2.startingTime);
//                } else {
//                    return r2.startingTime.compareTo(r1.startingTime);
//                }
//            }
//        });
//
//        adapter.notifyDataSetChanged();
//        button.setText(isDateAscending ? "Date ↑" : "Date ↓");
//
//        Log.d("RideHistoryFragment", "Sorted by date: " + (isDateAscending ? "ascending" : "descending"));
        isDateAscending = !isDateAscending;
        button.setText(isDateAscending ? "Date ↑" : "Date ↓");
        loadRidesFromBackend();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}