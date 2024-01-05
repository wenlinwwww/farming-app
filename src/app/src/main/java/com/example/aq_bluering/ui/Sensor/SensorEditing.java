package com.example.aq_bluering.ui.Sensor;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.SensorEditFragmentBinding;
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
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SimpleTimeZone;


public class SensorEditing extends DialogFragment {
    private RequestConstructor rc;
    private SensorEditFragmentBinding binding;
    private MutableLiveData<String> editSensorResponse = new MutableLiveData<>();

    private boolean isActive;
    private String sleeping;
    private String alias;
    private Sensor sensor;
    private String SensorResponse;
    private String farmResponse = null;
    private String fieldResponse = null;
    private String farmName, fieldName;
    private MapView mapView;
    private GoogleMap googleMap;
    private LatLng oldGeom;
    private LatLng newGeom;
    private String sensorID;
    private String fieldID;
    private int position;
    private Marker currentMarker;

    public SensorEditing(RequestConstructor rc, SensorListAdapter adapter, int position) {
        this.rc = rc;
        this.position = position;
        this.sensorID = adapter.sensors.get(position);
        this.sensor = adapter.sensor;
        this.SensorResponse = sensor.getViewModel().getSensorResponse().getValue();
        this.farmResponse = sensor.getViewModel().getFarmResponse().getValue();
        this.fieldResponse = sensor.getViewModel().getFieldResponse().getValue();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = SensorEditFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.backButton.setOnClickListener(view -> dismiss());
        Spinner farmSpinner = binding.farmSpinner;
        Spinner fieldSpinner = binding.fieldSpinner;
        JsonParser jsonParser = new JsonParser();
        if (sensor != null) {
            String points = jsonParser.getMultipleValue(SensorResponse, "sensor_id", this.sensorID, "points");
            oldGeom = jsonParser.getCoordinate(points);
            alias = jsonParser.getMultipleValue(SensorResponse, "sensor_id", this.sensorID, "alias");
            sleeping = jsonParser.getMultipleValue(SensorResponse, "sensor_id", this.sensorID, "sleeping");
            fieldID = jsonParser.getMultipleValue(SensorResponse, "sensor_id", this.sensorID, "field_id");
            isActive = Boolean.parseBoolean(jsonParser.getMultipleValue(SensorResponse, "sensor_id", this.sensorID, "is_active"));
        } else {
            Log.e("SensorEditing", "Adapter or sensor in adapter is null!");
        }
        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UICapture();
                binding.inputView.setVisibility(View.GONE);
                binding.zoneMap.setVisibility(View.VISIBLE);
                init_step2();
            }
        });


        sendToUI();


        /**
         * change farm and field of a sensor is not available at this point
         ArrayList<String> farmList = jsonParser.getValueList(farmResponse, "farm_name");
         setFarmList(farmSpinner, farmList);
         farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentFarm = farmSpinner.getSelectedItem().toString();
        ArrayList<String> fields = new ArrayList<>();
        if (fieldResponse != null) {
        fields = jsonParser.getValueListByName(fieldResponse, "farm_name",
        farmName, "field_name");
        }
        if (fields.isEmpty()) {
        fields.add("No Available Field");
        }
        setFieldList(fieldSpinner, fields);
        }

        @Override public void onNothingSelected(AdapterView<?> adapterView) {

        }
        });
         */

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.onResume();



        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);

    }

    private void setFarmList(Spinner spinner, ArrayList<String> farmList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);


    }

    private void setCoordinateListener() {
        binding.latitudeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateMarkerDynamically();
            }
        });
        binding.longitudeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateMarkerDynamically();
            }
        });
    }

    private void sendToUI() {
        binding.aliasInput.setText(alias);
        binding.etSensorId.setText(sensorID);
        //sleeptime spinner display
        Spinner sleepingTimeSpinner = binding.sleepingTimeSpinner;

        String[] sleepingTimeArray = getResources().getStringArray(R.array.sleeping_times);
        List<String> sleepingTimeList = Arrays.asList(sleepingTimeArray);
        List<String> onlyNumbersList = new ArrayList<>();

        for (String time : sleepingTimeList) {
            String number = time.replaceAll("[^0-9]", "");
            onlyNumbersList.add(number);
        }

        ArrayAdapter<String> adapter_s = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sleepingTimeList);
        adapter_s.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sleepingTimeSpinner.setAdapter(adapter_s);

        int positionOfSleep = onlyNumbersList.indexOf(sleeping);

        if (positionOfSleep != -1) {
            sleepingTimeSpinner.setSelection(positionOfSleep, true);
        }
    }

    private void UICapture() {
        //farmName = binding.farmSpinner.getSelectedItem().toString();
        // Capture Field
        //fieldName = binding.fieldSpinner.getSelectedItem().toString();
        alias = binding.aliasInput.getText().toString();
        String getSleeping = binding.sleepingTimeSpinner.getSelectedItem().toString();
        sleeping = String.valueOf(extractNumbers(getSleeping));
    }

    private void init_step2() {
        //set text enter initial value for coordinate
        String latString = String.valueOf(oldGeom.latitude);
        String lngString = String.valueOf(oldGeom.longitude);
        binding.latitudeInput.setText(latString);
        binding.longitudeInput.setText(lngString);
        setMap(mapView);
        setCoordinateListener();
        editSensorResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")) {
                    Toast.makeText(getActivity(), "Update Sensor Successfully!", Toast.LENGTH_SHORT).show();
                    sensor.refresh();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Fail to update this sensor, please try again!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSensor();
            }
        });

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
                showOriginalPosition();
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
                selectNewLocation(latLng);
                binding.latitudeInput.setText(String.valueOf(latLng.latitude));
                binding.longitudeInput.setText(String.valueOf(latLng.longitude));
            });
        });

        moveButton.setOnClickListener(view -> {
            drawingButton.setBackgroundColor(getResources().getColor(R.color.background));
            moveButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            googleMap.setOnMapClickListener(null); // Cancel placing a marker when clicking on the map
        });
    }

    private void showOriginalPosition() {
        googleMap.addMarker(new MarkerOptions().position(oldGeom).title(sensorID));
    }

    private void updateMarkerDynamically() {
        try {
            double lat = Double.parseDouble(binding.latitudeInput.getText().toString());
            double lon = Double.parseDouble(binding.longitudeInput.getText().toString());
            LatLng latLng = new LatLng(lat, lon);
            selectNewLocation(latLng);
        } catch (Exception e) {
            Log.e("error in set lat/lon", e.getMessage());
        }
    }

    private void selectNewLocation(LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("New Sensor")
                .snippet(latLng.toString())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        currentMarker = googleMap.addMarker(markerOptions);
        newGeom = latLng; // Update the newGeom variable

    }

    private void adjustCameraToCentral(ArrayList<LatLng> coordinates) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : coordinates) {
            builder.include(point);
        }
        int padding = 50;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
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
        showOriginalPosition();
        setMapToolBar();
    }

    public int extractNumbers(String input) {
        String numbersOnly = input.replaceAll("[^0-9]", "");
        return Integer.parseInt(numbersOnly);
    }

    private void editSensor() {
        ApiConnection apiConnection = new ApiConnection();
        LatLng finalGeom = oldGeom;
        if (newGeom != null) {
            finalGeom = newGeom;
        }
        String json = rc.getJson_editSensor(finalGeom, isActive, Integer.parseInt(sleeping), alias);
        String targetURL = "https://webapp.aquaterra.cloud/api/sensor/" + sensorID;

        apiConnection.putAsync(json, targetURL, new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                editSensorResponse.postValue("success");
            }

            @Override
            public void onError(String errorMessage) {
                editSensorResponse.postValue("fail");
                Log.e("Connection Error", errorMessage);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mapView = null;
        googleMap = null;
        currentMarker = null;
    }
}