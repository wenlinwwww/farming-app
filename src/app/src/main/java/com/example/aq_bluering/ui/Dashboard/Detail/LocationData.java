package com.example.aq_bluering.ui.Dashboard.Detail;

import com.google.gson.Gson;

public class LocationData {
    private String response;
    private double[] coordinates;

    public static LocationData fromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, LocationData.class);
    }

    public double getLongitude() {
        if (coordinates != null && coordinates.length >= 2) {
            return coordinates[0];
        }
        return 0.0;
    }

    public double getLatitude() {
        if (coordinates != null && coordinates.length >= 2) {
            return coordinates[1];
        }
        return 0.0;
    }
}

