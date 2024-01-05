package com.example.aq_bluering.ui.FarmField;

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

import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.ui.DeleteConfirm;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.FragmentFarmfieldBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.ui.Sensor.SensorListAdapter;

import java.util.ArrayList;

public class FarmField extends Fragment {

    private FragmentFarmfieldBinding binding;
    private ArrayList<String> farmList = new ArrayList<>();
    private String currentFarmName = null;
    private FarmFieldViewModel viewModel;
    private FieldListAdapter adapter;
    private RecyclerView recyclerList;
    private boolean fieldReady = false;
    private UsernameShare usernameShare;
    private String userName=null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FarmFieldViewModel.class);
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

        binding = FragmentFarmfieldBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        JsonParser jsonParser = new JsonParser();
        Spinner farmSpinner = binding.farmList;
        Button createButton = binding.newField;
        Button deleteButton=binding.deleteButton;
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FarmFieldCreation creation = new FarmFieldCreation(viewModel.getRequestConstructor(),farmList,FarmField.this);
                creation.show(getChildFragmentManager(), "FarmFieldCreate");
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFarmName!=null&&!currentFarmName.equals("No available farm")){
                    String message="Are you sure you want to delete this farm?\n"+currentFarmName;
                    DeleteConfirm delete = new DeleteConfirm(viewModel.getRequestConstructor(),currentFarmName,FarmField.this,message);
                    delete.show(getChildFragmentManager(), "Farm delete");
                }

            }
        });
        recyclerList = binding.list;
        recyclerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.noData.setVisibility(View.GONE);
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
            setRecyclerList(jsonParser, s);
        });

        //set up menu selection listener
        // set when a farm name selection changed, update field list
        farmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (viewModel.getFieldResponse() != null) {
                    currentFarmName = farmSpinner.getSelectedItem().toString();
                    setRecyclerList(jsonParser, viewModel.getFieldResponse().getValue());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        //get farm & field data from API
        refresh();
        return root;
    }

    public void refresh(){
        //get farm & field data from API
        viewModel.fetchFarmList();
        viewModel.fetchFieldList();
    }

    private void setFarmList(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_option_blue);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(farmList);
    }

    private void setRecyclerList(JsonParser jsonParser, String response) {
        if (fieldReady){
            ArrayList<String> fieldNames = jsonParser.re_getValueListByName(response, "farm_name",
                    currentFarmName, "field_name");
            ArrayList<String> fieldCorps = jsonParser.re_getValueListByName(response, "farm_name",
                    currentFarmName, "crop_type");
            ArrayList<String> fieldIDs = jsonParser.re_getValueListByName(response, "farm_name",
                    currentFarmName, "field_id");
            if (fieldNames.isEmpty()) {
                recyclerList.swapAdapter(new FieldListAdapter(), true);
                binding.noData.setVisibility(View.VISIBLE);
            } else {
                binding.noData.setVisibility(View.GONE);
                adapter = new FieldListAdapter(viewModel.getRequestConstructor(),fieldNames, fieldCorps, fieldIDs, this);
                recyclerList.setAdapter(adapter);
            }
        }
    }


    public String getFieldCoordinates(String fieldID){
        JsonParser jsonParser = new JsonParser();
        String json = viewModel.getFieldResponse().getValue();
        String fieldPolygon = jsonParser.getMultipleValue(json, "field_id", fieldID, "points");
        return  fieldPolygon;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}