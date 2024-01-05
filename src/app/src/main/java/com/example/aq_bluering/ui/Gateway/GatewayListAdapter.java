package com.example.aq_bluering.ui.Gateway;

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

import java.util.List;

public class GatewayListAdapter extends RecyclerView.Adapter<ListVH> {

    public List<String> gateways;
    public Gateway gateway;

    public RequestConstructor rc;

    //TBD: pass view model into adapter, call viewModel delete request to database, fetching new list
    public GatewayListAdapter(RequestConstructor rc,List<String> gateways, Gateway gateway) {
        this.gateways = gateways;
        this.gateway = gateway;
        this.rc=rc;
    }

    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //linking view holder and adapter
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_gateway, parent, false);
        return new ListVH(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        String name = "";
        try {
            name = gateways.get(position);
        } catch (Exception e) {}
        if (name != null && name != "") {
            String gatewayText = "ID: " + name;
            String indexText = "# " + String.valueOf(position + 1);
            holder.gatewayTextView.setText(gatewayText);//set text for textview
            holder.idxTextView.setText(indexText);
        }
    }

    @Override
    public int getItemCount() {
        return gateways.size();
    }
}


class ListVH extends RecyclerView.ViewHolder {
    TextView idxTextView;
    TextView gatewayTextView;
    Button delete;
    private GatewayListAdapter adapter;

    public ListVH(@NonNull View itemView) {
        super(itemView);
        idxTextView = itemView.findViewById(R.id.re_gateway_idx);
        gatewayTextView = itemView.findViewById(R.id.re_gatewayID);
        //set actions for view (button, spinner, etc)
        delete = itemView.findViewById(R.id.re_gateway_button_delete);

        delete.setOnClickListener(view -> {
            int position = getAdapterPosition();
            String gatewayName = adapter.gateways.get(position);
            String msg ="Are you sure you want to delete this gateway?\n"+gatewayName;
            DeleteConfirm delete = new DeleteConfirm(adapter.rc,adapter,position,msg,gatewayName);
            delete.show(adapter.gateway.getChildFragmentManager(), "Gateway delete");
        });
    }

    public ListVH linkAdapter(GatewayListAdapter adapter) {
        this.adapter = adapter;
        return this;
    }
}