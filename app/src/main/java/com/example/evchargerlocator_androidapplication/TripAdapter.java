package com.example.evchargerlocator_androidapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripSegmentViewHolder> {

    private List<TripSegment> tripSegments;
    private OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemove(TripSegment segment);
    }

    public TripAdapter(List<TripSegment> tripSegments, OnRemoveClickListener listener) {
        this.tripSegments = tripSegments != null ? tripSegments : new ArrayList<>();
        this.removeClickListener = listener;
    }

    @NonNull
    @Override
    public TripSegmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_segment, parent, false);
        return new TripSegmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripSegmentViewHolder holder, int position) {
        TripSegment segment = tripSegments.get(position);
        holder.stationName.setText(segment.getStationName());
        holder.distanceText.setText(String.format("%.2f mi", segment.getDistance()));

        holder.removeBtn.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemove(segment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripSegments.size();
    }

    public static class TripSegmentViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, distanceText;
        Button removeBtn;

        public TripSegmentViewHolder(@NonNull View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.stationName);
            distanceText = itemView.findViewById(R.id.stationDistance);
            removeBtn = itemView.findViewById(R.id.removeStation);
        }
    }

    // 🔄 Updated method to replace current trip segments
    public void updateTripSegments(List<TripSegment> newSegments) {
        this.tripSegments.clear();
        this.tripSegments.addAll(newSegments);
        notifyDataSetChanged();
    }
}
