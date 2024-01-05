package com.example.aq_bluering.Connection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApi {
    public interface ApiResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    private String responseBody;

    public void getAsync(double latitude, double longitude,String link, final ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(link).newBuilder();
                urlBuilder.addQueryParameter("lat", String.valueOf(latitude));
                urlBuilder.addQueryParameter("lon", String.valueOf(longitude));
                urlBuilder.addQueryParameter("units", "metric");
                urlBuilder.addQueryParameter("appid", "46f80a02ecae410460d59960ded6e1c6");
                String url = urlBuilder.build().toString();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public String getWeatherApiMainValue(String json, String endpoint) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            // get "main" content
            JsonObject mainObject = jsonObject.getAsJsonObject("main");

            // get "Temp" value
            if (mainObject != null && mainObject.has(endpoint)) {
                return mainObject.get(endpoint).getAsString();
            } else {
                // if not exist, throw e
                throw new IllegalStateException("Temp not found in weatherApi");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getWeatherTimeZone(String json, String endpoint) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            JsonObject mainObject = jsonObject.getAsJsonObject();
            if (mainObject != null) {
                return mainObject.get(endpoint).getAsString();
            } else {
                // if not exist, throw e
                throw new IllegalStateException("time zone not found in weatherApi");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}

