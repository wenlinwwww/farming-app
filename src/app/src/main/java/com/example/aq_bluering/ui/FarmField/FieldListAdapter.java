package com.example.aq_bluering.ui.FarmField;

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

public class FieldListAdapter extends RecyclerView.Adapter<ListVH> {

    public List<String> fieldNames;
    public List<String> crop;
    public List<String> fieldIDs;
    FarmField farmField;
    public RequestConstructor rc;

    //TBD: pass view model into adapter, call viewModel delete request to database, fetching new list
    public FieldListAdapter(RequestConstructor rc,List<String> fieldNames, List<String> crop, List<String> fieldIDs, FarmField farmField) {
        this.fieldNames = fieldNames;
        this.crop = crop;
        this.fieldIDs = fieldIDs;
        this.farmField = farmField;
        this.rc=rc;
    }

    public FieldListAdapter() {
        this.fieldNames = new ArrayList<>();
        this.crop = new ArrayList<>();
        this.crop = new ArrayList<>();
    }

    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //linking view holder and adapter
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_farmfield, parent, false);
        return new ListVH(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        String name = fieldNames.get(position);
        String cropType = crop.get(position);

        if (name != null && name != "") {
            String fieldNameText = "Field name: " + name;
            String cropText = "Crop Type: " + cropType;
            String indexText = "# " + String.valueOf(position + 1);
            holder.fieldNameTextView.setText(fieldNameText);//set text for textview
            holder.corpTextView.setText(cropText);
            holder.idxTextView.setText(indexText);
        }

    }

    @Override
    public int getItemCount() {
        return fieldNames.size();
    }
}


class ListVH extends RecyclerView.ViewHolder {
    TextView idxTextView;
    TextView fieldNameTextView;
    TextView corpTextView;
    Button install;
    Button delete;

    private FieldListAdapter adapter;

    public ListVH(@NonNull View itemView) {
        super(itemView);
        idxTextView = itemView.findViewById(R.id.re_field_idx);
        fieldNameTextView = itemView.findViewById(R.id.re_field_name);
        corpTextView = itemView.findViewById(R.id.re_field_crop);
        delete = itemView.findViewById(R.id.re_field_button_delete);
        install = itemView.findViewById(R.id.re_field_button_install);

        delete.setOnClickListener(view -> {
            int position = getAdapterPosition();
            String fieldName=adapter.fieldNames.get(position);
            String msg ="Are you sure you want to delete this field?\n"+fieldName;
            DeleteConfirm delete = new DeleteConfirm(adapter.rc,adapter, position,msg, fieldName );
            delete.show(adapter.farmField.getChildFragmentManager(), "Field delete");
        });

        install.setOnClickListener(view -> {
            int position = getAdapterPosition();
            String fieldID = adapter.fieldIDs.get(position);
            String fieldPolygon=adapter.farmField.getFieldCoordinates(fieldID);
            SensorInstallation install = new SensorInstallation(adapter.rc,fieldID,fieldPolygon);
            install.show(adapter.farmField.getChildFragmentManager(), "install sensor");
        });
    }

    public ListVH linkAdapter(FieldListAdapter adapter) {
        this.adapter = adapter;
        return this;
    }
}