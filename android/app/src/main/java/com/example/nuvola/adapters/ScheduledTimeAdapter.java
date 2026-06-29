package com.example.nuvola.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nuvola.R;

import java.util.List;

public class ScheduledTimeAdapter
        extends ArrayAdapter<String> {

    private final LayoutInflater inflater;

    public ScheduledTimeAdapter(
            @NonNull Context context,
            @NonNull List<String> values
    ) {
        super(
                context,
                R.layout.item_scheduled_time,
                values
        );

        inflater =
                LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        View view =
                convertView;

        if (view == null) {
            view = inflater.inflate(
                    R.layout.item_scheduled_time,
                    parent,
                    false
            );
        }

        TextView textView =
                view.findViewById(
                        R.id.tvScheduledTimeOption
                );

        textView.setText(
                getItem(position)
        );

        return view;
    }

    @Override
    public View getDropDownView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        View view =
                convertView;

        if (view == null) {
            view = inflater.inflate(
                    R.layout.item_scheduled_time_dropdown,
                    parent,
                    false
            );
        }

        TextView textView =
                view.findViewById(
                        R.id.tvScheduledTimeDropdownOption
                );

        textView.setText(
                getItem(position)
        );

        return view;
    }
}