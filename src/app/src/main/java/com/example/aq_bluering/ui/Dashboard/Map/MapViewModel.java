package com.example.aq_bluering.ui.Dashboard.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

public class MapViewModel extends ViewModel {

    private GoogleMap googleMap;

    private RequestConstructor rc;
    private ArrayList<Marker> marker_list = new ArrayList<>();

    private MutableLiveData<String> sensorResponse = new MutableLiveData<>();

    private MutableLiveData<String> sensorDetail = new MutableLiveData<>();
    private MutableLiveData<String> farmResponse = new MutableLiveData<>();
    private MutableLiveData<String> fieldResponse = new MutableLiveData<>();
    private MutableLiveData<String> zoneResponse = new MutableLiveData<>();

    //contain list of farm name for current user
    public LiveData<String> getFarmResponse() {
        return farmResponse;
    }

    //contain list of field for a given farm
    public LiveData<String> getFieldResponse() {
        return fieldResponse;
    }

    public LiveData<String> getZoneResponse() {
        return zoneResponse;
    }

    //response contain sensor list in a given field with location info
    public LiveData<String> getSensorResponse() {
        return sensorResponse;
    }

    //response contain battery, temp, moisture
    public LiveData<String> getSensorDetail() {
        return sensorDetail;
    }

    public MapViewModel() {}
    public void setUsername(String username){
        this.rc= new RequestConstructor(username);
    }

    public RequestConstructor getRequestConstructor(){
        return this.rc;
    }

    public void setMap(MapView mapView) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            //when loading map
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //close all marker's information window when click on map
                        for (Marker marker : marker_list) {
                            marker.hideInfoWindow();
                        }
                    }
                });
                init_map_setting();
            }
        });

    }

    private void init_map_setting() {
        //enable map function of zoom in/out, zoom controller, and rotation
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.setPadding(0, 0, 0, 100);
        //set map display as SATELLITE view
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    public void clearMap() {
        googleMap.clear();
        marker_list.clear();
    }

    public void clearMarkers() {
        if (!marker_list.isEmpty()) {
            for (Marker marker : marker_list) {
                marker.remove();
            }
            marker_list.clear();
        }
    }

    public void setMarker(LatLng coordinates, String title, String snippet, int color) {
        if (!(coordinates == null)) {
            Marker mk = googleMap.addMarker(new MarkerOptions().
                    position(coordinates)
                    .title(title)
                    .snippet(snippet)
                    .icon(getMarkerIcon(color)));
            marker_list.add(mk);
            googleMap.setOnMarkerClickListener(marker -> {
                if (!mk.isInfoWindowShown()) {
                    mk.showInfoWindow();
                }
                return false;
            });
        }
    }

    private BitmapDescriptor getMarkerIcon(int color) {
        // Create a new GradientDrawable
        GradientDrawable circleShape = new GradientDrawable();
        circleShape.setShape(GradientDrawable.OVAL);
        circleShape.setColor(color);
        int width = 70;
        int height = 70;
        circleShape.setBounds(0, 0, 70, 70);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // Ensure that the intrinsic width and height are greater than 0
        Canvas canvas = new Canvas(bitmap);
        circleShape.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //pick suitable zoom in level and move to central of the polygon
    private void adjustCameraToCentral(ArrayList<LatLng> coordinates) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : coordinates) {
            builder.include(point);
        }
        int padding = 50;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    public void setMyLocationButton(ArrayList<LatLng> coordinates) {
        adjustCameraToCentral(coordinates);
    }

    //type 0 for draw field, type 1 for draw irrigation zone
    public void drawPolygonOnMap(ArrayList<LatLng> coordinateList, int type) {
        if (!coordinateList.isEmpty()) {
            int color = 0;
            if (type == 0) {
                color = Color.RED;
                adjustCameraToCentral(coordinateList);
            } else if (type == 1) {
                color = Color.BLUE;
            }
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(coordinateList)
                    .strokeWidth(5)
                    .strokeColor(color);
            googleMap.addPolygon(polygonOptions);
        }
    }

    public void fetchSensorDetail(String sensorId, String fieldName) {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchSensorDetail(sensorId,fieldName);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/moisture", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {

                sensorDetail.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }

    public void fetchZoneDetail() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchZones();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/zone", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                zoneResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }

    //methods to obtain data for menu
    public void fetchFarmList() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchFarms();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/farm", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                farmResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }

    public void fetchFieldList() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchFields();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                fieldResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }

    public void fetchSensorList(String fieldID) {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchSensors(fieldID);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                sensorResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }
    //end here

}