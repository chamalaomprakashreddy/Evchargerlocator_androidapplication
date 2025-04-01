package com.example.evchargerlocator_androidapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.evchargerlocator_androidapplication.StationDetailsActivity;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HomePageActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap myMap;
    private RequestQueue requestQueue;
    private DatabaseReference databaseReference;

    private LatLng startLocation, endLocation;
    private double distanceFilter = 5.0;
    private Polyline routePolyline;
    private List<LatLng> routePoints;
    private List<ChargingStation> selectedStations = new ArrayList<>();

    private TextView distanceStat, timeStat, stationStat;
    private boolean filterLevel1 = false, filterLevel2 = false, filterLevel3 = false;
    private Set<String> filterConnectors = new HashSet<>();
    private Set<String> filterNetworks = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        distanceStat = findViewById(R.id.distanceStat);
        timeStat = findViewById(R.id.timeStat);
        stationStat = findViewById(R.id.stationStat);
        requestQueue = Volley.newRequestQueue(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("ChargingStations");

        Intent intent = getIntent();
        startLocation = getLatLngFromIntent(intent.getStringExtra("startLocation"));
        endLocation = getLatLngFromIntent(intent.getStringExtra("endLocation"));
        distanceFilter = intent.getDoubleExtra("distanceFilter", 5.0);

        // ✅ Only apply filters if passed via Intent
        if (intent.hasExtra("level1") || intent.hasExtra("connectorTypes") || intent.hasExtra("networkTesla")) {
            filterLevel1 = intent.getBooleanExtra("level1", false);
            filterLevel2 = intent.getBooleanExtra("level2", false);
            filterLevel3 = intent.getBooleanExtra("level3", false);

            String[] connectorTypes = intent.getStringArrayExtra("connectorTypes");
            if (connectorTypes != null) filterConnectors.addAll(Arrays.asList(connectorTypes));

            if (intent.getBooleanExtra("networkTesla", false)) filterNetworks.add("Tesla");
            if (intent.getBooleanExtra("networkChargePoint", false)) filterNetworks.add("ChargePoint");
            if (intent.getBooleanExtra("networkEVgo", false)) filterNetworks.add("EVgo");
            if (intent.getBooleanExtra("networkElectrify", false)) filterNetworks.add("Electrify America");
        }

        // Vehicle info for smart planning
        String vehicleType = intent.getStringExtra("vehicleType");
        int batteryPercent = intent.getIntExtra("batteryPercent", 100);

        // Restore trip if reopening
        if (intent.getBooleanExtra("restoreTrip", false)) {
            selectedStations = intent.getParcelableArrayListExtra("stations");

            updateTripBadge();
            updateStationStat();
        }

        // Always draw route if valid
        if (startLocation != null && endLocation != null) {
            drawRouteAndEVStations();
        }

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Trip Badge click
        FrameLayout tripBadge = findViewById(R.id.tripBadge);
        tripBadge.setOnClickListener(v -> {
            Intent tripIntent = new Intent(HomePageActivity.this, MyTripActivity.class);
            tripIntent.putParcelableArrayListExtra("stations", new ArrayList<>(selectedStations));

            if (startLocation != null)
                tripIntent.putExtra("startLocation", startLocation.latitude + "," + startLocation.longitude);
            if (endLocation != null)
                tripIntent.putExtra("endLocation", endLocation.latitude + "," + endLocation.longitude);

            Geocoder geocoder = new Geocoder(HomePageActivity.this, Locale.getDefault());
            try {
                String fromAddress = "", toAddress = "";
                if (startLocation != null) {
                    List<Address> startAddrs = geocoder.getFromLocation(startLocation.latitude, startLocation.longitude, 1);
                    if (!startAddrs.isEmpty()) fromAddress = startAddrs.get(0).getAddressLine(0);
                }
                if (endLocation != null) {
                    List<Address> endAddrs = geocoder.getFromLocation(endLocation.latitude, endLocation.longitude, 1);
                    if (!endAddrs.isEmpty()) toAddress = endAddrs.get(0).getAddressLine(0);
                }
                tripIntent.putExtra("fromAddress", fromAddress);
                tripIntent.putExtra("toAddress", toAddress);
            } catch (Exception e) {
                tripIntent.putExtra("fromAddress", "");
                tripIntent.putExtra("toAddress", "");
            }

            startActivity(tripIntent);
        });

        // Save trip button
        ImageButton saveTripButton = findViewById(R.id.saveTripButton);
        saveTripButton.setOnClickListener(v -> showSaveTripDialog());

        // Filter button
        ImageButton filterBtn = findViewById(R.id.filter);
        filterBtn.setOnClickListener(v -> {
            Intent filterIntent = new Intent(HomePageActivity.this, FilterActivity.class);
            startActivityForResult(filterIntent, 101);
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        if (startLocation != null && endLocation != null) {
            drawRouteAndEVStations();

            // ✅ Auto-zoom to fit route and markers
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(startLocation);
            boundsBuilder.include(endLocation);

            if (selectedStations != null && !selectedStations.isEmpty()) {
                for (ChargingStation station : selectedStations) {
                    boundsBuilder.include(new LatLng(station.getLatitude(), station.getLongitude()));
                }
            }

            LatLngBounds bounds = boundsBuilder.build();

            myMap.setOnMapLoadedCallback(() -> {
                int padding = 100; // padding in pixels
                myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            });
        }
    }

    private void drawRouteAndEVStations() {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + startLocation.latitude + "," + startLocation.longitude
                + "&destination=" + endLocation.latitude + "," + endLocation.longitude
                + "&key=" + getString(R.string.google_maps_key);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            String polyline = route.getJSONObject("overview_polyline").getString("points");
                            routePoints = PolyUtil.decode(polyline);

                            if (routePolyline != null) routePolyline.remove();
                            routePolyline = myMap.addPolyline(new PolylineOptions()
                                    .addAll(routePoints)
                                    .width(10)
                                    .color(ContextCompat.getColor(this, R.color.black)));

                            JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                            distanceStat.setText("🚗 " + leg.getJSONObject("distance").getString("text"));
                            timeStat.setText("⏱ " + leg.getJSONObject("duration").getString("text"));

                            fetchEVStationsAlongRoute(routePoints);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing route", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to fetch route", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void fetchEVStationsAlongRoute(List<LatLng> routePoints) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot stationSnap : snapshot.getChildren()) {
                    ChargingStation station = stationSnap.getValue(ChargingStation.class);
                    if (station == null) continue;

                    LatLng loc = new LatLng(station.getLatitude(), station.getLongitude());
                    boolean isOnRoute = PolyUtil.isLocationOnPath(loc, routePoints, true, distanceFilter * 1609.34);

                    if (isOnRoute) {
                        Intent intent = getIntent();
                        String vehicleType = intent.getStringExtra("vehicleType");
                        int batteryPercent = intent.getIntExtra("batteryPercent", 100);
                        VehicleModel vehicle = VehicleData.getVehicleByName(vehicleType);
                        double usableRangeKm = (batteryPercent / 100.0) * vehicle.maxRangeKm;

                        float[] result = new float[1];
                        Location.distanceBetween(startLocation.latitude, startLocation.longitude,
                                station.getLatitude(), station.getLongitude(), result);
                        float distanceFromStartKm = result[0] / 1000f;

                        if (distanceFromStartKm >= usableRangeKm) {

                            // Apply filters only if intent contains any filter keys
                            boolean shouldApplyFilter = intent.hasExtra("level1") || intent.hasExtra("connectorTypes") || intent.hasExtra("networkTesla");

                            boolean matchesLevel = true;
                            boolean matchesConnector = true;
                            boolean matchesNetwork = true;

                            if (shouldApplyFilter) {
                                String level = station.getChargingLevel().toLowerCase();
                                String connector = station.getConnectorType();
                                String network = station.getNetwork();

                                matchesLevel =
                                        (filterLevel1 && level.contains("level 1")) ||
                                                (filterLevel2 && level.contains("level 2")) ||
                                                (filterLevel3 && level.contains("dc"));

                                matchesConnector = filterConnectors.isEmpty() || filterConnectors.contains(connector);
                                matchesNetwork = filterNetworks.isEmpty() || filterNetworks.contains(network);
                            }

                            if (matchesLevel && matchesConnector && matchesNetwork) {
                                // Set marker color
                                float markerColor;
                                String level = station.getChargingLevel().toLowerCase();
                                switch (level) {
                                    case "level 1": markerColor = BitmapDescriptorFactory.HUE_YELLOW; break;
                                    case "level 2": markerColor = BitmapDescriptorFactory.HUE_GREEN; break;
                                    case "dc fast": markerColor = BitmapDescriptorFactory.HUE_BLUE; break;
                                    default: markerColor = BitmapDescriptorFactory.HUE_RED; break;
                                }

                                myMap.addMarker(new MarkerOptions()
                                        .position(loc)
                                        .title(station.getName())
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                                count++;
                            } else {
                                Log.d("FILTER", "Skipping " + station.getName() + " due to filter mismatch.");
                            }

                        } else {
                            Log.d("SMART_PLAN", "Skipping " + station.getName() + " — " + distanceFromStartKm + " km from start");
                        }
                    }
                }

                stationStat.setText("📍 " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d("MARKER_CLICK", "Marker clicked: " + marker.getTitle());

        if (marker.getTitle() == null) return false;

        LatLng position = marker.getPosition();
        String stationName = marker.getTitle();

        // Construct ChargingStation object from marker
        ChargingStation clickedStation = new ChargingStation();
        clickedStation.setName(stationName);
        clickedStation.setLatitude(position.latitude);
        clickedStation.setLongitude(position.longitude);
        clickedStation.setChargingLevel("Level 2");
        clickedStation.setConnectorType("J1772");
        clickedStation.setNetwork("ChargePoint");
        clickedStation.setPowerOutput("7.2 kW");
        clickedStation.setAvailability("2/2 Available");
        clickedStation.setAvailablePorts(2);
        clickedStation.setTotalPorts(2);

        // Determine reference point for distance
        LatLng referencePoint;
        if (selectedStations.isEmpty()) {
            referencePoint = startLocation;
        } else {
            ChargingStation last = selectedStations.get(selectedStations.size() - 1);
            referencePoint = new LatLng(last.getLatitude(), last.getLongitude());
        }

        // Compute distance & time
        double distance = SphericalUtil.computeDistanceBetween(referencePoint, position) / 1609.34;
        String distanceText = String.format(Locale.getDefault(), "%.2f Mi", distance);
        String driveTimeText = estimateDriveTime(distance);

        // Show popup
        findViewById(R.id.stationPopup).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.stationDistanceText)).setText("Distance: " + distanceText);
        ((TextView) findViewById(R.id.popupStationDistance)).setText(distanceText);
        ((TextView) findViewById(R.id.popupStationName)).setText(stationName);
        ((TextView) findViewById(R.id.popupStationDetails)).setText("EV Charging Station");

        // Trip toggle logic
        Button tripToggleBtn = findViewById(R.id.addToTripButton);
        updateTripButtonState(tripToggleBtn, clickedStation);
        tripToggleBtn.setOnClickListener(v -> {
            boolean isInTrip = isStationInTrip(clickedStation);

            if (isInTrip) {
                selectedStations.removeIf(s ->
                        s.getLatitude() == clickedStation.getLatitude() &&
                                s.getLongitude() == clickedStation.getLongitude() &&
                                s.getName().equals(clickedStation.getName())
                );
                Toast.makeText(this, "Removed from trip", Toast.LENGTH_SHORT).show();
            } else {
                selectedStations.add(clickedStation);
                Toast.makeText(this, "Added to trip", Toast.LENGTH_SHORT).show();
            }

            updateTripBadge();
            updateStationStat();
            updateTripButtonState(tripToggleBtn, clickedStation);
        });

        // Details button logic
        Button detailsBtn = findViewById(R.id.viewStationDetails);
        detailsBtn.setOnClickListener(v -> {
            Log.d("DETAILS_BTN", "Opening StationDetailsActivity...");

            Intent detailsIntent = new Intent(HomePageActivity.this, StationDetailsActivity.class);

            // Required fields
            detailsIntent.putExtra("stationName", clickedStation.getName());
            detailsIntent.putExtra("paymentMethods", "Google Pay, PayPal");
            detailsIntent.putExtra("plugType", clickedStation.getConnectorType());
            detailsIntent.putExtra("plugPrice", "$0.40/kWh");
            detailsIntent.putExtra("plugAvailability", clickedStation.getAvailability());

            // Already calculated distance/time (from marker popup or routing logic)
            detailsIntent.putExtra("distance", distanceText);   // e.g., "2.4 mi"
            detailsIntent.putExtra("duration", driveTimeText);  // e.g., "5 min"

            // Latitude/Longitude for reverse geocoding
            detailsIntent.putExtra("latitude", clickedStation.getLatitude());
            detailsIntent.putExtra("longitude", clickedStation.getLongitude());

            Log.d("DETAILS_BTN", "Intent data: " + clickedStation.getName() + ", " + clickedStation.getConnectorType());

            startActivity(detailsIntent);
        });



        return true;
    }


    private String estimateDriveTime(double miles) {
        double avgSpeed = 30.0; // mph
        double minutes = (miles / avgSpeed) * 60;
        return String.format(Locale.getDefault(), "%.0f min", minutes);
    }




    private void updateTripBadge() {
        ((TextView) findViewById(R.id.badgeCount)).setText(String.valueOf(selectedStations.size()));
    }

    private void updateStationStat() {
        ((TextView) findViewById(R.id.stationStat)).setText("📍 " + selectedStations.size());
    }

    private boolean isStationInTrip(ChargingStation station) {
        for (ChargingStation s : selectedStations) {
            if (s.getLatitude() == station.getLatitude()
                    && s.getLongitude() == station.getLongitude()
                    && s.getName().equals(station.getName())) {
                return true;
            }
        }
        return false;
    }

    private void updateTripButtonState(Button button, ChargingStation station) {
        if (isStationInTrip(station)) {
            button.setText("Remove from Trip");
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_remove_circle, 0, 0, 0);
        } else {
            button.setText("Add to Trip");
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0);
        }
    }
    private LatLng getLatLngFromIntent(String str) {
        if (str == null || !str.contains(",")) return null;
        String[] parts = str.split(",");
        return new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    private void showSaveTripDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_trip, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView).setCancelable(false).create();

        EditText tripNameInput = dialogView.findViewById(R.id.tripNameInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String tripName = tripNameInput.getText().toString().trim();
            if (tripName.isEmpty()) {
                tripNameInput.setError("Trip name is required");
                return;
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                String fromAddress = getAddressFromLatLng(startLocation, geocoder);
                String toAddress = getAddressFromLatLng(endLocation, geocoder);

                SavedTrip trip = new SavedTrip(tripName, fromAddress, toAddress,
                        startLocation.latitude + "," + startLocation.longitude,
                        endLocation.latitude + "," + endLocation.longitude,
                        selectedStations);

                FirebaseDatabase.getInstance().getReference("SavedTrips")
                        .push().setValue(trip)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Trip saved!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save trip", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Toast.makeText(this, "Error saving trip", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private String getAddressFromLatLng(LatLng latLng, Geocoder geocoder) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) return addresses.get(0).getAddressLine(0);
        } catch (Exception ignored) {}
        return latLng.latitude + ", " + latLng.longitude;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            boolean level1 = data.getBooleanExtra("level1", false);
            boolean level2 = data.getBooleanExtra("level2", false);
            boolean level3 = data.getBooleanExtra("level3", false);

            String[] connectors = data.getStringArrayExtra("connectorTypes");
            boolean netTesla = data.getBooleanExtra("networkTesla", false);
            boolean netChargePoint = data.getBooleanExtra("networkChargePoint", false);
            boolean netEVgo = data.getBooleanExtra("networkEVgo", false);
            boolean netElectrify = data.getBooleanExtra("networkElectrify", false);

            Intent refreshed = new Intent(HomePageActivity.this, HomePageActivity.class);
            refreshed.putExtra("startLocation", startLocation.latitude + "," + startLocation.longitude);
            refreshed.putExtra("endLocation", endLocation.latitude + "," + endLocation.longitude);
            refreshed.putExtra("vehicleType", getIntent().getStringExtra("vehicleType"));
            refreshed.putExtra("batteryPercent", getIntent().getIntExtra("batteryPercent", 100));
            refreshed.putExtra("distanceFilter", distanceFilter);

            refreshed.putExtra("level1", level1);
            refreshed.putExtra("level2", level2);
            refreshed.putExtra("level3", level3);
            refreshed.putExtra("connectorTypes", connectors);
            refreshed.putExtra("networkTesla", netTesla);
            refreshed.putExtra("networkChargePoint", netChargePoint);
            refreshed.putExtra("networkEVgo", netEVgo);
            refreshed.putExtra("networkElectrify", netElectrify);

            startActivity(refreshed);
            finish();
        }
    }

}
