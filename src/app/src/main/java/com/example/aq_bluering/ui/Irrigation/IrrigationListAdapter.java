package com.example.aq_bluering.ui.Irrigation;

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
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class IrrigationListAdapter extends RecyclerView.Adapter<ListVH> {
    public  String farmName = null;
    public String fieldName=null;
    public List<String> zoneNames;
    public List<String> cropTypes;
    public List<String> soil_25;
    public List<String> soil_75;
    public List<String> soil_125;
    Irrigation zone;

    public RequestConstructor rc;

    //TBD: pass view model into adapter, call viewModel delete request to database, fetching new list
    public IrrigationListAdapter(RequestConstructor rc,String farmName,String fieldName,List<String> zoneNames,
                                 List<String> cropTypes, List<String> soil_25, List<String> soil_75,
                                 List<String> soil_125, Irrigation zone) {
        this.fieldName=fieldName;
        this.farmName = farmName;
        this.zoneNames = zoneNames;
        this.cropTypes = cropTypes;
        this.soil_25 = soil_25;
        this.soil_75 = soil_75;
        this.soil_125 = soil_125;
        this.zone =zone;
        this.rc=rc;
    }

    public IrrigationListAdapter() {
        this.zoneNames = new ArrayList<>();
        this.cropTypes =  new ArrayList<>();
        this.soil_25 =  new ArrayList<>();
        this.soil_75 =  new ArrayList<>();
        this.soil_125 =  new ArrayList<>();
    }



    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //linking view holder and adapter
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_irrigation, parent, false);
        return new ListVH(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        String name = zoneNames.get(position);
        if (name != null && name != "No data" && name != "") {
            String zoneText = "ID: " + name;
            String indexText = "# " + String.valueOf(position + 1);
            String cropText = "Crop Type: " + cropTypes.get(position);
            String soil_25Text = "25cm: " + soil_25.get(position);
            String soil_75Text = "75cm: " + soil_75.get(position);
            String soil_125Text = "125cm: " + soil_125.get(position);
            holder.zoneTextView.setText(zoneText);//set text for textview
            holder.idxTextView.setText(indexText);
            holder.cropTextView.setText(cropText);
            holder.soil_25TextView.setText(soil_25Text);
            holder.soil_75TextView.setText(soil_75Text);
            holder.soil_125TextView.setText(soil_125Text);

        }

    }


    @Override
    public int getItemCount() {
        return zoneNames.size();
    }
}


class ListVH extends RecyclerView.ViewHolder {
    TextView idxTextView;
    TextView zoneTextView;
    TextView cropTextView;
    TextView soil_25TextView;
    TextView soil_75TextView;
    TextView soil_125TextView;
    Button delete;
    Button edit;
    private IrrigationListAdapter adapter;

    public ListVH(@NonNull View itemView) {
        super(itemView);
        idxTextView = itemView.findViewById(R.id.re_zone_idx);
        zoneTextView = itemView.findViewById(R.id.re_zone_name);
        cropTextView = itemView.findViewById(R.id.re_zone_crop);
        soil_25TextView = itemView.findViewById(R.id.re_zone_soil_25);
        soil_75TextView = itemView.findViewById(R.id.re_zone_soil_75);
        soil_125TextView = itemView.findViewById(R.id.re_zone_soil_125);
        //set actions for view (button, spinner, etc)
        delete = itemView.findViewById(R.id.re_zone_button_delete);
        edit = itemView.findViewById(R.id.re_zone_button_edit);

        delete.setOnClickListener(view -> {
            int position=getAdapterPosition();
            String zoneName = adapter.zoneNames.get(position);
            String msg = "Are you sure you want to delete this zone?\n"+zoneName;
            DeleteConfirm delete = new DeleteConfirm(adapter.rc,adapter,position,msg,zoneName);
            delete.show(adapter.zone.getChildFragmentManager(), "Zone delete");
        });

        edit.setOnClickListener(view -> {
            int position = getAdapterPosition();
            ZoneEditing editing = new ZoneEditing(adapter.rc,adapter,position );
            editing.show(adapter.zone.getChildFragmentManager(), "Edit Zone");
        });
    }

    public ListVH linkAdapter(IrrigationListAdapter adapter) {
        this.adapter = adapter;
        return this;
    }
}