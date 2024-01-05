package com.example.aq_bluering.ui.Gateway;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.aq_bluering.databinding.GatewayCreateFragmentBinding;
import com.example.aq_bluering.databinding.ProfileFragmentBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GatewayCreation extends DialogFragment {

    private GatewayCreateFragmentBinding binding;
    private MapView mapView;
    private GoogleMap googleMap;
    private String currentGateway = null;
    private LatLng currentPoint = null;

    private MutableLiveData<String> submissionResponse = new MutableLiveData<>();
    private RequestConstructor rc;
    private Gateway gateway;

    public GatewayCreation(RequestConstructor rc, Gateway gateway) {
        this.gateway = gateway;
        this.rc = rc;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = GatewayCreateFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // close dialogFragment, when click back btn
        binding.backButton.setOnClickListener(view -> dismiss());

        // check input error
        ConstraintLayout createGateway1 = binding.createGateway1;
        ConstraintLayout createGateway2 = binding.createGateway2;
        EditText gatewayInput = binding.gatewayInput;
        AppCompatButton nextButton = binding.nextButton;
        AppCompatButton submitButton = binding.submitButton;
        TextView gatewayNote2 = binding.gatewayNote2;
        TextView error = binding.gatewayErrorText;

        // highlight example
        String text = "• Example: AquaTerraGateway909a56";
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.button));
        int startIndex = text.indexOf("A");
        int endIndex = text.length();
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        gatewayNote2.setText(spannableString);

        // check whether the input match constraint
        gatewayInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = editable.toString();
                String pattern = "^AquaTerraGateway[0-9a-zA-Z]{6}$";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(inputText);
                if (m.find()) {
                    // match
                    error.setVisibility(View.GONE);

                } else {
                    // not match
                    error.setVisibility(View.VISIBLE);
                }
            }
        });

        nextButton.setOnClickListener(view -> {
            currentGateway = gatewayInput.getText().toString();
            String pattern = "^AquaTerraGateway[0-9a-zA-Z]{6}$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(currentGateway);
            if (currentGateway.isEmpty() || !m.find()) {
                error.setVisibility(View.VISIBLE);
            } else {
                error.setVisibility(View.GONE);
                createGateway1.setVisibility(View.GONE);
                createGateway2.setVisibility(View.VISIBLE);
            }
        });

        submissionResponse.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")) {
                    Toast.makeText(getActivity(), "Gateway Create Successfully!", Toast.LENGTH_SHORT).show();
                    gateway.refresh();
                    dismiss();
                } else {
                    Toast.makeText(getActivity(), "Gateway Create Failed, Please Try Again!", Toast.LENGTH_SHORT).show();
                }
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
        ImageButton clearButton = binding.clearButton;
        ImageButton drawingButton = binding.drawingButton;
        ImageButton moveButton = binding.moveButton;
        clearButton.setOnClickListener(view -> {
            googleMap.clear();
        });

        drawingButton.setOnClickListener(view -> {
            //set background color to show status
            //set current clicked button to grey, resume other button to white
            drawingButton.setBackgroundColor(getResources().getColor(R.color.grey_boarder));
            moveButton.setBackgroundColor(getResources().getColor(R.color.background));

            //set click map to place marker and draw polygon
            googleMap.setOnMapClickListener(latLng -> {
                if (currentPoint != null) {
                    googleMap.clear(); // clean previous point
                }
                googleMap.addMarker(new MarkerOptions().position(latLng));
                currentPoint = latLng;
                Log.i("currentPoint", currentPoint.toString());
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

        submitButton.setOnClickListener(view -> {
            if (currentPoint != null) {
                ApiConnection apiConnection = new ApiConnection();

                String gatewayJson = rc.getJson_AddGateway(currentGateway, currentPoint);
                apiConnection.postAsync(gatewayJson, "https://webapp.aquaterra.cloud/api/gateway/new", new ApiConnection.ApiResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        submissionResponse.postValue("success");
                        Log.i("gateway create success", response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        submissionResponse.postValue("fail");
                        Log.i("gateway create fail", errorMessage);

                    }
                });
            } else {
                Toast.makeText(getActivity(), "Please mark this gateway location on map!", Toast.LENGTH_SHORT).show();
            }


        });

        return root;
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
        adjustCameraToCentral();
    }

    private void adjustCameraToCentral() {
        //coordinate of uom
        LatLng coordinate = new LatLng(-37.798634, 144.960771);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
    }

    public void setMap(MapView mapView) {
        //when loading map
        mapView.getMapAsync(map -> {
            googleMap = map;
            init_map_setting();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mapView = null;
        googleMap = null;
        rc = null;
    }
}