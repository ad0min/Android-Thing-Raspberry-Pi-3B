package com.khanhtran.lab3rc522;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Http{
    public interface Callback{
        void onSuccess(String response);
        void onError();
    }

    private RequestQueue queue;
    private String url = "http://demo1.chipfc.com/SensorValue/update?sensorid=7&sensorvalue=";

    public Http(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public void sendData(String data, final Callback callback){
        // Request a string response from the provided URL.
        String api = url + data;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                       callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}