package com.example.aq_bluering.ui.Dashboard.Chart;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChartViewModel extends ViewModel {

    private MutableLiveData<String> responseData = new MutableLiveData<>();
    private RequestConstructor rc;

    public LiveData<String> getResponseData() {
        return responseData;
    }

    private MutableLiveData<String> sensorDetailsResponse = new MutableLiveData<>();

    public LiveData<String> getSensorDetailsResponse() {
        return sensorDetailsResponse;
    }

    //for menu
    private MutableLiveData<String> farmResponse = new MutableLiveData<>();
    private MutableLiveData<String> fieldResponse = new MutableLiveData<>();
    private MutableLiveData<String> sensorResponse = new MutableLiveData<>();

    public LiveData<String> getFarmResponse() {
        return farmResponse;
    }

    public LiveData<String> getFieldResponse() {
        return fieldResponse;
    }

    public LiveData<String> getSensorResponse() {
        return sensorResponse;
    }

    private MutableLiveData<String> weatherResponse = new MutableLiveData<>();

    public LiveData<String> getWeatherResponse() {
        return weatherResponse;
    }

    public void setUsername(String username){
        this.rc= new RequestConstructor(username);
    }

    public RequestConstructor getRequestConstructor(){
        return this.rc;
    }
    //end
    void fetchWeatherData(ArrayList<LatLng> polygonPoints) {
        // Create a Calendar instance
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -6); // Subtracting 6 days to account for tomorrow
        Date begDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String beginDateString = sdf.format(begDate);
        String endDateString = sdf.format(endDate);

        String sensorLocation = processCoordinateString(polygonPoints);
        OkHttpClient client = new OkHttpClient();
        if (sensorLocation == null) {
            sensorLocation = "melbourne";
        }
        String urlString = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                sensorLocation + "/" + beginDateString + "/" + endDateString +
                "?unitGroup=metric&key=BFHRKMHQ9YWU5GLMGLNPW4P6X&include=days&contentType=json";
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    weatherResponse.postValue(responseData);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error in retrieving precipitation", e.getMessage());
            }
        });
    }

    private String processCoordinateString(ArrayList<LatLng> polygonPoints) {
        String coordinateString = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : polygonPoints) {
            builder.include(point);
        }
        try {
            LatLngBounds bounds = builder.build();
            LatLng center = bounds.getCenter();
            double centralLatitude = center.latitude;
            double centralLongitude = center.longitude;
            coordinateString = String.format("%.4f", centralLatitude) + "," + String.format("%.4f", centralLongitude);
        } catch (Exception e) {
            Log.e("invalid coordinates for current field", e.getMessage());
        }
        return coordinateString;
    }

    public void fetchEvaFromApi(String fieldID) {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchEvaporation(fieldID);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/evaporation", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                responseData.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Connection Error", errorMessage);
            }
        });
    }

    //get all sensor's data within a given number of days starting from today (e.g. 5 means from 5 days ago to today)
    public void fetchSensorDetails(String fieldName, int numberOfDays) {
        ApiConnection apiConnection = new ApiConnection();
        //fetch past 24 hour's data
        String json = rc.getJson_fetchSensorMoisture(fieldName);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/moisture", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                sensorDetailsResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Connection Error", errorMessage);
            }
        });
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
                Log.e("Connection Error", errorMessage);
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
                Log.e("Connection Error", errorMessage);
            }
        });
    }

    public void fetchSensorList(String fieldID) {
        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchSensors(fieldID);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                sensorResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Connection Error", errorMessage);
            }
        });
    }
    //end here


}