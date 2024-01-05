package com.example.aq_bluering.ui.Irrigation;

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
import com.example.aq_bluering.databinding.FragmentIrrigationBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.ui.Sensor.SensorListAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import kotlinx.serialization.json.Json;


public class Irrigation extends Fragment {

    private FragmentIrrigationBinding binding;
    private String currentFarmName = null;
    private String currentFieldName = null;
    private IrrigationViewModel viewModel;
    private RecyclerView recyclerList;
    private IrrigationListAdapter adapter;
    private boolean zoneDataReady=false;

    private boolean fieldReady=false;
    private UsernameShare usernameShare;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(IrrigationViewModel.class);
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


        binding = FragmentIrrigationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        JsonParser jsonParser = new JsonParser();

        //portion to set up menu bar on top for selecting farm, field, and sensor
        Spinner farmSpinner = binding.farmList;
        Spinner fieldSpinner = binding.fieldList;

        Button createButton = binding.newZone;
        createButton.setOnClickListener(view -> {
            String zoneResponse=null;
            if (zoneDataReady){
                zoneResponse=viewModel.getZoneResponse().getValue();
            }
            ZoneCreation creation = new ZoneCreation(adapter.rc,viewModel.getFarmResponse().getValue(),
                    viewModel.getFieldResponse().getValue(),zoneResponse,this);
            creation.show(getChildFragmentManager(), "Zone Create");

        });

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

        //initial field list for a given farm name
        viewModel.getFieldResponse().observe(getViewLifecycleOwner(), s -> {
            fieldReady=true;
            ArrayList<String> fieldList = jsonParser.getValueListByName(s, "farm_name",
                    currentFarmName, "field_name");
            if (fieldList.isEmpty()) {
                fieldList.add("No Available Field");
            }
            setFieldList(fieldSpinner, fieldList);
        });
        //obtain irrigation zone information
        viewModel.getZoneResponse().observe(getViewLifecycleOwner(), s -> {
            ArrayList<LatLng> fieldPolygon=new ArrayList<>();
            if (viewModel.getFieldResponse().getValue()!=null&&!viewModel.getFieldResponse().getValue().isEmpty()
            &&currentFieldName!=null&&!currentFieldName.isEmpty()){
                String fieldPolygonJson = jsonParser.getMultipleValue(
                        viewModel.getFieldResponse().getValue(),"field_name",currentFieldName,"points");
                fieldPolygon = jsonParser.getCoordinates(fieldPolygonJson);
            }

            setRecyclerList(jsonParser, viewModel.getZoneResponse().getValue(),fieldPolygon);
            zoneDataReady=true;
        });

        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                currentFarmName = farmSpinner.getSelectedItem().toString();
                ArrayList<String> fields = new ArrayList<>();
                String response = viewModel.getFieldResponse().getValue();
                if (response != null && response != ""&&fieldReady) {
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
                currentFieldName = fieldSpinner.getSelectedItem().toString();
                String fieldPolygonJson = jsonParser.getMultipleValue(
                        viewModel.getFieldResponse().getValue(),"field_name",currentFieldName,"points");
                ArrayList<LatLng> fieldPolygon = jsonParser.getCoordinates(fieldPolygonJson);
                if(zoneDataReady){
                    setRecyclerList(jsonParser, viewModel.getZoneResponse().getValue(),fieldPolygon);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        refresh();

        return root;
    }

    public void refresh(){
        viewModel.fetchFarmList();
        viewModel.fetchFieldList();
        viewModel.fetchZoneDetail();
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

    private void setRecyclerList(JsonParser jsonParser, String response,ArrayList<LatLng> fieldPolygon) {
        binding.noData.setVisibility(View.GONE);
        ArrayList<String> zoneNames = jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "zonename");
        ArrayList<String> zoneCrops = jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "croptype");
        ArrayList<String> soil_25 = jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "soiltype_25");
        ArrayList<String> soil_75 = jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "soiltype_75");
        ArrayList<String> soil_125 = jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "soiltype_125");
        ArrayList<String> coordinatesJson= jsonParser.re_getValueListByName(response, "fieldname",
                currentFieldName, "points");
        if (zoneNames.isEmpty()) {
            recyclerList.swapAdapter(new IrrigationListAdapter(), true);
            binding.noData.setVisibility(View.VISIBLE);
        } else {
            adapter = new IrrigationListAdapter(viewModel.getRequestConstructor(),
                    currentFarmName,currentFieldName,zoneNames, zoneCrops, soil_25, soil_75,
                    soil_125, this);
            recyclerList.setAdapter(adapter);
        }
    }
    public IrrigationViewModel getViewModel(){
        return this.viewModel;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}