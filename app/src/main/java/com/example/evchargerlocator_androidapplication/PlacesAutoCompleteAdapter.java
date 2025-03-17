package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;

import java.util.ArrayList;
import java.util.List;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private final PlacesClient placesClient;
    private final List<Place.Field> placeFields;
    private List<String> suggestions = new ArrayList<>();

    public PlacesAutoCompleteAdapter(Context context, PlacesClient placesClient, List<Place.Field> fields) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        this.placesClient = placesClient;
        this.placeFields = fields;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int position) {
        return suggestions.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(constraint.toString())
                            .build();

                    placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener(response -> {
                                suggestions.clear();
                                response.getAutocompletePredictions().forEach(prediction ->
                                        suggestions.add(prediction.getPrimaryText(null).toString()));

                                results.values = suggestions;
                                results.count = suggestions.size();
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> results.count = 0);
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
