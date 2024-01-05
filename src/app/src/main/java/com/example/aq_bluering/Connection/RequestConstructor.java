package com.example.aq_bluering.Connection;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RequestConstructor {
    private String userName = null;

    public RequestConstructor(String userName) {
        this.userName = userName;
    }

    public String getJson_fetchEvaporation(String fieldID) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("fieldId", fieldID);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct evaporation json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchFarms() {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct farm json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchFields() {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct field json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchSensors(String fieldID) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("fieldId", fieldID);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct sensors json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchSensorDetail(String sensorID, String fieldName) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            request.put("sensorID", sensorID);
            request.put("fieldName", fieldName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct sensor detail json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchSensorMoisture(String fieldName) {
        String requestJson = null;
        int dateRange = 24 * 60 * 60 * 1000;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            request.put("fieldName", fieldName);
            request.put("dateRange:", String.valueOf(dateRange));
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct moisture json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchZones() {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct zones json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchGateways() {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct gateway json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_CreateFarm(String farmName) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            request.put("farmName", farmName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct farm json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_CreateField(String fieldName, String farmName, ArrayList<LatLng> coordinatesList) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_polygon(coordinatesList);
            request.put("fieldName", fieldName);
            request.put("userName", userName);
            request.put("farmName", farmName);
            request.put("geom", geom);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct field json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_AddGateway(String gatewayId, LatLng point) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_point(point);
            request.put("userName", userName);
            request.put("gatewayId", gatewayId);
            request.put("geom", geom);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct gateway json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_fetchGatewaysNearField(String fieldId) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("username", userName);
            request.put("fieldId", fieldId);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct gateway near a field json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_activatedGateways(ArrayList<String> gatewayIDs) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONArray gatewaysIDsJson = new JSONArray(gatewayIDs);
            request.put("gatewayIds", gatewaysIDsJson);
            ArrayList<Boolean> status = new ArrayList<>();
            for (int i = 0; i < gatewayIDs.size(); i++) {
                status.add(true);
            }
            request.put("status", "true");
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct activated gateways json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_deActivatedGateways(ArrayList<String> gatewayIDs) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONArray gatewaysIDsJson = new JSONArray(gatewayIDs);
            request.put("gatewayIds", gatewaysIDsJson);
            request.put("status", "false");
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct activated gateways json", e.getMessage());
        }
        return requestJson;
    }


    public String getJson_fetchPairedSensor(ArrayList<String> gatewayIDs) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONArray gatewaysIDsJson = new JSONArray(gatewayIDs);
            request.put("gatewayIds", gatewaysIDsJson);
            request.put("userName", userName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct paired sensor json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_add_Version1_sensor(String sensorId, String gatewayId, String fieldId, LatLng latlng) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_point(latlng);
            request.put("sensorId", sensorId);
            request.put("gatewayId", gatewayId);
            request.put("fieldId", fieldId);
            request.put("geom", geom);

            requestJson = request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }
    public String getJson_add_v2_sensor(String sensorId, String fieldId, LatLng latlng) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_point(latlng);
            request.put("userName",userName);
            request.put("sensorId", sensorId);
            request.put("fieldId", fieldId);
            request.put("geom", geom);

            requestJson = request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }
    public String getJson_createZone(String farmName, String fieldName, String zoneName, ArrayList<LatLng> coordinatesList,
                                     String cropType, String soilType25, String soilType75,
                                     String soilType125, double wPoint50, double wPoint100,
                                     double wPoint150, double fCapacity50, double fCapacity100,
                                     double fCapacity150, double saturation50, double saturation100,
                                     double saturation150) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_polygon(coordinatesList);
            //geom
            request.put("userName", userName);
            request.put("farmName", farmName);
            request.put("fieldName", fieldName);
            request.put("zoneName", zoneName);
            request.put("geom", geom);
            request.put("cropType", cropType);
            request.put("soilType25", soilType25);
            request.put("soilType75", soilType75);
            request.put("soilType125", soilType125);
            request.put("wiltingPoint50", wPoint50);
            request.put("wiltingPoint100", wPoint100);
            request.put("wiltingPoint150", wPoint150);
            request.put("fieldCapacity50", fCapacity50);
            request.put("fieldCapacity100", fCapacity100);
            request.put("fieldCapacity150", fCapacity150);
            request.put("saturation50", saturation50);
            request.put("saturation100", saturation100);
            request.put("saturation150", saturation150);

            requestJson = request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }
    public String getJson_editZone(String farmName, String fieldName, String zoneName, ArrayList<LatLng> coordinatesList,
                                     String cropType, String soilType25, String soilType75,
                                     String soilType125, double wPoint50, double wPoint100,
                                     double wPoint150, double fCapacity50, double fCapacity100,
                                     double fCapacity150, double saturation50, double saturation100,
                                     double saturation150,String oldZoneName) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_polygon(coordinatesList);
            //geom
            request.put("userName", userName);
            request.put("farmName", farmName);
            request.put("fieldName", fieldName);
            request.put("zoneName", zoneName);
            request.put("geom", geom);
            request.put("cropType", cropType);
            request.put("soilType25", soilType25);
            request.put("soilType75", soilType75);
            request.put("soilType125", soilType125);
            request.put("wiltingPoint50", wPoint50);
            request.put("wiltingPoint100", wPoint100);
            request.put("wiltingPoint150", wPoint150);
            request.put("fieldCapacity50", fCapacity50);
            request.put("fieldCapacity100", fCapacity100);
            request.put("fieldCapacity150", fCapacity150);
            request.put("saturation50", saturation50);
            request.put("saturation100", saturation100);
            request.put("saturation150", saturation150);
            request.put("oldZoneName",oldZoneName);

            requestJson = request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }

    private JSONObject convertGeomJson_polygon(ArrayList<LatLng> coordinatesList) {
        JSONObject geom = new JSONObject();
        try {
            List<List<Double>> coordinates = new ArrayList<>();
            for (LatLng latlng : coordinatesList) {
                List<Double> coordinate = new ArrayList<>();
                coordinate.add(latlng.longitude);
                coordinate.add(latlng.latitude);
                coordinates.add(coordinate);
            }
            List<List<List<Double>>> coordinatesFinal = new ArrayList<>();
            coordinatesFinal.add(coordinates);
            JSONArray jsonArray = new JSONArray(coordinatesFinal);
            JSONObject geometry = new JSONObject();
            geometry.put("type", "polygon");
            geometry.put("coordinates", jsonArray);
            geom.put("type", "Feature");
            geom.put("geometry", geometry);
            geom.put("properties", new JSONObject());
        } catch (Exception e) {
            Log.e("Error in construct geom json object", e.getMessage());
        }

        return geom;
    }

    private JSONObject convertGeomJson_point(LatLng latlng) {
        JSONObject geom = new JSONObject();
        try {
            List<Double> coordinate = new ArrayList<>();
            coordinate.add(latlng.longitude);
            coordinate.add(latlng.latitude);
            JSONArray coordinateJson = new JSONArray(coordinate);
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", coordinateJson);
            geom.put("type", "Feature");
            geom.put("geometry", geometry);
            geom.put("properties", new JSONObject());
        } catch (Exception e) {
            Log.e("Error in construct geom json object", e.getMessage());
        }

        return geom;
    }


    public String getUrl_delete_farm(String farmName) {
        String url = null;
        if (userName != null && farmName != null) {
            url = "https://webapp.aquaterra.cloud/api/farm/" + userName + "/" + farmName;
        }
        return url;
    }

    public String getUrl_delete_field(String fieldID) {
        String url = null;
        if (userName != null && fieldID != null) {
            url = "https://webapp.aquaterra.cloud/api/field/" + fieldID;
        }
        return url;
    }

    public String getUrl_delete_sensor(String sensorID) {
        String url = null;
        if (userName != null && sensorID != null) {
            url = "https://webapp.aquaterra.cloud/api/sensor/" + sensorID;
        }
        return url;
    }

    public String getJson_delete_gateway(String gatewayId) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            request.put("gatewayId", gatewayId);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct delete gateways json", e.getMessage());
        }
        return requestJson;
    }

    public String getJson_delete_zone(String fieldName, String zoneName) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            request.put("userName", userName);
            request.put("fieldName", fieldName);
            request.put("zoneName", zoneName);
            requestJson = request.toString();
        } catch (Exception e) {
            Log.e("Error in construct delete zone json", e.getMessage());
        }
        return requestJson;
    }
    public LatLng parseCoordinateFromJson(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray coordArray = jsonObject.getJSONArray("coordinates");
            double lon = coordArray.getDouble(1); // Longitude is usually the first value
            double lat = coordArray.getDouble(0); // Latitude is usually the second value
            return new LatLng(lat, lon);
        } catch (JSONException e) {
            Log.e("SensorEditing", "Error parsing JSON for coordinates.", e);
            return null;
        }
    }

    public String getUrl_delete_gateway() {
        String url = "https://webapp.aquaterra.cloud/api/gateway/delete";
        return url;
    }

    public String getUrl_delete_zone() {
        String url = "https://webapp.aquaterra.cloud/api/zone/deleteZone";
        return url;
    }

    public String getJson_editSensor(LatLng coordinate, boolean isActive, int sleeping, String alias) {
        String requestJson = null;
        try {
            JSONObject request = new JSONObject();
            JSONObject geom = convertGeomJson_point(coordinate);
            //geom
            request.put("geom", geom.toString());
            request.put("is_active", isActive);
            request.put("sleeping", sleeping);
            request.put("alias", alias);



            requestJson = request.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestJson;
    }
}
