package com.example.aq_bluering.ui.FarmField;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.FarmfieldCreateFragmentBinding;
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
import java.util.Objects;


public class FarmFieldCreation extends DialogFragment {
    private FarmField farmField;
    private FarmfieldCreateFragmentBinding binding;
    private String currentField = null;
    private String currentFarmName = null;

    private ArrayList<String> farmNameList = new ArrayList<>();

    private MapView mapView;
    private GoogleMap googleMap;
    private Polygon currentPolygon = null;
    private ArrayList<LatLng> coordinateList = new ArrayList<>();
    private MutableLiveData<String> farmCreateResponse = new MutableLiveData<>();
    private MutableLiveData<String> fieldCreateResponse = new MutableLiveData<>();
    private RequestConstructor rc;

    public FarmFieldCreation(RequestConstructor rc,ArrayList<String> farmList, FarmField farmField) {
        this.farmField = farmField;
        this.rc=rc;
        for (String farm : farmList) {
            this.farmNameList.add(farm);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FarmfieldCreateFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ConstraintLayout createFarm1 = root.findViewById(R.id.createFarm1);
        ConstraintLayout createFarm2 = root.findViewById(R.id.createFarm2);
        AppCompatButton nextButton = root.findViewById(R.id.nextButton);
        AppCompatButton submitButton = root.findViewById(R.id.submitButton);
        Spinner farmNameSpinner = root.findViewById(R.id.farmNameSpinner);
        EditText farmNameText = root.findViewById(R.id.farmNameText);
        EditText fieldNameText = root.findViewById(R.id.fieldNameInput);
        TextView errorText = root.findViewById(R.id.errorText);

        ImageButton backButton = binding.backButton;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button newFarm = binding.optionNewfarm;
        Button oldFarm = binding.optionOldfarm;

        newFarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change button color
                newFarm.setBackground(getResources().getDrawable(R.drawable.create_button));
                newFarm.setTextColor(getResources().getColor(R.color.background));
                oldFarm.setBackground(getResources().getDrawable(R.drawable.action_button));
                oldFarm.setTextColor(getResources().getColor(R.color.button));
                farmNameText.setVisibility(View.VISIBLE);
                farmNameSpinner.setVisibility(View.GONE);
                farmCreateResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
                    @Override
                    public void onChanged(String s) {
                        if (s.equals("success")){
                            createFarm1.setVisibility(View.GONE);
                            createFarm2.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), "Farm Create success! Please continue field creation", Toast.LENGTH_SHORT).show();
                        } else {
                            binding.promptText.setText("Farm Create Failed, Please try again or select an existed farm");
                            binding.promptText.setTextColor(getResources().getColor(R.color.red));
                            Toast.makeText(getActivity(), "Farm Create Failed, Please Try Again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentFarmName = farmNameText.getText().toString();
                        currentField=fieldNameText.getText().toString();
                        if (currentFarmName == null || currentFarmName.length() < 3 || currentFarmName.isEmpty()) {
                            binding.invalidFarm.setVisibility(View.VISIBLE);
                        } else if (currentField == null || currentField.length() < 3 || currentField.isEmpty()) {
                            binding.errorText.setVisibility(View.VISIBLE);
                        } else {
                            createFarm();
                        }
                    }
                });
            }
        });

        oldFarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change button color
                oldFarm.setBackground(getResources().getDrawable(R.drawable.create_button));
                oldFarm.setTextColor(getResources().getColor(R.color.background));
                newFarm.setBackground(getResources().getDrawable(R.drawable.action_button));
                newFarm.setTextColor(getResources().getColor(R.color.button));
                // initial spinner
                setFarmList(farmNameSpinner, farmNameList);
                farmNameText.setVisibility(View.GONE);
                farmNameSpinner.setVisibility(View.VISIBLE);
                binding.promptText.setText("Select \"New Farm\" to create farm");
                // when click next button
                nextButton.setOnClickListener(view1 -> {
                    currentFarmName = farmNameSpinner.getSelectedItem().toString();
                    currentField = fieldNameText.getText().toString();
                    if (currentField == null || currentField.length() < 3 || currentField.isEmpty()) {
                        binding.errorText.setVisibility(View.VISIBLE);
                    } else {
                        createFarm1.setVisibility(View.GONE);
                        createFarm2.setVisibility(View.VISIBLE);
                    }

                });

            }
        });



        // set map
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //async map
        setMap(mapView);


        // check whether the input text match constraint
        fieldNameText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String inputText = charSequence.toString();
                if (inputText.length() < 3 || !Character.isLetter(inputText.charAt(0))) {
                    // less than 3 letters
                    errorText.setVisibility(View.VISIBLE);
                } else {
                    // input match
                    errorText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        fieldCreateResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")) {
                    Toast.makeText(getActivity(), "Field Create Successfully!", Toast.LENGTH_SHORT).show();
                    farmField.refresh();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Farm Create failed, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!coordinateList.isEmpty()&&coordinateList.size()>2){
                    createField();
                } else {
                    Toast.makeText(getActivity(), "Please draw a valid field on map.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        oldFarm.callOnClick();
        return root;
    }



    private void createFarm() {
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_CreateFarm(currentFarmName);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/farm/addFarm", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                farmCreateResponse.postValue("success");

            }

            @Override
            public void onError(String errorMessage) {
                farmCreateResponse.postValue("fail");
            }
        });

    }

    private void createField() {
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_CreateField(currentField, currentFarmName, coordinateList);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/field/addField", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                fieldCreateResponse.postValue("success");
            }

            @Override
            public void onError(String errorMessage) {
                fieldCreateResponse.postValue("fail");
            }
        });
    }

    // initial spinner
    private void setFarmList(Spinner spinner, ArrayList<String> farmList) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, farmList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
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
        ImageButton clearButton = binding.clearButton;
        ImageButton drawingButton = binding.drawingButton;
        ImageButton moveButton = binding.moveButton;
        clearButton.setOnClickListener(view -> {
            googleMap.clear();
            coordinateList.clear();
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
                Log.i("coordinate", coordinateList.toString());
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
        adjustCameraToCentral();
    }

    private void adjustCameraToCentral() {
        //coordinate of uom
        LatLng coordinate = new LatLng(-37.798634, 144.960771);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
    }

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

    public void drawPolygonOnMap() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        currentField = null;
        currentFarmName = null;
        farmNameList = null;
        mapView = null;
        googleMap = null;
        currentPolygon = null;
        coordinateList = null;
        rc = null;
    }


}