package com.example.aq_bluering.ui.Irrigation;

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
import android.widget.EditText;
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
import com.example.aq_bluering.databinding.ZoneCreateEditFragmentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;


import java.util.ArrayList;

public class ZoneCreation extends DialogFragment {
    private RequestConstructor rc;
    //get information
    private String fieldName = null;
    private String zoneName = null;
    private String farmName = null;
    private String cropType = null;
    private ArrayList<LatLng> coordinateList = new ArrayList<>();
    private String soilType25 = null;
    private String soilType75 = null;
    private String soilType125 = null;
    private double wPoint50 = 0;
    private double wPoint100 = 0;
    private double wPoint150 = 0;
    private double fCapacity50 = 0;
    private double fCapacity100 = 0;
    private double fCapacity150 = 0;
    private double saturation50 = 0;
    private double saturation100 = 0;
    private double saturation150 = 0;
    private MutableLiveData<String> addZoneResponse = new MutableLiveData<>();
    private ZoneCreateEditFragmentBinding binding;
    private String farmResponse = null;
    private String fieldResponse = null;
    private String zoneResponse = null;
    private MapView mapView;
    private GoogleMap googleMap;
    private Polygon currentZonePolygon = null;
    Irrigation irrigation = null;

    public ZoneCreation(RequestConstructor rc, String farmResponse, String fieldResponse, String zoneResponse, Irrigation irrigation) {
        this.rc = rc;
        this.irrigation = irrigation;
        this.farmResponse = farmResponse;
        this.fieldResponse = fieldResponse;
        this.zoneResponse = zoneResponse;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = ZoneCreateEditFragmentBinding.inflate(inflater, container, false);
        mapView = binding.mapView;
        View root = binding.getRoot();
        JsonParser jsonParser = new JsonParser();
        addZoneNameInputCheck();
        addRangeCheck(binding.wp30);
        addRangeCheck(binding.wp100);
        addRangeCheck(binding.wp150);
        addRangeCheck(binding.fc30);
        addRangeCheck(binding.fc100);
        addRangeCheck(binding.fc150);
        addRangeCheck(binding.sp30);
        addRangeCheck(binding.sp100);
        addRangeCheck(binding.sp150);

        // Setting up the TextViews and their click listeners
        Spinner farmSpinner = binding.farmSpinner;
        Spinner fieldSpinner = binding.fieldSpinner;

        ArrayList<String> farmList = jsonParser.getValueList(farmResponse, "farm_name");
        if (farmList.isEmpty()) {
            farmList.add("No Available Farm");
            Toast.makeText(getActivity(), "Please create a farm first", Toast.LENGTH_SHORT).show();
        }

        setFarmList(farmSpinner, farmList);
        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                farmName = farmSpinner.getSelectedItem().toString();
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

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.backButton.setOnClickListener(view -> {
            dismiss();
        });
        binding.nextButton.setOnClickListener(view -> {
            //hide scroll-view
            captureFromUI();
            if (isValidInput()) {
                binding.step1.setVisibility(View.GONE);
                binding.zoneMap.setVisibility(View.VISIBLE);
                //set map - draw polygon

                mapView.onCreate(savedInstanceState);
                mapView.onResume();

                try {
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                init_step2();

            }
        });




        return root;
    }

    private void addZoneNameInputCheck() {
        binding.zoneNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (input.length() < 3 || !Character.isLetter(input.charAt(0))) {
                    binding.zoneNameInput.setError("Minimum 3 characters, and must start with letters");
                } else {
                    binding.zoneNameInput.setError(null);
                }
            }
        });
    }

    //step 1
    private void setFarmList(Spinner spinner, ArrayList<String> farmList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);
    }

    private void addRangeCheck(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) return;
                try {
                    double value = Double.parseDouble(s.toString());
                    if (value < 0 || value >= 100) {
                        editText.setError("Please enter a number between 0 - 100");
                    } else {
                        editText.setError(null);
                    }
                } catch (NumberFormatException e) {
                    editText.setError("Invalid number");
                }
            }
        });
    }

    private void captureFromUI() {
        // Capture Farm
        farmName = binding.farmSpinner.getSelectedItem().toString();

        // Capture Field
        fieldName = binding.fieldSpinner.getSelectedItem().toString();

        // Capture Zone Name
        zoneName = binding.zoneNameInput.getText().toString();

        // Capture Crop Type
        cropType = binding.corpSpinner.getSelectedItem().toString();

        // Capture Soil Type at 25cm
        soilType25 = binding.soil25Spinner.getSelectedItem().toString();

        // Capture Soil Type at 75cm
        soilType75 = binding.soil75Spinner.getSelectedItem().toString();

        // Capture Soil Type at 125cm
        soilType125 = binding.soil125Spinner.getSelectedItem().toString();

        // Capture Wilting Points
        wPoint50 = Double.parseDouble(binding.wp30.getText().toString());
        wPoint100 = Double.parseDouble(binding.wp100.getText().toString());
        wPoint150 = Double.parseDouble(binding.wp150.getText().toString());

        // Capture Field Capacities
        fCapacity50 = Double.parseDouble(binding.fc30.getText().toString());
        fCapacity100 = Double.parseDouble(binding.fc100.getText().toString());
        fCapacity150 = Double.parseDouble(binding.fc150.getText().toString());

        saturation50 = Double.parseDouble(binding.sp30.getText().toString());
        saturation100 = Double.parseDouble(binding.sp100.getText().toString());
        saturation150 = Double.parseDouble(binding.sp150.getText().toString());
    }

    private boolean isValidInput() {
        if (farmName == null) {
            Toast.makeText(getActivity(), "Please select a farm", Toast.LENGTH_SHORT).show();
            return false;
        } else if (farmName.equals("No Available Farm")){
            Toast.makeText(getActivity(), "Please create a farm first", Toast.LENGTH_SHORT).show();
            return false;
        }else if (fieldName.equals("No Available Farm")) {
            Toast.makeText(getActivity(), "Current Farm don't have any field, please create a field first", Toast.LENGTH_SHORT).show();
            return false;
        }else if (fieldName == null) {
            Toast.makeText(getActivity(), "Please select a field", Toast.LENGTH_SHORT).show();
            return false;
        } else if (zoneName == null || zoneName.length() < 3) {
            Toast.makeText(getActivity(), "Please enter a valid zone name with length > 3", Toast.LENGTH_SHORT).show();
            return false;
        } else if (cropType == null) {
            Toast.makeText(getActivity(), "Please select a crop type", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soilType25 == null) {
            Toast.makeText(getActivity(), "Please select a soil type for 25", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soilType75 == null) {
            Toast.makeText(getActivity(), "Please select a soil type for 75", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soilType125 == null) {
            Toast.makeText(getActivity(), "Please select a soil type for 125", Toast.LENGTH_SHORT).show();
            return false;
        } else if (wPoint50 < 0 || wPoint50 >= 100) {
            Toast.makeText(getActivity(), "Please enter wPoint50 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (wPoint100 < 0 || wPoint50 >= 100) {
            Toast.makeText(getActivity(), "Please enter wPoint100 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (wPoint150 < 0 || wPoint50 >= 100) {
            Toast.makeText(getActivity(), "Please enter wPoint150 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (fCapacity50 < 0 || fCapacity50 >= 100) {
            Toast.makeText(getActivity(), "Please enter fCapacity50 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (fCapacity100 < 0 || fCapacity100 >= 100) {
            Toast.makeText(getActivity(), "Please enter fCapacity100 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (fCapacity150 < 0 || fCapacity150 >= 100) {
            Toast.makeText(getActivity(), "Please enter fCapacity150 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (saturation50 < 0 || saturation50 >= 100) {
            Toast.makeText(getActivity(), "Please enter saturation50 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (saturation100 < 0 || saturation100 >= 100) {
            Toast.makeText(getActivity(), "Please enter saturation100 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else if (saturation150 < 0 || saturation150 >= 100) {
            Toast.makeText(getActivity(), "Please enter saturation150 in range 0-100", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    //step 2
    private void init_step2() {
        //async map
        setMap(mapView);
        ImageButton clearButton = binding.clearButton;
        ImageButton drawingButton = binding.drawingButton;
        ImageButton moveButton = binding.moveButton;
        clearButton.setOnClickListener(view -> {
            googleMap.clear();
            coordinateList.clear();
            showFieldPolygon();
            showOtherZone();
        });

        drawingButton.setOnClickListener(view -> {
            //set background color to show status
            //set current clicked button to grey, resume other button to white
            drawingButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            moveButton.setBackgroundColor(getResources().getColor(R.color.background));

            //set click map to place marker and draw polygon
            googleMap.setOnMapClickListener(latLng -> {
                googleMap.addMarker(new MarkerOptions().position(latLng));
                coordinateList.add(latLng);
                drawPolygonOnMap();
            });
        });

        moveButton.setOnClickListener(view -> {
            //set background color to show status
            //set current clicked button to grey, resume other button to white
            drawingButton.setBackgroundColor(getResources().getColor(R.color.background));
            moveButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            //cancel placing a marker when click on map
            googleMap.setOnMapClickListener(null);
        });
        addZoneResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(getActivity(), "Successfully Submitted!", Toast.LENGTH_SHORT).show();
                irrigation.refresh();
                dismiss();
            }
        });
        binding.step3Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //submit info
                //if failed add response
                if (isValidPolygon()) {
                    //form a closed shape by coordinates
                    LatLng startPoint= coordinateList.get(0);
                    coordinateList.add(startPoint);
                    binding.submitScreen.setVisibility(View.GONE);
                    addZone();
                    Toast.makeText(getActivity(), "Successfully Submitted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please draw a valid zone on map.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //map
    private void setMap(MapView mapView) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            //when loading map
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
        if (zoneResponse!=null&&!zoneResponse.isEmpty()){
            showOtherZone();
        }

    }

    private void drawPolygonOnMap() {
        if (currentZonePolygon != null) {
            currentZonePolygon.remove();
        }
        if (!coordinateList.isEmpty()) {
            int color = Color.GREEN;
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(coordinateList)
                    .strokeWidth(5)
                    .strokeColor(color);
            currentZonePolygon = googleMap.addPolygon(polygonOptions);
        }
    }

    private void adjustCameraToCentral(ArrayList<LatLng> coordinates) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : coordinates) {
            builder.include(point);
        }
        int padding = 50;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    //show field range
    private void showFieldPolygon() {
        JsonParser jsonParser = new JsonParser();
        String fieldGeomJson = jsonParser.getMultipleValue(fieldResponse,
                "field_name", fieldName, "points");
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

    private void showOtherZone() {
        JsonParser jsonParser = new JsonParser();
        ArrayList<String> otherZoneGeomString = jsonParser.getValueListByName(zoneResponse,
                "fieldname", fieldName, "points");
        for (String json : otherZoneGeomString) {
            ArrayList<LatLng> zonePolygon = jsonParser.getCoordinates(json);
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(zonePolygon)
                    .strokeWidth(5)
                    .strokeColor(Color.BLUE);
            googleMap.addPolygon(polygonOptions);
        }
    }

    //check polygon
    private Boolean isValidPolygon() {
        if (this.coordinateList.isEmpty() || coordinateList.size() < 3) {
            return false;
        } else {
            return true;
        }
    }

    private void addZone() {

        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_createZone(farmName, fieldName, zoneName, coordinateList,
                cropType, soilType25, soilType75, soilType125, wPoint50, wPoint100, wPoint150,
                fCapacity50, fCapacity100, fCapacity150, saturation50, saturation100, saturation150);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/zone/addZone", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                addZoneResponse.postValue(response);

            }

            @Override
            public void onError(String errorMessage) {
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
        currentZonePolygon = null;
        coordinateList.clear();
    }
}