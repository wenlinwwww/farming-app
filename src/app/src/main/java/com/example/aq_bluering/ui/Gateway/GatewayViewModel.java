package com.example.aq_bluering.ui.Gateway;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;

public class GatewayViewModel extends ViewModel {

    private RequestConstructor rc;

    private MutableLiveData<String> gatewayResponse = new MutableLiveData<>();

    public GatewayViewModel() {
    }
    public void setUsername(String username){
        this.rc= new RequestConstructor(username);
    }

    public RequestConstructor getRequestConstructor(){
        return this.rc;
    }
    public LiveData<String> getGatewayResponse() {
        return gatewayResponse;
    }

    public void fetchGatewayList() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchGateways();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/gateway", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                gatewayResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }

}