package com.example.nuvola.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.nuvola.R;

import java.util.ArrayList;
import java.util.List;

import dto.AddressSuggestion;

public class AddressSuggestionAdapter
        extends BaseAdapter
        implements Filterable {

    private final LayoutInflater inflater;

    private final List<AddressSuggestion> suggestions =
            new ArrayList<>();

    public AddressSuggestionAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setSuggestions(
            List<AddressSuggestion> newSuggestions
    ) {
        suggestions.clear();

        if (newSuggestions != null) {
            suggestions.addAll(newSuggestions);
        }

        notifyDataSetChanged();
    }

    public void clearSuggestions() {
        suggestions.clear();
        notifyDataSetChanged();
    }

    public AddressSuggestion getSuggestion(
            int position
    ) {
        if (position < 0
                || position >= suggestions.size()) {

            return null;
        }

        return suggestions.get(position);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public AddressSuggestion getItem(
            int position
    ) {
        return suggestions.get(position);
    }

    @Override
    public long getItemId(
            int position
    ) {
        return position;
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent
    ) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(
                    R.layout.item_address_suggestion,
                    parent,
                    false
            );
        }

        TextView addressText =
                view.findViewById(
                        R.id.tvSuggestedAddress
                );

        AddressSuggestion suggestion =
                getItem(position);

        addressText.setText(
                suggestion.getAddress()
        );

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(
                    CharSequence constraint
            ) {
                FilterResults results =
                        new FilterResults();

                results.values =
                        new ArrayList<>(suggestions);

                results.count =
                        suggestions.size();

                return results;
            }

            @Override
            protected void publishResults(
                    CharSequence constraint,
                    FilterResults results
            ) {
                notifyDataSetChanged();
            }
        };
    }
}