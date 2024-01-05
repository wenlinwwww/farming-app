package com.example.aq_bluering.ui.Irrigation;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.ZoneCreateEditFragmentBinding;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.RequestBody;

public class ZoneEditing extends DialogFragment {

    private RequestConstructor rc;

    private String farmName, fieldName, zoneName, oldZoneName, soil_25, soil_75, soil_125, cropType;
    private double wPoint50, wPoint100, wPoint150, fCapacity50, fCapacity100, fCapacity150, saturation50, saturation100, saturation150;
    private ArrayList<LatLng> coordinateList = new ArrayList<>();
    private ArrayList<LatLng> oldCoordinateList = new ArrayList<>();

    private ZoneCreateEditFragmentBinding binding;
    private String farmResponse = null;
    private String fieldResponse = null;

    private String zoneResponse = null;
    private MapView mapView;
    private GoogleMap googleMap;
    private Polygon currentPolygon = null;
    private Irrigation irrigation = null;
    private int position;
    private MutableLiveData<String> editZoneResponse = new MutableLiveData<>();


    public ZoneEditing(RequestConstructor rc, IrrigationListAdapter adapter, int position) {
        this.rc = rc;
        this.position = position;
        this.zoneName = adapter.zoneNames.get(position);
        this.oldZoneName = adapter.zoneNames.get(position);
        this.soil_25 = adapter.soil_25.get(position);
        this.soil_75 = adapter.soil_75.get(position);
        this.soil_125 = adapter.soil_125.get(position);
        this.cropType = adapter.cropTypes.get(position);
        this.irrigation = adapter.zone;
        this.farmName = adapter.farmName;
        this.fieldName = adapter.fieldName;
        this.farmResponse = irrigation.getViewModel().getFarmResponse().getValue();
        this.fieldResponse = irrigation.getViewModel().getFieldResponse().getValue();
        this.zoneResponse = irrigation.getViewModel().getZoneResponse().getValue();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = ZoneCreateEditFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.title.setText("Irrigation Zone Editing");
        mapView = binding.mapView;
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
        if (irrigation != null) {
            String json = irrigation.getViewModel().getZoneResponse().getValue();
            wPoint50 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "wpoint_50");
            wPoint100 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "wpoint_100");
            wPoint150 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "wpoint_150");
            fCapacity50 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "fcapacity_50");
            fCapacity100 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "fcapacity_100");
            fCapacity150 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "fcapacity_150");
            saturation50 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "saturation_50");
            saturation100 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "saturation_100");
            saturation150 = jsonParser.getDoubleValue(json, "zonename", oldZoneName, "saturation_150");
            String points = jsonParser.getMultipleValue(json, "zonename", oldZoneName, "points");
            oldCoordinateList = jsonParser.getCoordinates(points);
        } else {
            Log.e("ZoneEditing", "Adapter or Zone in adapter is null!");
        }

        // Setting up the TextViews and their click listeners
        Spinner farmSpinner = binding.farmSpinner;
        Spinner fieldSpinner = binding.fieldSpinner;

        ArrayList<String> farmList = jsonParser.getValueList(farmResponse, "farm_name");

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


        //display current corptype
        Spinner cropSpinner = binding.corpSpinner;

        String[] cropsArray = getResources().getStringArray(R.array.corp_array);
        List<String> cropsList = Arrays.asList(cropsArray);

        ArrayAdapter<String> adapter_c = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cropsList);
        adapter_c.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cropSpinner.setAdapter(adapter_c);

        int positionOfCropType = cropsList.indexOf(cropType);

        if (positionOfCropType != -1) {
            cropSpinner.setSelection(positionOfCropType, true);
        }

        //display current soiltype 25
        Spinner soil25Spinner = binding.soil25Spinner;
        String[] soil25Array = getResources().getStringArray(R.array.soil_25_array);
        List<String> soil25List = Arrays.asList(soil25Array);

        ArrayAdapter<String> adapter_s25 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, soil25List);
        adapter_s25.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soil25Spinner.setAdapter(adapter_s25);

        int positionOfSoil25 = soil25List.indexOf(soil_25);

        if (positionOfSoil25 != -1) {
            soil25Spinner.setSelection(positionOfSoil25, true);
        }
        // current soiltype 75
        Spinner soil75Spinner = binding.soil75Spinner;
        String[] soil75Array = getResources().getStringArray(R.array.soil_75_array);
        List<String> soil75List = Arrays.asList(soil75Array);

        ArrayAdapter<String> adapter_s75 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, soil75List);
        adapter_s75.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soil75Spinner.setAdapter(adapter_s75);

        int positionOfSoil75 = soil75List.indexOf(soil_75);

        if (positionOfSoil75 != -1) {
            soil75Spinner.setSelection(positionOfSoil75, true);
        }
        //soiltype 125
        Spinner soil125Spinner = binding.soil125Spinner;
        String[] soil125Array = getResources().getStringArray(R.array.soil_125_array);
        List<String> soil125List = Arrays.asList(soil125Array);

        ArrayAdapter<String> adapter_s125 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, soil125List);
        adapter_s125.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soil125Spinner.setAdapter(adapter_s125);

        int positionOfSoil125 = soil125List.indexOf(soil_125);

        if (positionOfSoil125 != -1) {
            soil125Spinner.setSelection(positionOfSoil125, true);
        }

        sendToUI();
        binding.backButton.setOnClickListener(view -> {
            irrigation.refresh();
            dismiss();
        });
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFromUI();
                //hide scroll-view
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

            }
        });


        return root;
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);

        int positionOfField = fieldList.indexOf(fieldName);
        if (positionOfField != -1) {
            spinner.setSelection(positionOfField);
        }
    }

    private void setFarmList(Spinner spinner, ArrayList<String> farmList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.list_option_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
        int positionOfFarm = farmList.indexOf(farmName);
        if (positionOfFarm != -1) {
            spinner.setSelection(positionOfFarm);
        }

    }

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
        editZoneResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
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

                //form a closed shape by coordinates
                if (coordinateList.isEmpty()) {
                    coordinateList = oldCoordinateList;
                } else {
                    LatLng startPoint = coordinateList.get(0);
                    coordinateList.add(startPoint);
                }

                binding.submitScreen.setVisibility(View.GONE);
                editZone();
                Toast.makeText(getActivity(), "Successfully Submitted!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //map
    public void setMap(MapView mapView) {
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
        showOtherZone();
    }

    private void drawPolygonOnMap() {

        if (currentPolygon != null) {
            currentPolygon.remove();
        }
        if (!coordinateList.isEmpty()) {
            int color = Color.GREEN;
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(coordinateList)
                    .strokeWidth(5)
                    .strokeColor(color);
            currentPolygon = googleMap.addPolygon(polygonOptions);
        }
    }

    //display current map
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

    private void adjustCameraToCentral(ArrayList<LatLng> coordinates) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : coordinates) {
            builder.include(point);
        }
        int padding = 50;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    private Boolean isValidPolygon() {
        if (this.coordinateList.isEmpty() || coordinateList.size() < 3) {
            return false;
        } else {
            return true;
        }
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

    //data response
    private void sendToUI() {
        binding.zoneNameInput.setText(zoneName);
        binding.wp30.setText(String.valueOf(wPoint50));
        binding.wp100.setText(String.valueOf(wPoint100));
        binding.wp150.setText(String.valueOf(wPoint150));
        binding.fc30.setText(String.valueOf(fCapacity50));
        binding.fc100.setText(String.valueOf(fCapacity100));
        binding.fc150.setText(String.valueOf(fCapacity150));
        binding.sp30.setText(String.valueOf(saturation50));
        binding.sp100.setText(String.valueOf(saturation100));
        binding.sp150.setText(String.valueOf(saturation150));

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
        soil_25 = binding.soil25Spinner.getSelectedItem().toString();

        // Capture Soil Type at 75cm
        soil_75 = binding.soil75Spinner.getSelectedItem().toString();

        // Capture Soil Type at 125cm
        soil_125 = binding.soil125Spinner.getSelectedItem().toString();

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
        } else if (fieldName == null) {
            Toast.makeText(getActivity(), "Please select a field", Toast.LENGTH_SHORT).show();
            return false;
        } else if (fieldName.equals("No Available Field")) {
            Toast.makeText(getActivity(), "Current Farm don't have any field, please create a field first",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (zoneName == null || zoneName.length() < 3) {
            Toast.makeText(getActivity(), "Please enter a valid zone name with length > 3", Toast.LENGTH_SHORT).show();
            return false;
        } else if (cropType == null) {
            Toast.makeText(getActivity(), "Please select a crop type", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soil_25 == null) {
            Toast.makeText(getActivity(), "Please select a soil type for 25", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soil_75 == null) {
            Toast.makeText(getActivity(), "Please select a soil type for 75", Toast.LENGTH_SHORT).show();
            return false;
        } else if (soil_125 == null) {
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

    private void editZone() {
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_editZone(farmName, fieldName, zoneName, coordinateList,
                cropType, soil_25, soil_75, soil_125, wPoint50, wPoint100, wPoint150,
                fCapacity50, fCapacity100, fCapacity150, saturation50, saturation100, saturation150, oldZoneName);
        apiConnection.putAsync(json, "https://webapp.aquaterra.cloud/api/zone/", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                editZoneResponse.postValue(response);
                Log.i("editZoneResponse", "onResponse: " + response);
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
        currentPolygon = null;
        coordinateList.clear();
    }
}