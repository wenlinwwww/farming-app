package com.example.aq_bluering.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;


import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.DeleteComfirmationMsgBinding;
import com.example.aq_bluering.ui.FarmField.FarmField;
import com.example.aq_bluering.ui.FarmField.FieldListAdapter;
import com.example.aq_bluering.ui.Gateway.GatewayListAdapter;
import com.example.aq_bluering.ui.Irrigation.IrrigationListAdapter;
import com.example.aq_bluering.ui.Sensor.SensorListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DeleteConfirm extends DialogFragment {

    private DeleteComfirmationMsgBinding binding;
    private Button confirmButton;
    private Button cancelButton;
    private RecyclerView.Adapter adapter;
    private int position;
    private String message;
    private String name;
    private String farmName = null;
    private FarmField farmField;
    private MutableLiveData<String> deleteResponse = new MutableLiveData<>();
    private MutableLiveData<String> deleteFail = new MutableLiveData<>();

    private RequestConstructor rc;

    public DeleteConfirm(RequestConstructor rc,RecyclerView.Adapter adapter, int position, String message, String name) {
        this.name = name;
        this.adapter = adapter;
        this.position = position;
        this.message = message;
        this.rc=rc;
    }

    public DeleteConfirm(RequestConstructor rc,String farmName, FarmField farmField, String message) {
        this.rc=rc;
        this.message = message;
        this.farmField = farmField;
        this.farmName = farmName;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DeleteConfirmationTheme);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DeleteComfirmationMsgBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        confirmButton = binding.confirmButton;
        cancelButton = binding.cancelButton;
        binding.message.setText(message);
        binding.background.setOnClickListener(view -> dismiss());
        cancelButton.setOnClickListener(view -> dismiss());

        deleteResponse.observe(getViewLifecycleOwner(), s -> {
            if (s.equals("farm")) {
                name=farmName;
                farmField.refresh();
            } else if (s.equals("field")) {
                FieldListAdapter fieldListAdapter = (FieldListAdapter) adapter;
                fieldListAdapter.crop.remove(position);
                fieldListAdapter.fieldNames.remove(position);
                fieldListAdapter.fieldIDs.remove(position);
                fieldListAdapter.notifyItemRemoved(position);
                dismiss();
                if (position != fieldListAdapter.getItemCount()) {
                    int itemCount = fieldListAdapter.getItemCount() - position;
                    fieldListAdapter.notifyItemRangeChanged(position, itemCount);
                }
            } else if (s.equals("gateway")) {
                GatewayListAdapter gatewayListAdapter = (GatewayListAdapter) adapter;
                gatewayListAdapter.gateways.remove(position);
                gatewayListAdapter.notifyItemRemoved(position);
                if (position != gatewayListAdapter.getItemCount()) {
                    int itemCount = gatewayListAdapter.getItemCount() - position;
                    gatewayListAdapter.notifyItemRangeChanged(position, itemCount);
                }
            } else if (s.equals("zone")) {
                IrrigationListAdapter irrigationListAdapter = (IrrigationListAdapter) adapter;
                irrigationListAdapter.zoneNames.remove(position);
                irrigationListAdapter.cropTypes.remove(position);
                irrigationListAdapter.soil_25.remove(position);
                irrigationListAdapter.soil_75.remove(position);
                irrigationListAdapter.soil_125.remove(position);
                irrigationListAdapter.notifyItemRemoved(position);
                if (position != irrigationListAdapter.getItemCount()) {
                    int itemCount = irrigationListAdapter.getItemCount() - position;
                    irrigationListAdapter.notifyItemRangeChanged(position, itemCount);
                }
            } else if (s.equals("sensor")) {
                SensorListAdapter sensorListAdapter = (SensorListAdapter) adapter;
                sensorListAdapter.sensors.remove(position);
                sensorListAdapter.gateways.remove(position);
                sensorListAdapter.notifyItemRemoved(position);
                if (position != sensorListAdapter.getItemCount()) {
                    int itemCount = sensorListAdapter.getItemCount() - position;
                    sensorListAdapter.notifyItemRangeChanged(position, itemCount);
                }
            }
            String msg = "Delete "+s+": " + name + " successfully!";
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            dismiss();
        });
        deleteFail.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("farm")){
                    name=farmName;
                }
                String msg = "Fail to delete "+s+": " + name + ", please try again!";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        confirmButton.setOnClickListener(view -> {
            if (adapter != null || farmName != null) {
                binding.deleting.setVisibility(View.VISIBLE);
                removeAction();
            }
        });


        getDialog().setCanceledOnTouchOutside(true);
        return root;
    }

    private void removeAction() {
        if (farmName != null & farmField != null) {
            String url = rc.getUrl_delete_farm(farmName);
            ApiConnection apiConnection = new ApiConnection();
            apiConnection.deleteAsync(url, new ApiConnection.ApiResponseCallback() {
                @Override
                public void onResponse(String response) {
                    deleteResponse.postValue("farm");
                }

                @Override
                public void onError(String errorMessage) {
                    deleteFail.postValue("farm");
                }
            });
        } else {
            if (adapter instanceof FieldListAdapter) {
                FieldListAdapter fieldListAdapter = (FieldListAdapter) adapter;
                String fieldId = fieldListAdapter.fieldIDs.get(position);
                String url = rc.getUrl_delete_field(fieldId);
                ApiConnection apiConnection = new ApiConnection();
                apiConnection.deleteAsync(url, new ApiConnection.ApiResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        deleteResponse.postValue("field");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        deleteFail.postValue("field");
                    }
                });
            } else if (adapter instanceof GatewayListAdapter) {
                GatewayListAdapter gatewayListAdapter = (GatewayListAdapter) adapter;
                String gatewayID = gatewayListAdapter.gateways.get(position);
                String url = rc.getUrl_delete_gateway();
                String json = rc.getJson_delete_gateway(gatewayID);
                ApiConnection apiConnection = new ApiConnection();
                apiConnection.postAsync(json, url, new ApiConnection.ApiResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        deleteResponse.postValue("gateway");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        deleteFail.postValue("gateway");
                    }
                });
            } else if (adapter instanceof IrrigationListAdapter) {
                IrrigationListAdapter irrigationListAdapter = (IrrigationListAdapter) adapter;
                String fieldName = irrigationListAdapter.fieldName;
                String zoneName = ((IrrigationListAdapter) adapter).zoneNames.get(position);
                String json = rc.getJson_delete_zone(fieldName, zoneName);
                String url = rc.getUrl_delete_zone();
                ApiConnection apiConnection = new ApiConnection();
                apiConnection.deleteAsync(json, url, new ApiConnection.ApiResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        deleteResponse.postValue("zone");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        deleteFail.postValue("zone");
                    }
                });
            } else if (adapter instanceof SensorListAdapter) {
                SensorListAdapter sensorListAdapter = (SensorListAdapter) adapter;
                String sensorID = sensorListAdapter.sensors.get(position);
                String url = rc.getUrl_delete_sensor(sensorID);
                ApiConnection apiConnection = new ApiConnection();
                apiConnection.deleteAsync(url, new ApiConnection.ApiResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        deleteResponse.postValue("sensor");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        deleteFail.postValue("sensor");
                    }
                });
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
