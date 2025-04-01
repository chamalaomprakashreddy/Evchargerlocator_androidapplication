package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class FilterActivity extends AppCompatActivity {

    private Switch switchLevel1, switchLevel2, switchLevel3;
    private ImageView type1Icon, type2Icon, ccsIcon, chademoIcon;
    private ImageView type1Tick, type2Tick, ccsTick, chademoTick;
    private Switch teslaToggle, chargePointToggle, evgoToggle, electrifyToggle, selectAllToggle;
    private Button applyButton, resetButton;
    private TextView backArrowText, resetText;

    private Set<String> selectedConnectors = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Level switches
        switchLevel1 = findViewById(R.id.switch_level1);
        switchLevel2 = findViewById(R.id.switch_level2);
        switchLevel3 = findViewById(R.id.switch_level3);

        // Connector icons and tick overlays
        type1Icon = findViewById(R.id.type1_icon);
        type2Icon = findViewById(R.id.type2_icon);
        ccsIcon = findViewById(R.id.ccs_icon);
        chademoIcon = findViewById(R.id.chademo_icon);

        type1Tick = findViewById(R.id.type1_tick);
        type2Tick = findViewById(R.id.type2_tick);
        ccsTick = findViewById(R.id.ccs_tick);
        chademoTick = findViewById(R.id.chademo_tick);

        // Network toggles
        teslaToggle = findViewById(R.id.tesla_toggle);
        chargePointToggle = findViewById(R.id.chargepoint_toggle);
        evgoToggle = findViewById(R.id.evgo_toggle);
        electrifyToggle = findViewById(R.id.electrify_toggle);
        selectAllToggle = findViewById(R.id.select_all_toggle);

        // Buttons
        applyButton = findViewById(R.id.button_apply);
        resetButton = findViewById(R.id.button_reset);
        resetText = findViewById(R.id.resetText);
        backArrowText = findViewById(R.id.backArrowText);

        // Back
        backArrowText.setOnClickListener(v -> onBackPressed());

        // Select All Networks
        selectAllToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            teslaToggle.setChecked(isChecked);
            chargePointToggle.setChecked(isChecked);
            evgoToggle.setChecked(isChecked);
            electrifyToggle.setChecked(isChecked);
        });

        // Setup connector toggles
        setupConnectorToggle("Type 1", type1Icon, type1Tick, R.drawable.ic_type1);
        setupConnectorToggle("Type 2", type2Icon, type2Tick, R.drawable.ic_type2);
        setupConnectorToggle("CCS", ccsIcon, ccsTick, R.drawable.ic_css_png);
        setupConnectorToggle("CHAdeMO", chademoIcon, chademoTick, R.drawable.ic_chademo);

        // Reset
        resetText.setOnClickListener(v -> resetFilters());

        // Apply filters
        applyButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();

            resultIntent.putExtra("level1", switchLevel1.isChecked());
            resultIntent.putExtra("level2", switchLevel2.isChecked());
            resultIntent.putExtra("level3", switchLevel3.isChecked());

            resultIntent.putExtra("connectorTypes", selectedConnectors.toArray(new String[0]));

            resultIntent.putExtra("networkTesla", teslaToggle.isChecked());
            resultIntent.putExtra("networkChargePoint", chargePointToggle.isChecked());
            resultIntent.putExtra("networkEVgo", evgoToggle.isChecked());
            resultIntent.putExtra("networkElectrify", electrifyToggle.isChecked());

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        resetButton.setOnClickListener(v -> resetFilters());
    }

    private void setupConnectorToggle(String type, ImageView icon, ImageView tick, int defaultResId) {
        icon.setTag(defaultResId); // Save default icon
        icon.setOnClickListener(v -> {
            if (selectedConnectors.contains(type)) {
                selectedConnectors.remove(type);
                resetConnectorStyle(icon, tick, defaultResId);
            } else {
                selectedConnectors.add(type);
                highlightConnector(icon, tick);
            }
        });
    }

    private void highlightConnector(ImageView icon, ImageView tick) {
        icon.setBackgroundResource(R.drawable.selected_connector_background);
        tick.setVisibility(View.VISIBLE);
    }

    private void resetConnectorStyle(ImageView icon, ImageView tick, int defaultResId) {
        icon.setBackgroundResource(0);
        icon.setImageResource(defaultResId);
        tick.setVisibility(View.GONE);
    }

    private void resetFilters() {
        switchLevel1.setChecked(false);
        switchLevel2.setChecked(false);
        switchLevel3.setChecked(false);

        teslaToggle.setChecked(false);
        chargePointToggle.setChecked(false);
        evgoToggle.setChecked(false);
        electrifyToggle.setChecked(false);
        selectAllToggle.setChecked(false);

        selectedConnectors.clear();

        resetConnectorStyle(type1Icon, type1Tick, R.drawable.ic_type1);
        resetConnectorStyle(type2Icon, type2Tick, R.drawable.ic_type2);
        resetConnectorStyle(ccsIcon, ccsTick, R.drawable.ic_css_png);
        resetConnectorStyle(chademoIcon, chademoTick, R.drawable.ic_chademo);
    }
}
