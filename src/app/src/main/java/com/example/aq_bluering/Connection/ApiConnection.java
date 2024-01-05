package com.example.aq_bluering.Connection;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiConnection {
    public interface ApiResponseCallback {
        void onResponse(String response);
        void onError(String errorMessage);
    }

    private String responseBody;
    public void putAsync(String json, String url, ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody rb = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .put(rb)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        assert response.body()!= null;
                        assert response.message()!=null;
                        Log.e("API Error body", "Status Code: " + statusCode+" "+response.message()+": "+response.body().string()+"\n"+response.toString());
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void postAsync(String json, String url, final ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody rb = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(rb)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        assert response.body()!= null;
                        assert response.message()!=null;
                        Log.e("API Error body", "Status Code: " + statusCode+" "+response.message()+": "+response.body().string());
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void deleteAsync(String url, final ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        assert response.body()!= null;
                        assert response.message()!=null;
                        Log.e("API Error body", "Status Code: " + statusCode+" "+response.message()+": "+response.body().string());
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }
    public void deleteAsync(String json, String url, final ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody rb = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .delete(rb)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        assert response.body()!= null;
                        assert response.message()!=null;
                        Log.e("API Error body", "Status Code: " + statusCode+" "+response.message()+": "+response.body().string());
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }
    public void postAsync_delete(String json, String url, final ApiResponseCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody rb = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .post(rb)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        assert response.body()!= null;
                        assert response.message()!=null;
                        Log.e("API Error body", "Status Code: " + statusCode+" "+response.message()+": "+response.body().string());
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public void postAsync(String json, String url, final ApiResponseCallback callback, String contentType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody rb = RequestBody.create(json, MediaType.parse(contentType));
                Request request = new Request.Builder()
                        .url(url)
                        .post(rb)
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        responseBody = response.body().string();
                        callback.onResponse(responseBody);
                    } else {
                        int statusCode = response.code();
                        Log.e("API Error", "Status Code: " + statusCode);
                        callback.onError("Request failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }).start();
    }


}
