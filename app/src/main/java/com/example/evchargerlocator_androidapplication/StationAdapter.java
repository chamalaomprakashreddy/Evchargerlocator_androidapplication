package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private final List<ChargingStation> stationList;
    private final Context context;
    private final Geocoder geocoder;

    public StationAdapter(List<ChargingStation> stationList, Context context) {
        this.stationList = stationList;
        this.context = context;
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.station_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChargingStation station = stationList.get(position);
        holder.stationName.setText(station.getName());

        // Get address from coordinates
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    station.getLatitude(),
                    station.getLongitude(),
                    1
            );

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = formatAddress(address);
                holder.stationAddress.setText(addressText);
            } else {
                // Fallback to coordinates if no address found
                holder.stationAddress.setText("Lat: " + station.getLatitude() +
                        ", Lng: " + station.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to coordinates if geocoding fails
            holder.stationAddress.setText("Lat: " + station.getLatitude() +
                    ", Lng: " + station.getLongitude());
        }

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDetailsActivity.class);
            intent.putExtra("stationId", station.getStationId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("ChargingStations")
                    .child(station.getStationId());
            databaseReference.removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(context, "Station deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete station", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String formatAddress(Address address) {
        StringBuilder addressText = new StringBuilder();

        // Build address from available components
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressText.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex()) {
                addressText.append(", ");
            }
        }

        return addressText.toString();
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, stationAddress;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.stationName);
            stationAddress = itemView.findViewById(R.id.stationAddress);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}