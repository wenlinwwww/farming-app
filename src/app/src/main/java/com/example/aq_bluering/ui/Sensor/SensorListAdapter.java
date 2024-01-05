package com.example.aq_bluering.ui.Sensor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.ui.DeleteConfirm;
import com.example.aq_bluering.R;

import java.util.ArrayList;
import java.util.List;

public class SensorListAdapter extends RecyclerView.Adapter<ListVH> {
    public List<String> sensors;
    public List<String> gateways;
    Sensor sensor;
    public RequestConstructor rc;

    //TBD: pass view model into adapter, call viewModel delete request to database, fetching new list
    public SensorListAdapter(RequestConstructor rc,List<String> sensors, List<String> gateways, Sensor sensor) {
        this.sensors = sensors;
        this.gateways = gateways;
        this.sensor = sensor;
        this.rc=rc;
    }

    public SensorListAdapter(){
        this.sensors=new ArrayList<>();
        this.gateways=new ArrayList<>();
    }

    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //linking view holder and adapter
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_sensor, parent, false);
        return new ListVH(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        String name = "";
        try {
            name = sensors.get(position);
        } catch (Exception e) {
        }
        if (name != null && name != "") {
            String sensorText = "Sensor ID: " + name;
            String gatewayText = "Gateway: " + gateways.get(position);
            String indexText = "# " + String.valueOf(position + 1);
            holder.sensorTextView.setText(sensorText);
            holder.gatewayTextView.setText(gatewayText);//set text for textview
            holder.idxTextView.setText(indexText);
        }

    }

    @Override
    public int getItemCount() {
        return sensors.size();
    }
}


class ListVH extends RecyclerView.ViewHolder {
    TextView idxTextView;
    TextView sensorTextView;
    TextView gatewayTextView;
    Button delete;
    Button edit;
    private SensorListAdapter adapter;

    public ListVH(@NonNull View itemView) {
        super(itemView);
        idxTextView = itemView.findViewById(R.id.re_sensor_idx);
        sensorTextView = itemView.findViewById(R.id.re_sensor_name);
        gatewayTextView = itemView.findViewById(R.id.re_sensor_gateway);
        //set actions for view (button, spinner, etc)
        delete = itemView.findViewById(R.id.re_Sensor_button_delete);
        edit = itemView.findViewById(R.id.re_Sensor_button_edit);

        //delete item (TBD: send delete request to database
        delete.setOnClickListener(view -> {
            int position = getAdapterPosition();
            String sensorName=adapter.sensors.get(position);
            String msg="Are you sure you want to delete this sensor?\n"+sensorName;
            DeleteConfirm delete = new DeleteConfirm(adapter.rc,adapter,position,msg,sensorName);
            delete.show(adapter.sensor.getChildFragmentManager(), "Sensor delete");

        });

        //edit item (TBD: open a new page with selected sensor current information-allow edit text, map location show as marker-place new marker
        edit.setOnClickListener(view -> {
            int position = getAdapterPosition();
            SensorEditing editing = new SensorEditing(adapter.rc,adapter,position);
            editing.show(adapter.sensor.getChildFragmentManager(), "Edit Sensor");

        });
    }

    public ListVH linkAdapter(SensorListAdapter adapter) {
        this.adapter = adapter;
        return this;
    }
}