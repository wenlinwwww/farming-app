package com.example.aq_bluering.ui.Sensor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aq_bluering.R;
import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.databinding.FragmentSensorBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.ui.Irrigation.ZoneCreation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Sensor extends Fragment {
    private SensorViewModel viewModel;
    private FragmentSensorBinding binding;
    private String currentFarmName = null;
    private RecyclerView recyclerList;
    private SensorListAdapter adapter;
    private boolean fieldReady = false;
    private UsernameShare usernameShare;
    private String fieldID;
    private ArrayList<String> geom;
    private ArrayList<String> sensorIDs;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel =
                new ViewModelProvider(this).get(SensorViewModel.class);
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

        binding = FragmentSensorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        JsonParser jsonParser = new JsonParser();

        //portion to set up menu bar on top for selecting farm, field, and sensor
        Spinner farmSpinner = binding.farmList;
        Spinner fieldSpinner = binding.fieldList;
        recyclerList = binding.list;
        recyclerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.noData.setVisibility(View.GONE);

        //set up listener for receiving data response from api
        //initial farm list
        viewModel.getFarmResponse().observe(getViewLifecycleOwner(), s -> {
            ArrayList<String> farmList = jsonParser.getValueList(s, "farm_name");
            //default value for current selection of farm is the first farm (if any farm exists)
            if (farmList.isEmpty()) {
                farmList.add("No available farm");
            }
            setFarmList(farmSpinner, farmList);
        });
        viewModel.getSensorResponse().observe(getViewLifecycleOwner(),s -> {
            setRecyclerList(jsonParser,s);
            geom =jsonParser.re_getValueList(s,"points");
            sensorIDs=jsonParser.re_getValueList(s,"sensor_id");

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
        //obtain irrigation zone information
        viewModel.getSensorResponse().observe(getViewLifecycleOwner(), s -> {
            setRecyclerList(jsonParser, s);
        });

        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentFarmName = farmSpinner.getSelectedItem().toString();

                ArrayList<String> fields = new ArrayList<>();
                String response = viewModel.getFieldResponse().getValue();
                if (response != null && response != "" && fieldReady) {
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

        //when the current field selection changed, get a new sensor list
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String json = viewModel.getFieldResponse().getValue();
                String currentFieldName = fieldSpinner.getSelectedItem().toString();
                fieldID = jsonParser.getMultipleValue(json, "field_name",
                        currentFieldName, "field_id");
                viewModel.fetchSensorList(fieldID);
                recyclerList.swapAdapter(new SensorListAdapter(), true);
                binding.noData.setVisibility(View.VISIBLE);


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        Button createButton = binding.newSensor;
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SensorCreation creation = new SensorCreation(viewModel.getRequestConstructor(), fieldID,geom,sensorIDs,Sensor.this );
                creation.show(getChildFragmentManager(), "Sensor Create");
            }
        });

        viewModel.fetchFarmList();
        viewModel.fetchFieldList();

        return root;
    }
    public void refresh(){
        viewModel.fetchFarmList();
        viewModel.fetchFieldList();
        viewModel.fetchSensorList(fieldID);
    }
    private void setFarmList(Spinner spinner, ArrayList<String> farmList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_blue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
    }

    private void setFieldList(Spinner spinner, ArrayList<String> fieldList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_blue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(fieldList);
    }

    private void setRecyclerList(JsonParser jsonParser, String response) {

        binding.noData.setVisibility(View.GONE);
        ArrayList<String> sensorList = jsonParser.re_getValueList(response, "sensor_id");
        ArrayList<String> gatewayList = jsonParser.re_getValueList(response, "gateway_id");
        if (sensorList.isEmpty() || sensorList == null) {
            binding.noData.setVisibility(View.VISIBLE);
        } else {
            adapter = new SensorListAdapter(viewModel.getRequestConstructor(),sensorList, gatewayList, this);
            recyclerList.setAdapter(adapter);

        }
    }
    public SensorViewModel getViewModel(){
        return this.viewModel;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}