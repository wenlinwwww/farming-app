package com.example.aq_bluering.ui.Sensor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.FragmentSensorBinding;
import com.example.aq_bluering.databinding.ProfileFragmentBinding;
import com.example.aq_bluering.databinding.SensorCreateFragmentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import java.util.ArrayList;


public class SensorCreation extends DialogFragment {
    private RequestConstructor rc;
    private SensorCreateFragmentBinding binding;
    private Marker currentMarker = null;
    private MapView mapView;
    private GoogleMap googleMap;
    private String fieldID;
    private ArrayList<String> geom = new ArrayList<>();
    private ArrayList<String> sensorIDs = new ArrayList<>();
    private String newSensorID = null;
    private LatLng newGeom;
    private MutableLiveData<String> addSensorResponse = new MutableLiveData<>();
    Sensor sensor = null;
    private String fieldResponse = null;


    public SensorCreation(RequestConstructor rc, String fieldID, ArrayList<String> geom, ArrayList<String> sensorIDs, Sensor sensor) {
        this.rc = rc;
        this.fieldID = fieldID;
        this.geom = geom;
        this.sensorIDs = sensorIDs;
        this.sensor = sensor;
        this.fieldResponse = sensor.getViewModel().getFieldResponse().getValue();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = SensorCreateFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.backButton.setOnClickListener(view -> dismiss());

        //set map - draw polygon
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        setMap(mapView);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        addSensorResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")) {
                    Toast.makeText(getActivity(), "Sensor Create Successfully!", Toast.LENGTH_SHORT).show();
                    sensor.refresh();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Fail to create new sensor, please try again!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    addSensor();
                }
            }
        });

        return root;
    }

    private boolean isValidInput() {
        if (newGeom == null) {
            Toast.makeText(getActivity(), "Please select a valid location on the map.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String inputSensorId = binding.etSensorId.getText().toString();
        if (inputSensorId == null || inputSensorId.trim().isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a valid Sensor ID.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void addSensor() {
        newSensorID = binding.etSensorId.getText().toString();
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_add_v2_sensor(newSensorID, fieldID, newGeom);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/v2/new", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                addSensorResponse.postValue("success");
            }

            @Override
            public void onError(String errorMessage) {
                addSensorResponse.postValue("fail");
                Log.e("fail create sensor", errorMessage);
            }
        });
    }

    private void setMap(MapView mapView) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
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
        showFieldPolygon();
        showOtherSensor();
        setMapToolBar();
    }

    private void showFieldPolygon() {
        JsonParser jsonParser = new JsonParser();
        String fieldGeomJson = jsonParser.getMultipleValue(fieldResponse,
                "field_id", fieldID, "points");
        ArrayList<LatLng> currentFieldPolygon = jsonParser.getCoordinates(fieldGeomJson);

        if (!currentFieldPolygon.isEmpty()) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(currentFieldPolygon)
                    .strokeWidth(5)
                    .strokeColor(Color.RED);
            googleMap.addPolygon(polygonOptions);
            adjustCameraToCentral(currentFieldPolygon);
        }
    }

    private void showOtherSensor() {
        JsonParser jsonParser = new JsonParser();
        for (int i = 0; i < geom.size(); i++) {
            String geoStr = geom.get(i);
            String sensorID = sensorIDs.get(i);
            LatLng coordinate = jsonParser.getCoordinate(geoStr);
            googleMap.addMarker(new MarkerOptions().position(coordinate).title(sensorID));
        }

    }

    private void adjustCameraToCentral(ArrayList<LatLng> coordinates) {
        if (coordinates.isEmpty()) {
            Log.e("SensorCreation", "No coordinates provided to adjust the camera.");
            return; // Exit the method or set a default camera position here.
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : coordinates) {
            builder.include(point);
        }
        int padding = 50;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    private void setMapToolBar() {
        ImageButton clearButton = binding.clearButton;
        ImageButton drawingButton = binding.drawingButton;
        ImageButton moveButton = binding.moveButton;
        clearButton.setOnClickListener(view -> {
            if (currentMarker != null) {
                currentMarker.remove();
                currentMarker = null;
                newGeom = null;
                googleMap.clear();
                showFieldPolygon();
                showOtherSensor();
            }
        });

        drawingButton.setOnClickListener(view -> {
            // Set background color to show status
            drawingButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            moveButton.setBackgroundColor(getResources().getColor(R.color.background));
            // Set click map to place marker
            googleMap.setOnMapClickListener(latLng -> {
                if (currentMarker != null) {
                    currentMarker.remove();
                }
                MarkerOptions markerOptions=new MarkerOptions()
                        .position(latLng)
                        .title("New Sensor")
                        .snippet(latLng.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                currentMarker = googleMap.addMarker(markerOptions);
                newGeom = latLng; // Update the newGeom variable
            });
        });

        moveButton.setOnClickListener(view -> {
            drawingButton.setBackgroundColor(getResources().getColor(R.color.background));
            moveButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            googleMap.setOnMapClickListener(null); // Cancel placing a marker when clicking on the map
        });
    }
    //show field range


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mapView = null;
        googleMap = null;
    }
}