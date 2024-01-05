package com.example.aq_bluering.ui.Gateway;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aq_bluering.UsernameShare;
import com.example.aq_bluering.databinding.FragmentGatewayBinding;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.ui.FarmField.FarmFieldCreation;

import java.util.ArrayList;

public class Gateway extends Fragment {

    private FragmentGatewayBinding binding;
    private GatewayViewModel viewModel;
    private GatewayListAdapter adapter;
    private RecyclerView recyclerList;
    private UsernameShare usernameShare;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GatewayViewModel.class);
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


        binding = FragmentGatewayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        JsonParser jsonParser = new JsonParser();
        Button createButton = binding.createGateway;
        createButton.setOnClickListener(view -> {
            GatewayCreation creation = new GatewayCreation(viewModel.getRequestConstructor(),this);
            creation.show(getChildFragmentManager(), "Gateway Create");
        });
        recyclerList = binding.list;
        recyclerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.noData.setVisibility(View.GONE);

        //set gateway for a given farm name
        viewModel.getGatewayResponse().observe(getViewLifecycleOwner(), s -> {
            setRecyclerList(jsonParser, s);
        });

        //get farm & field data from API
       refresh();
        return root;
    }

    public void refresh(){
        viewModel.fetchGatewayList();
    }


    private void setRecyclerList(JsonParser jsonParser, String response) {
        binding.noData.setVisibility(View.GONE);
        ArrayList<String> gatewayIds = jsonParser.re_getValueList(response, "gateway_id");

        if (gatewayIds.isEmpty()) {
            binding.noData.setVisibility(View.VISIBLE);
        } else {
            adapter=new GatewayListAdapter(viewModel.getRequestConstructor(),gatewayIds,this);
            recyclerList.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}