package com.example.aq_bluering.ui.Dashboard.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.aq_bluering.R;

import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.databinding.FragmentDashboardMapBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class Map extends Fragment {
    private MapView mapView;
    private MapViewModel viewModel;
    private FragmentDashboardMapBinding binding;

    private ArrayList<String> farmList = new ArrayList<>();
    private String currentFarmName = null;
    private String currentFieldID = null;
    private String currentFieldName = null;
    private ArrayList<String> sensorIDs = new ArrayList<>();
    private ArrayList<LatLng> sensorCoordinates = new ArrayList<>();
    private ArrayList<String> batteryInfo = new ArrayList<>();
    private ArrayList<String> temperatureInfo = new ArrayList<>();
    private ArrayList<String> cap50Info = new ArrayList<>();
    private ArrayList<String> cap100Info = new ArrayList<>();
    private ArrayList<String> cap150Info = new ArrayList<>();
    private boolean fieldReady = false;

    private UsernameShare usernameShare;

    private final String[] options = {"Battery", "Temperature", "Moisture: 30-50cm Underground",
            "Moisture: 100cm Underground", "Moisture: 150cm Underground"};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        try{
            usernameShare= new ViewModelProvider(requireActivity()).get(UsernameShare.class);
            String userName=usernameShare.getUsername().getValue();
            viewModel.setUsername(userName);
        } catch (Exception e){
            Log.e("error in obtain username", e.getMessage());
        }

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDashboardMapBinding.inflate(inflater, container, false);

        JsonParser jsonParser = new JsonParser();

        View root = binding.getRoot();
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //async map
        viewModel.setMap(mapView);

        //portion to set up menu bar on top for selecting farm, field, and sensor
        Spinner farmSpinner = binding.spinnerFarm;
        Spinner fieldSpinner = binding.spinnerField;
        Spinner typeSpinner = binding.mapDisplayType;
        ImageView legendView = binding.legend;


        //set up listener for receiving data response from api
        //initial farm list
        viewModel.getFarmResponse().observe(getViewLifecycleOwner(), s -> {
            farmList.clear();
            farmList = jsonParser.getValueList(s, "farm_name");
            //default value for current selection of farm is the first farm (if any farm exists)
            if (farmList.isEmpty()) {
                farmList.add("No available farm");
            }
            setFarmList(farmSpinner);
        });
        //initial field list for a given farm name
        viewModel.getFieldResponse().observe(getViewLifecycleOwner(), s -> {
            fieldReady = true;
            ArrayList<String> fieldList = jsonParser.getValueListByName(s, "farm_name",
                    currentFarmName, "field_name");
            if (fieldList.isEmpty()) {
                fieldList.add("No Available Field");
            }
            setFieldList(fieldSpinner, fieldList);
        });
        //when receive sensor list in current field, fetching each sensor latest detail
        viewModel.getSensorResponse().observe(getViewLifecycleOwner(), s -> {
            ArrayList<String> sensorList = jsonParser.getValueList(s, "sensor_id");
            for (String sensor : sensorList) {
                viewModel.fetchSensorDetail(sensor, currentFieldName);
            }
        });
        //when each sensor latest detail received:
        //obtain id, batter, temperature, moisture of each sensor
        viewModel.getSensorDetail().observe(getViewLifecycleOwner(), s -> {

            String sensorID = jsonParser.getValue(s, "sensor_id");
            if (!(sensorID == "no data temporarily")) {
                sensorIDs.add(sensorID);
                LatLng coordinate = jsonParser.getCoordinate_moisture(s);
                sensorCoordinates.add(coordinate);
                String battery = jsonParser.getValue(s, "battery_vol");
                batteryInfo.add(battery);
                String temp = jsonParser.getValue(s, "temperature");
                temperatureInfo.add(temp);
                String cap50 = jsonParser.getValue(s, "cap50");
                cap50Info.add(cap50);
                String cap100 = jsonParser.getValue(s, "cap100");
                cap100Info.add(cap100);
                String cap150 = jsonParser.getValue(s, "cap150");
                cap150Info.add(cap150);
                try {
                    //display info of battery at default
                    double value = Double.parseDouble(battery);
                    int color = getResources().getColor(R.color.battery0);
                    if (value <= 3.4) {
                        color = getResources().getColor(R.color.battery0);
                    } else if (value <= 3.6) {
                        color = getResources().getColor(R.color.battery1);
                    } else if (value <= 3.8) {
                        color = getResources().getColor(R.color.battery2);
                    } else {
                        color = getResources().getColor(R.color.battery3);
                    }
                    viewModel.setMarker(coordinate, sensorID, "Battery: " + battery + "V", color);
                } catch (Exception e) {
                    Log.e("Invalid battery value", e.getMessage());
                }

            }

        });
        //obtain irrigation zone information
        viewModel.getZoneResponse().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                ArrayList<String> zones = jsonParser.getValueListByName(s, "fieldname", currentFieldName, "points");
                if (!zones.isEmpty()) {
                    for (String zone : zones) {
                        ArrayList<LatLng> polygonPoints = jsonParser.getCoordinates(zone);
                        viewModel.drawPolygonOnMap(polygonPoints, 1);

                    }
                }
            }
        });


        //set up menu to select current display on map
        setMoistureMenu(typeSpinner);

        //set up menu selection listener
        // set when a farm name selection changed, get field list in this farm
        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFarmName = farmSpinner.getSelectedItem().toString();
                ArrayList<String> fields = new ArrayList<>();
                if (fieldReady) {
                    fields = jsonParser.getValueListByName(viewModel.getFieldResponse().getValue(), "farm_name",
                            currentFarmName, "field_name");
                }
                if (fields.isEmpty()) {
                    fields.add("No Available Field");
                }
                setFieldList(fieldSpinner, fields);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        //when the current field selection changed, get the sensor list in this field, update the map with polygon
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clearSensorInfo();
                viewModel.clearMap();
                currentFieldName = fieldSpinner.getSelectedItem().toString();
                if (!currentFieldName.equals("No Available Field")) {
                    String fieldID = jsonParser.getMultipleValue(viewModel.getFieldResponse().getValue(), "field_name",
                            currentFieldName, "field_id");
                    currentFieldID = fieldID;
                    if (currentFieldID != null) {
                        viewModel.fetchSensorList(fieldID);
                        viewModel.fetchZoneDetail();
                    }


                    String fieldPolygon = jsonParser.getMultipleValue(viewModel.getFieldResponse().getValue(), "field_id", fieldID, "points");
                    ArrayList<LatLng> polygonPoints = jsonParser.getCoordinates(fieldPolygon);
                    viewModel.drawPolygonOnMap(polygonPoints, 0);
                    binding.locationButton.setOnClickListener(view1 -> viewModel.setMyLocationButton(polygonPoints));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setLegend(legendView, i);
                if (!sensorIDs.isEmpty()) {
                    setAllMarkers(i);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        viewModel.fetchFarmList();
        viewModel.fetchFieldList();


        return root;
    }


    private void setFarmList(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);
    }

    private void setMoistureMenu(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_white);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(options);
    }

    private void setAllMarkers(int i) {
        viewModel.clearMarkers();
        for (String sensorID : sensorIDs) {
            String snippet = "no data available for now";
            String title = sensorID;
            int index = sensorIDs.indexOf(sensorID);
            LatLng coordinate = sensorCoordinates.get(index);
            double value = 0;
            int color = getResources().getColor(R.color.battery0);
            try {
                switch (i) {
                    //change snippet text and color based on current selection
                    case 0:
                        snippet = options[0] + ": " + batteryInfo.get(index) + "V";
                        value = Double.parseDouble(batteryInfo.get(index));

                        if (value <= 3.4) {
                            color = getResources().getColor(R.color.battery0);
                        } else if (value <= 3.6) {
                            color = getResources().getColor(R.color.battery1);
                        } else if (value <= 3.8) {
                            color = getResources().getColor(R.color.battery2);
                        } else {
                            color = getResources().getColor(R.color.battery3);
                        }
                        break;
                    case 1:
                        snippet = options[1] + ": " + temperatureInfo.get(index) + "℃";
                        value = Double.parseDouble(temperatureInfo.get(index));

                        if (value <= 0) {
                            color = getResources().getColor(R.color.temperature0);
                        } else if (value <= 10) {
                            color = getResources().getColor(R.color.temperature1);
                        } else if (value <= 20) {
                            color = getResources().getColor(R.color.temperature2);
                        } else if (value <= 30) {
                            color = getResources().getColor(R.color.temperature3);
                        } else {
                            color = getResources().getColor(R.color.temperature4);
                        }
                        break;
                    case 2:
                        snippet = options[2] + ": " + cap50Info.get(index) + "%";
                        value = Double.parseDouble(cap50Info.get(index));

                        if (value <= 20) {
                            color = getResources().getColor(R.color.moisture0);
                        } else if (value <= 40) {
                            color = getResources().getColor(R.color.moisture1);
                        } else if (value <= 50) {
                            color = getResources().getColor(R.color.moisture2);
                        } else if (value <= 60) {
                            color = getResources().getColor(R.color.moisture3);
                        } else if (value <= 80) {
                            color = getResources().getColor(R.color.moisture4);
                        } else {
                            color = getResources().getColor(R.color.moisture5);
                        }
                        break;
                    case 3:
                        snippet = options[3] + ": " + cap100Info.get(index) + "%";
                        value = Double.parseDouble(cap100Info.get(index));

                        if (value <= 20) {
                            color = getResources().getColor(R.color.moisture0);
                        } else if (value <= 40) {
                            color = getResources().getColor(R.color.moisture1);
                        } else if (value <= 50) {
                            color = getResources().getColor(R.color.moisture2);
                        } else if (value <= 60) {
                            color = getResources().getColor(R.color.moisture3);
                        } else if (value <= 80) {
                            color = getResources().getColor(R.color.moisture4);
                        } else {
                            color = getResources().getColor(R.color.moisture5);
                        }
                        break;
                    case 4:
                        snippet = options[4] + ": " + cap150Info.get(index) + "%";
                        value = Double.parseDouble(cap150Info.get(index));

                        if (value <= 20) {
                            color = getResources().getColor(R.color.moisture0);
                        } else if (value <= 40) {
                            color = getResources().getColor(R.color.moisture1);
                        } else if (value <= 50) {
                            color = getResources().getColor(R.color.moisture2);
                        } else if (value <= 60) {
                            color = getResources().getColor(R.color.moisture3);
                        } else if (value <= 80) {
                            color = getResources().getColor(R.color.moisture4);
                        } else {
                            color = getResources().getColor(R.color.moisture5);
                        }
                        break;
                    default:
                        break;
                }

                viewModel.setMarker(coordinate, title, snippet, color);

            } catch (Exception e) {
                Log.e("Invalid  value", e.getMessage());
            }
        }
    }

    private void setLegend(ImageView legendView, int i) {

        if (i == 0) {
            legendView.setBackground(getResources().getDrawable(R.drawable.battery_legend));
        } else if (i == 1) {
            legendView.setBackground(getResources().getDrawable(R.drawable.temperature_legend));
        } else if (i == 2 || i == 3 || i == 4) {
            legendView.setBackground(getResources().getDrawable(R.drawable.moisture_legend));
        }
    }

    private void clearSensorInfo() {
        sensorIDs.clear();
        batteryInfo.clear();
        temperatureInfo.clear();
        cap50Info.clear();
        cap100Info.clear();
        cap150Info.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}