package com.example.evchargerlocator_androidapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedTripAdapter extends RecyclerView.Adapter<SavedTripAdapter.SavedTripViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(SavedTrip trip);
        void onTripDelete(SavedTrip trip, int position);
    }

    private List<SavedTrip> tripList;
    private OnTripClickListener listener;
    private Context context;

    public SavedTripAdapter(List<SavedTrip> tripList, OnTripClickListener listener, Context context) {
        this.tripList = tripList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public SavedTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_trip, parent, false);
        return new SavedTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedTripViewHolder holder, int position) {
        SavedTrip trip = tripList.get(position);
        holder.tripName.setText(trip.getTripName());
        holder.fromAddress.setText("From\n" + trip.getFromAddress());
        holder.toAddress.setText("To\n" + trip.getToAddress());

        holder.tripCard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTripClick(trip);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Delete Trip")
                    .setMessage("Are you sure you want to delete this trip?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (listener != null) {
                            listener.onTripDelete(trip, position);
                        }
                    })
                    .setNegativeButton("No", null) // Cancel deletion
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public void removeItem(int position) {
        tripList.remove(position);
        notifyItemRemoved(position);
    }

    static class SavedTripViewHolder extends RecyclerView.ViewHolder {
        TextView tripName, fromAddress, toAddress;
        CardView tripCard;
        ImageButton deleteButton;

        public SavedTripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripName = itemView.findViewById(R.id.tripTitle);
            fromAddress = itemView.findViewById(R.id.fromAddress);
            toAddress = itemView.findViewById(R.id.toAddress);
            tripCard = itemView.findViewById(R.id.tripCard);
            deleteButton = itemView.findViewById(R.id.deleteTripButton);
        }
    }
}
