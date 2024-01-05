package com.example.aq_bluering.ui.Irrigation;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;

public class IrrigationViewModel extends ViewModel {



    private MutableLiveData<String> farmResponse = new MutableLiveData<>();
    private MutableLiveData<String> fieldResponse = new MutableLiveData<>();
    private MutableLiveData<String> zoneResponse = new MutableLiveData<>();
    private RequestConstructor rc;

    public IrrigationViewModel() {}
    public void setUsername(String username){
        this.rc= new RequestConstructor(username);
    }

    public RequestConstructor getRequestConstructor(){
        return this.rc;
    }
    public LiveData<String> getZoneResponse() {
        return zoneResponse;
    }
    public LiveData<String> getFarmResponse() {
        return farmResponse;
    }
    public LiveData<String> getFieldResponse() {
        return fieldResponse;
    }

    //methods to obtain data for menu
    public void fetchFarmList() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchFarms();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/farm", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                farmResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }
    public void fetchFieldList() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchFields();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                fieldResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }
    public void fetchZoneDetail() {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json =rc.getJson_fetchZones();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/zone", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                zoneResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.i("Connection Error", errorMessage);
            }
        });
    }
}