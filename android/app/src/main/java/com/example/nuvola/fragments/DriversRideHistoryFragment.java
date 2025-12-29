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
import com.example.nuvola.databinding.FragmentDriversRideHistoryBinding;
import com.example.nuvola.model.Ride;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriversRideHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriversRideHistoryFragment extends ListFragment {

    private DriversRideHistoryListAdapter adapter;
    private ArrayList<Ride> mRides;
    private FragmentDriversRideHistoryBinding binding;
    private static final String ARG_PARAM1 = "param";

    private boolean isDateAscending = false;

    public DriversRideHistoryFragment() {
        // Required empty public constructor
    }

    public static DriversRideHistoryFragment newInstance(ArrayList<Ride> rides) {
        DriversRideHistoryFragment fragment = new DriversRideHistoryFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, rides);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRides = getArguments().getParcelableArrayList(ARG_PARAM1, Ride.class);
            adapter = new DriversRideHistoryListAdapter(getActivity(), mRides);
            setListAdapter(adapter);

        }
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
        isDateAscending = !isDateAscending;

        Collections.sort(mRides, new Comparator<Ride>() {
            @Override
            public int compare(Ride r1, Ride r2) {
                if (isDateAscending) {
                    return r1.startingTime.compareTo(r2.startingTime);
                } else {
                    return r2.startingTime.compareTo(r1.startingTime);
                }
            }
        });

        adapter.notifyDataSetChanged();
        button.setText(isDateAscending ? "Date ↑" : "Date ↓");

        Log.d("RideHistoryFragment", "Sorted by date: " + (isDateAscending ? "ascending" : "descending"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}