package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private final List<ChargingStation> stationList;
    private final Context context;

    public StationAdapter(List<ChargingStation> stationList, Context context) {
        this.stationList = stationList;
        this.context = context;
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
        holder.stationLocation.setText("Lat: " + station.getLatitude() + ", Lng: " + station.getLongitude());

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDetailsActivity.class);
            intent.putExtra("stationId", station.getStationId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations").child(station.getStationId());
            databaseReference.removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(context, "Station deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete station", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, stationLocation;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.stationName);
            stationLocation = itemView.findViewById(R.id.stationLocation);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
