package com.example.aq_bluering.Connection;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    public String getValue(String json, String endpoint) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        if (dataArray.isEmpty()) {
            return "no data temporarily";
        } else {
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            return dataObject.get(endpoint).getAsString();
        }
    }

    public List<String> getValues(String json, String endpoint) {
        Gson gson = new Gson();
        List<String> dataValues = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        for (JsonElement element : dataArray) {
            JsonObject dataObject = element.getAsJsonObject();
            dataValues.add(dataObject.get(endpoint).getAsString());
        }
        return dataValues;
    }

    public boolean inValidJson(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        if (dataArray.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public String getMultipleValue(String json, String checkWord, String value, String endpoint) {
        String result = null;
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        try{
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            for (JsonElement element : dataArray) {
                JsonObject dataObject = element.getAsJsonObject();
                String current = dataObject.get(checkWord).getAsString();

                if (current.equals(value)) {
                    JsonElement aliasElement = dataObject.get(endpoint);
                    if (aliasElement != null && !aliasElement.isJsonNull()) {
                        result= aliasElement.getAsString();
                    }
                }
            }
        } catch (Exception e){
            Log.e("error in json parser", e.getMessage());
        }

        // if no match return null
        return result;
    }
    public double getDoubleValue(String json, String checkWord, String value, String endpoint) {
        double result=0;
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        try{
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            for (JsonElement element : dataArray) {
                JsonObject dataObject = element.getAsJsonObject();
                String current = dataObject.get(checkWord).getAsString();
                if (current.equals(value)) {
                    JsonElement aliasElement = dataObject.get(endpoint);
                    if (aliasElement != null && !aliasElement.isJsonNull()) {
                        result= aliasElement.getAsDouble();
                    }
                }
            }
        }catch (Exception e){
            Log.e("json parser error", "not a double number");
        }
        return result;
    }
    public ArrayList<String> getValueList(String json, String endpoint) {
        ArrayList<String> valueList = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        try {
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            for (JsonElement element : dataArray) {
                JsonObject dataObject = element.getAsJsonObject();
                String current = dataObject.get(endpoint).getAsString();
                valueList.add(current);
            }
        } catch (Exception e) {
            Log.e("error when get list from json", e.getMessage());
        }
        return valueList;
    }

    public ArrayList<String> getValueListByName(String json, String checkWord, String value, String endpoint) {
        ArrayList<String> valueList = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        for (JsonElement element : dataArray) {
            JsonObject dataObject = element.getAsJsonObject();
            String current = dataObject.get(checkWord).getAsString();
            if (current.equals(value)) {
                JsonElement aliasElement = dataObject.get(endpoint);
                if (aliasElement != null && !aliasElement.isJsonNull()) {
                    valueList.add(aliasElement.getAsString());
                }
            }
        }
        return valueList;
    }

    public LatLng getCoordinate_moisture(String jsonString) {
        LatLng coordinate = null;
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        if (!dataArray.isEmpty()) {
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            Double lat = dataObject.get("latitude").getAsDouble();
            Double lng = dataObject.get("longitude").getAsDouble();

            coordinate = new LatLng(lat, lng);
        }

        return coordinate;
    }

    public ArrayList<LatLng> getCoordinates(String jsonString) {
        ArrayList<LatLng> coordinateList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String type = jsonObject.getString("type");
            if (type.equals("Polygon")) {
                JSONArray coordinatesJson = jsonObject.getJSONArray("coordinates");
                JSONArray coordinateSet = coordinatesJson.getJSONArray(0);
                for (int i = 0; i < coordinateSet.length(); i++) {
                    JSONArray coordinatePair = coordinateSet.getJSONArray(i);
                    double latitude = coordinatePair.getDouble(1);
                    double longitude = coordinatePair.getDouble(0);
                    LatLng coordinate = new LatLng(latitude, longitude);
                    coordinateList.add(coordinate);
                }

            }
        } catch (Exception e) {
            Log.e("Coordinates Loss", e.getMessage());
        }

        return coordinateList;
    }

    public LatLng getCoordinate(String jsonString) {
        LatLng coordinate = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String type = jsonObject.getString("type");
            if (type.equals("Point")) {
                JSONArray coordinatesJson = jsonObject.getJSONArray("coordinates");
                    double latitude = coordinatesJson.getDouble(1);
                    double longitude = coordinatesJson.getDouble(0);
                     coordinate = new LatLng(latitude, longitude);
            }
        } catch (Exception e) {
            Log.e("Coordinates Loss", e.getMessage());
        }

        return coordinate;
    }

    public ArrayList<String> re_getValueList(String json, String endpoint) {
        ArrayList<String> valueList = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        for (JsonElement element : dataArray) {
            JsonObject dataObject = element.getAsJsonObject();
            String current = null;
            try {
                current = dataObject.get(endpoint).getAsString();
            } catch (Exception e) {
                Log.e("error when get list from json", e.getMessage());
                current = "";
            }
            valueList.add(current);
        }
        return valueList;
    }

    public ArrayList<String> re_getValueListByName(String json, String checkWord, String value, String endpoint) {
        ArrayList<String> valueList = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        for (JsonElement element : dataArray) {
            JsonObject dataObject = element.getAsJsonObject();
            try {
                String current = dataObject.get(checkWord).getAsString();
                if (current.equals(value)) {
                    String result = null;
                    try {
                        result = dataObject.get(endpoint).getAsString();
                    } catch (Exception e) {
                        Log.e("No value when get list from json", e.getMessage());
                        result = "";
                    }
                    valueList.add(result);
                }
            } catch (Exception e) {
                Log.e("No value for check word", e.getMessage());
            }
        }

        return valueList;
    }

    // json: response json string; checkWord: filter data by which attribute (e.g. sensor_id, farm_name, etc)
    // value: what value is used to filter data (e.g. when enter farm_name for checkWord, and "demo" for value, function
    // return all the sensors contained in demo farm)
    //endpoint: which value you want to obtain (e.g. battery_vol, cap50, etc.)
    //e.g. extractDataByNameAndTimeRange(json, "sensor_id","AFA00000DEMO49","battery_vol) will return
    //all battery information for sensor with ID = AFA00000DEMO49
    public ArrayList<String> extractLastTenData(String json, String checkWord, String value, String endpoint) {
        ArrayList<String> valueList = new ArrayList<>();
        ArrayList<String> results = new ArrayList<>();
        Gson gson = new Gson();
        try {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            JsonArray dataArray = jsonObject.getAsJsonArray("data");
            for (JsonElement element : dataArray) {
                JsonObject dataObject = element.getAsJsonObject();
                //get value related to check word for test
                String current = dataObject.get(checkWord).getAsString();
                //get date of this element
                if (current.equals(value)) {
                    String valueElement = dataObject.get(endpoint).getAsString();
                    valueList.add(valueElement);
                }
            }
            if (valueList.size() >= 10) {
                List<String> subList = valueList.subList(valueList.size() - 11, valueList.size() - 1);
                for (String s : subList) {
                    results.add(s);
                }
            } else {
                results = valueList;
            }

        } catch (Exception e) {
            Log.e("Error when parsing response from database", e.getMessage());
        }

        return results;
    }

}
