package com.example.evchargerlocator_androidapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
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
    private static final Set<String> DEFAULT_CONNECTORS = new HashSet<>(Arrays.asList("Type 1", "Type 2", "CCS", "CHAdeMO"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Initialize views
        switchLevel1 = findViewById(R.id.switch_level1);
        switchLevel2 = findViewById(R.id.switch_level2);
        switchLevel3 = findViewById(R.id.switch_level3);

        type1Icon = findViewById(R.id.type1_icon);
        type2Icon = findViewById(R.id.type2_icon);
        ccsIcon = findViewById(R.id.ccs_icon);
        chademoIcon = findViewById(R.id.chademo_icon);

        type1Tick = findViewById(R.id.type1_tick);
        type2Tick = findViewById(R.id.type2_tick);
        ccsTick = findViewById(R.id.ccs_tick);
        chademoTick = findViewById(R.id.chademo_tick);

        teslaToggle = findViewById(R.id.tesla_toggle);
        chargePointToggle = findViewById(R.id.chargepoint_toggle);
        evgoToggle = findViewById(R.id.evgo_toggle);
        electrifyToggle = findViewById(R.id.electrify_toggle);
        selectAllToggle = findViewById(R.id.select_all_toggle);

        applyButton = findViewById(R.id.button_apply);
        resetButton = findViewById(R.id.button_reset);
        resetText = findViewById(R.id.resetText);
        backArrowText = findViewById(R.id.backArrowText);

        // Back
        backArrowText.setOnClickListener(v -> onBackPressed());

        // Load saved filters from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("FilterPrefs", MODE_PRIVATE);
        switchLevel1.setChecked(prefs.getBoolean("level1", true));
        switchLevel2.setChecked(prefs.getBoolean("level2", true));
        switchLevel3.setChecked(prefs.getBoolean("level3", true));

        selectedConnectors.addAll(prefs.getStringSet("connectorTypes", DEFAULT_CONNECTORS));
        if (selectedConnectors.contains("Type 1")) highlightConnector(type1Icon, type1Tick);
        if (selectedConnectors.contains("Type 2")) highlightConnector(type2Icon, type2Tick);
        if (selectedConnectors.contains("CCS")) highlightConnector(ccsIcon, ccsTick);
        if (selectedConnectors.contains("CHAdeMO")) highlightConnector(chademoIcon, chademoTick);

        teslaToggle.setChecked(prefs.getBoolean("networkTesla", true));
        chargePointToggle.setChecked(prefs.getBoolean("networkChargePoint", true));
        evgoToggle.setChecked(prefs.getBoolean("networkEVgo", true));
        electrifyToggle.setChecked(prefs.getBoolean("networkElectrify", true));

        selectAllToggle.setChecked(teslaToggle.isChecked() && chargePointToggle.isChecked() && evgoToggle.isChecked() && electrifyToggle.isChecked());

        // Select All Networks logic
        selectAllToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            teslaToggle.setChecked(isChecked);
            chargePointToggle.setChecked(isChecked);
            evgoToggle.setChecked(isChecked);
            electrifyToggle.setChecked(isChecked);
        });

        // Setup connector toggle
        setupConnectorToggle("Type 1", type1Icon, type1Tick, R.drawable.ic_type1);
        setupConnectorToggle("Type 2", type2Icon, type2Tick, R.drawable.ic_type2);
        setupConnectorToggle("CCS", ccsIcon, ccsTick, R.drawable.ic_css_png);
        setupConnectorToggle("CHAdeMO", chademoIcon, chademoTick, R.drawable.ic_chademo);

        // Reset filters
        resetText.setOnClickListener(v -> resetFilters());
        resetButton.setOnClickListener(v -> resetFilters());

        // Apply filters
        applyButton.setOnClickListener(v -> {
            boolean level1 = switchLevel1.isChecked();
            boolean level2 = switchLevel2.isChecked();
            boolean level3 = switchLevel3.isChecked();
            boolean netTesla = teslaToggle.isChecked();
            boolean netChargePoint = chargePointToggle.isChecked();
            boolean netEVgo = evgoToggle.isChecked();
            boolean netElectrify = electrifyToggle.isChecked();

            // Save filters
            SharedPreferences.Editor editor = getSharedPreferences("FilterPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("level1", level1);
            editor.putBoolean("level2", level2);
            editor.putBoolean("level3", level3);
            editor.putStringSet("connectorTypes", selectedConnectors);
            editor.putBoolean("networkTesla", netTesla);
            editor.putBoolean("networkChargePoint", netChargePoint);
            editor.putBoolean("networkEVgo", netEVgo);
            editor.putBoolean("networkElectrify", netElectrify);
            editor.apply();

            // Detect if filters are customized
            boolean customized = !(level1 && level2 && level3
                    && selectedConnectors.containsAll(DEFAULT_CONNECTORS)
                    && selectedConnectors.size() == DEFAULT_CONNECTORS.size()
                    && netTesla && netChargePoint && netEVgo && netElectrify);

            // Return filter state
            Intent resultIntent = new Intent();
            resultIntent.putExtra("level1", level1);
            resultIntent.putExtra("level2", level2);
            resultIntent.putExtra("level3", level3);
            resultIntent.putExtra("connectorTypes", selectedConnectors.toArray(new String[0]));
            resultIntent.putExtra("networkTesla", netTesla);
            resultIntent.putExtra("networkChargePoint", netChargePoint);
            resultIntent.putExtra("networkEVgo", netEVgo);
            resultIntent.putExtra("networkElectrify", netElectrify);
            resultIntent.putExtra("filtersCustomized", customized); // for green dot + status

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setupConnectorToggle(String type, ImageView icon, ImageView tick, int defaultResId) {
        icon.setTag(defaultResId);
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
        switchLevel1.setChecked(true);
        switchLevel2.setChecked(true);
        switchLevel3.setChecked(true);

        teslaToggle.setChecked(true);
        chargePointToggle.setChecked(true);
        evgoToggle.setChecked(true);
        electrifyToggle.setChecked(true);
        selectAllToggle.setChecked(true);

        selectedConnectors.clear();
        selectedConnectors.addAll(DEFAULT_CONNECTORS);

        highlightConnector(type1Icon, type1Tick);
        highlightConnector(type2Icon, type2Tick);
        highlightConnector(ccsIcon, ccsTick);
        highlightConnector(chademoIcon, chademoTick);
    }
}
