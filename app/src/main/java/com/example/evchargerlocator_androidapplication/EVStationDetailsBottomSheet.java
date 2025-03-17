package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class EVStationDetailsBottomSheet extends BottomSheetDialogFragment {

    private TextView stationName, stationAddress, distance, accessTime, paymentMethods, connectorType, chargingLevel;
    private Button startChargingButton, navigateButton;
    private String latitude, longitude;

    public static EVStationDetailsBottomSheet newInstance(String name, String address, String distance, String access, String payment, String connector, String level, String lat, String lon) {
        EVStationDetailsBottomSheet fragment = new EVStationDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("address", address);
        args.putString("distance", distance);
        args.putString("access", access);
        args.putString("payment", payment);
        args.putString("connector", connector);
        args.putString("level", level);
        args.putString("lat", lat);
        args.putString("lon", lon);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ✅ Fix: Inflate using View, not BottomSheetDialog
        View view = inflater.inflate(R.layout.bottom_sheet_ev_station, container, false);

        stationName = view.findViewById(R.id.station_name);
        stationAddress = view.findViewById(R.id.station_address);
        distance = view.findViewById(R.id.station_distance);
        accessTime = view.findViewById(R.id.access_time);
        paymentMethods = view.findViewById(R.id.payment_methods);
        connectorType = view.findViewById(R.id.connector_type);
        chargingLevel = view.findViewById(R.id.charging_level);
        startChargingButton = view.findViewById(R.id.start_charging_button);
        navigateButton = view.findViewById(R.id.navigate_button);

        if (getArguments() != null) {
            stationName.setText(getArguments().getString("name"));
            stationAddress.setText(getArguments().getString("address"));
            distance.setText("Distance: " + getArguments().getString("distance"));
            accessTime.setText("Access Time: " + getArguments().getString("access"));
            paymentMethods.setText("Payment: " + getArguments().getString("payment"));
            connectorType.setText("Connector: " + getArguments().getString("connector"));
            chargingLevel.setText("Charging Level: " + getArguments().getString("level"));
            latitude = getArguments().getString("lat");
            longitude = getArguments().getString("lon");
        }

        navigateButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        startChargingButton.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}
