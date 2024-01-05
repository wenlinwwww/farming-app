package com.example.aq_bluering.ui.FarmField;

import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.ProfileFragmentBinding;
import com.example.aq_bluering.databinding.SensorInstallFragmentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;


public class SensorInstallation extends DialogFragment {
    private String fieldID;
    private String fieldPolygon;
    private RequestConstructor rc;
    private SensorInstallFragmentBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private Marker marker = null;

    private ArrayList<String> gateways = new ArrayList<>();

    private ArrayList<LatLng> coordinatesList = new ArrayList<>();

    private String selectedSensor = null;
    private String selectedGateway=null;

    private ArrayList<String> pairedSensor=new ArrayList<>();

    private MutableLiveData<String> gatewaysResponse = new MutableLiveData<>();
    private MutableLiveData<String> activationResponse = new MutableLiveData<>();
    private MutableLiveData<String> sensorResponse = new MutableLiveData<>();
    private MutableLiveData<String> submissionResponse = new MutableLiveData<>();


    public SensorInstallation(RequestConstructor rc,String fieldID, String fieldPolygon) {
        this.rc=rc;
        this.fieldID = fieldID;
        this.fieldPolygon=fieldPolygon;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        JsonParser jsonParser = new JsonParser();
        binding = SensorInstallFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.backButton.setOnClickListener(view -> dismiss());
        if (fieldID != null && !fieldID.isEmpty()) {
            //step 1
            fetchGateways(fieldID);
            //set map - draw polygon
            mapView = binding.mapView;
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //when get response with gateways near the field
        gatewaysResponse.observe(getViewLifecycleOwner(), s -> {
            gateways = jsonParser.getValueList(s, "gateway_id");
            init_step_1();
        });

        //when get information of paired sensor
        sensorResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.i("check paired sensor", s);
                if (s.equals("fail")) {
                    binding.loadingData.setVisibility(View.VISIBLE);
                    binding.loadingMsg.setText("No paired sensor is found\nPlease exit and try again!");
                } else {
                    init_step_2(jsonParser, s);
                }

            }
        });


        return root;
    }

    //step 1: retrieve available gateways near selected field, call gateways initiate pairing process
    private void init_step_1() {
        TextView gatewayText = binding.gatewayText;

        if (gateways.isEmpty()) {
            //if no gateway is found
            gatewayText.setText("No gateway found within this field range");
            gatewayText.setTextColor(getResources().getColor(R.color.red));
            binding.step1Next.setText("No Gateway Available");
        } else {
            //if gateways founded
            int qty = gateways.size();
            gatewayText.setText(qty + " gateways found ");
            gatewayText.setTextColor(getResources().getColor(R.color.edit));
            TextView gatewayList = binding.gatewayList;
            StringBuilder stringBuilder = new StringBuilder();
            for (String gateway : gateways) {
                stringBuilder.append("• ").append(gateway).append("\n");
            }
            gatewayList.setText(stringBuilder);


            gatewayList.setVisibility(View.VISIBLE);

            //set when get response of activating gateways successfully
            activationResponse.observe(getViewLifecycleOwner(), s -> {
                if (s.equals("fail")) {
                    Toast.makeText(getActivity(), "Fail to activate gateways, please exit and try again", Toast.LENGTH_SHORT).show();
                } else {
                    Button next = binding.step1Next;
                    next.setClickable(true);
                    next.setBackground(getResources().getDrawable(R.drawable.create_button));
                    next.setText("Next");
                    next.setOnClickListener(view -> {
                        binding.searchGateways.setVisibility(View.GONE);
                        binding.loadingData.setVisibility(View.VISIBLE);
                        fetchPairedSensor();
                    });
                }

            });

            setGatewaysActive();
        }
        binding.searchGateways.setVisibility(View.VISIBLE);
        binding.loadingData.setVisibility(View.GONE);
    }


    private void fetchGateways(String fieldId) {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchGatewaysNearField(fieldId);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/gateway/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                Log.i("fetch gateways near a field", response);
                gatewaysResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
                gatewaysResponse.postValue(errorMessage);
            }
        });
    }

    private void setGatewaysActive() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_activatedGateways(gateways);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/gateway/setup", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                Log.i("set up gateways", response);
                activationResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                activationResponse.postValue("fail");
                Log.i("Active gateway fail", errorMessage);
            }
        });
    }


    //step 2: select paired sensor to install

    private void init_step_2(JsonParser jsonParser, String json) {
        pairedSensor=jsonParser.getValueList(json,"sensor_id");
        binding.step2SensorText.setText("Found "+pairedSensor.size()+" sensor(s) registered");
        RadioGroup sensorRadioGroup = binding.step2SensorList;
        for (String sensorID : pairedSensor) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(sensorID);
            radioButton.setTextColor(getResources().getColor(R.color.edit));
            radioButton.setTextSize(25);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.topMargin = (int) (10 * getResources().getDisplayMetrics().density);
            radioButton.setLayoutParams(layoutParams);
            sensorRadioGroup.addView(radioButton);
        }
        sensorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = binding.getRoot().findViewById(checkedId);
            selectedSensor = selectedRadioButton.getText().toString();
            selectedGateway=jsonParser.getMultipleValue(json,"sensor_id",selectedSensor,"gateway_id");
            Button next = binding.step2Next;
            if (!next.hasOnClickListeners()){
                next.setClickable(true);
                next.setBackground(getResources().getDrawable(R.drawable.create_button));
                next.setOnClickListener(view -> {
                    //start step 3
                    binding.setSensor.setVisibility(View.GONE);
                    binding.setSensorLocation.setVisibility(View.VISIBLE);
                    init_step_3();
                });
            }
        });

        binding.loadingData.setVisibility(View.GONE);
        binding.setSensor.setVisibility(View.VISIBLE);
    }

    private void fetchPairedSensor() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchPairedSensor(gateways);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/gateway/sensors", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                sensorResponse.postValue(response);
                Log.i("sensor paired", response);

            }

            @Override
            public void onError(String errorMessage) {
                sensorResponse.postValue("fail");
                Log.e("no sensor paired", errorMessage);
            }
        });
    }


    //step 3: select marker location, create sensor in database
    private void init_step_3() {
        //async map
        setMap(mapView);

        ImageButton clearButton = binding.clearButton;
        ImageButton drawingButton = binding.drawingButton;
        ImageButton moveButton = binding.moveButton;
        Button submit = binding.step3Submit;
        clearButton.setOnClickListener(view -> {
            googleMap.clear();
            marker = null;
            coordinatesList.clear();
            binding.step3Submit.setClickable(false);
            binding.step3Submit.setBackground(getResources().getDrawable(R.drawable.invalid_button));
        });

        drawingButton.setOnClickListener(view -> {
            //set background color to show status
            //set current clicked button to grey, resume other button to white
            drawingButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            moveButton.setBackgroundColor(getResources().getColor(R.color.background));

            //set click map to place marker and draw polygon
            googleMap.setOnMapClickListener(latLng -> {
                coordinatesList.clear();
                googleMap.clear();
                coordinatesList.add(latLng);
                marker = googleMap.addMarker(new MarkerOptions().position(latLng));
                binding.step3Submit.setClickable(true);
                binding.step3Submit.setBackground(getResources().getDrawable(R.drawable.create_button));
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

        submissionResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")){
                    Toast.makeText(getActivity(), "Successfully Submitted!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Submission Failed, Please Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        submit.setOnClickListener(view -> {
            if (marker != null && selectedSensor != null && fieldID != null && selectedGateway!=null) {
                submission();
            }
        });


    }

    private void submission() {
        LatLng coordinate = marker.getPosition();
        String json = rc.getJson_add_Version1_sensor(selectedSensor, selectedGateway, fieldID, coordinate);

        ApiConnection apiConnection = new ApiConnection();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/new", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                    submissionResponse.postValue("success");
                Log.i("submit successfully", response);
            }

            @Override
            public void onError(String errorMessage) {
                submissionResponse.postValue("fail");
                Log.i("submit failed", errorMessage);

            }
        });

    }

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
        JsonParser jsonParser = new JsonParser();
        ArrayList<LatLng> coordinates = jsonParser.getCoordinates(fieldPolygon);
        showFieldPolygon(coordinates);
    }
    public void showFieldPolygon(ArrayList<LatLng> coordinateList) {
        if (!coordinateList.isEmpty()) {
            int color = Color.RED;
            adjustCameraToCentral(coordinateList);
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(coordinateList)
                    .strokeWidth(5)
                    .strokeColor(color);
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
    private void closeAllGateways() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_deActivatedGateways(gateways);
        Log.i("close gateway json", json);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/gateway/setup", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                Log.i("close gateway response", response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("close gateway Error", errorMessage);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeAllGateways();
        binding = null;
        marker = null;
        coordinatesList.clear();
        googleMap = null;
        mapView = null;

    }
}