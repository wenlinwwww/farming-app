package com.example.aq_bluering.ui.Dashboard.Detail;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.aq_bluering.Connection.ApiConnection;
import com.example.aq_bluering.Connection.JsonParser;
import com.example.aq_bluering.Connection.RequestConstructor;
import com.example.aq_bluering.Connection.WeatherApi;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailViewModel extends ViewModel {

    private RequestConstructor rc;
    private MutableLiveData<String> sensorDetailResponse = new MutableLiveData<>();
    private MutableLiveData<String> aliasData = new MutableLiveData<>();
    private MutableLiveData<String> farmResponse = new MutableLiveData<>();
    private MutableLiveData<String> fieldResponse = new MutableLiveData<>();
    private MutableLiveData<String> sensorResponse = new MutableLiveData<>();
    private MutableLiveData<String> weatherData = new MutableLiveData<>();
    private MutableLiveData<String> locationResponse = new MutableLiveData<>();
    private MutableLiveData<String> fiveDayWeatherResponse = new MutableLiveData<>();

    public LiveData<String> getFarmResponse() {
        return farmResponse;
    }

    public LiveData<String> getFieldResponse() {
        return fieldResponse;
    }

    public LiveData<String> getSensorResponse() {
        return sensorResponse;
    }

    public LiveData<String> getSensorDetailResponse() {
        return sensorDetailResponse;
    }

    public LiveData<String> getAliasData() {
        return aliasData;
    }

    public LiveData<String> getWeatherData() {
        return weatherData;
    }

    public LiveData<String> getLocationData() {
        return locationResponse;
    }

    public LiveData<String> getFiveDayData() {
        return fiveDayWeatherResponse;
    }

    public void setUsername(String username){
        this.rc= new RequestConstructor(username);
    }

    public RequestConstructor getRequestConstructor(){
        return this.rc;
    }

    public void fetchSensorDetails(String sensorId, String fieldName) {


        // connection request
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchSensorDetail(sensorId,fieldName);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/moisture", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                sensorDetailResponse.postValue(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Connection Error", errorMessage);
            }
        });
    }

    public void fetchAliasFromApi(String fieldID) {
        ApiConnection apiConnection = new ApiConnection();
        String json = rc.getJson_fetchSensors(fieldID);
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                aliasData.postValue(response);
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

    public void fetchWeatherData(String fieldId, String sensorId) {
        // get data from openWeatherApi
        String json = rc.getJson_fetchSensors(fieldId);
        ApiConnection apiConnection = new ApiConnection();
        WeatherApi weatherApi = new WeatherApi();
        JsonParser jsonParser = new JsonParser();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                String weatherResponse = jsonParser.getMultipleValue(response, "sensor_id", sensorId, "points");
                LocationData locationData = LocationData.fromJson(weatherResponse);
                double longitude = locationData.getLongitude();
                double latitude = locationData.getLatitude();
                // update LiveData
                if (response != null) {
                    String link = "https://api.openweathermap.org/data/2.5/weather";
                    weatherApi.getAsync(latitude, longitude, link, new WeatherApi.ApiResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            weatherData.postValue(response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("WeatherApiError", errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("fieldID-connection-error", errorMessage);
            }
        });
    }

    public void fetchLocationData(String fieldId, String sensorId) {
        // get data from openWeatherApi
        String json = rc.getJson_fetchSensors(fieldId);
        ApiConnection apiConnection = new ApiConnection();
        WeatherApi weatherApi = new WeatherApi();
        JsonParser jsonParser = new JsonParser();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                String weatherResponse = jsonParser.getMultipleValue(response, "sensor_id", sensorId, "points");
                LocationData locationData = LocationData.fromJson(weatherResponse);
                double longitude = locationData.getLongitude();
                double latitude = locationData.getLatitude();
                // update LiveData
                if (response != null) {
                    String link = "https://api.openweathermap.org/data/2.5/onecall";
                    weatherApi.getAsync(latitude, longitude, link, new WeatherApi.ApiResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            locationResponse.postValue(response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("WeatherApiError", errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("fieldID-connection-error", errorMessage);
            }
        });
    }

    public void fetchFiveDayWeather(String fieldId, String sensorId) {
        // get data from openWeatherApi
        String json = rc.getJson_fetchSensors(fieldId);
        ApiConnection apiConnection = new ApiConnection();
        WeatherApi weatherApi = new WeatherApi();
        JsonParser jsonParser = new JsonParser();
        apiConnection.postAsync(json, "https://webapp.aquaterra.cloud/api/sensor/field", new ApiConnection.ApiResponseCallback() {
            @Override
            public void onResponse(String response) {
                String weatherResponse = jsonParser.getMultipleValue(response, "sensor_id", sensorId, "points");
                LocationData locationData = LocationData.fromJson(weatherResponse);
                double longitude = locationData.getLongitude();
                double latitude = locationData.getLatitude();
                // update LiveData
                if (response != null) {
                    String link = "https://api.openweathermap.org/data/2.5/forecast";
                    weatherApi.getAsync(latitude, longitude, link, new WeatherApi.ApiResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            fiveDayWeatherResponse.postValue(response);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("FiveDayDataError", errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("fiveday-connection-error", errorMessage);
            }
        });
    }
}