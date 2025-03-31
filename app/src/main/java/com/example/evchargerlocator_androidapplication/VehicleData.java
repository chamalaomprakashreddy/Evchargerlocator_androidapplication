package com.example.evchargerlocator_androidapplication;

import java.util.HashMap;
import java.util.Map;

public class VehicleData {

    private static final Map<String, VehicleModel> vehicleMap = new HashMap<>();

    static {
        vehicleMap.put("Tesla", new VehicleModel("Tesla", 60, 430, 250));
        vehicleMap.put("Audi", new VehicleModel("Audi", 83, 405, 150));
        vehicleMap.put("BMW", new VehicleModel("BMW", 80, 480, 150));
        vehicleMap.put("Lamborghini", new VehicleModel("Lamborghini", 40, 220, 50));
        vehicleMap.put("Generic", new VehicleModel("Generic", 50, 300, 100)); // Default fallback
    }

    public static VehicleModel getVehicleByName(String vehicleName) {
        return vehicleMap.getOrDefault(vehicleName, vehicleMap.get("Generic"));
    }
}
