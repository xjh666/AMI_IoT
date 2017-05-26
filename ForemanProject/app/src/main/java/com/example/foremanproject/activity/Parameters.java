package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.foremanproject.R;
import com.example.foremanproject.other.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/25/2017.
 */

public class Parameters extends AppCompatActivity {
    private static int id;
    private static String title;
    private static String type;
    private HashMap<String, ArrayList<JSONObject>> parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters);
        setTitle(title);
        parameters = new HashMap<>();
        sendRequestToGetParameters();
    }

    private void sendRequestToGetParameters(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String api;

        if(type.equals("HOSTGROUPS"))
            api = "/api/hostgroups/"+ id +"/smart_class_parameters";
        else api = "/api/hosts/"+ id +"/smart_class_parameters";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getParameters(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
                String auth = Base64.encodeToString(UserInfo.getUNandPW().getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void getParameters(JSONObject response) throws JSONException {
        JSONArray arr = (JSONArray) response.get("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);
            JSONObject puppetClass = (JSONObject) obj.get("puppetclass_name");
            String puppetClassName = (String) puppetClass.get("name");
            if(parameters.containsKey(puppetClassName)){
                parameters.get(puppetClassName).add(obj);
            } else{
                parameters.put(puppetClassName,new ArrayList<JSONObject>());
                parameters.get(puppetClassName).add(obj);
            }
        }
        displayParameters();
    }

    private void displayParameters(){
        List<String> arr = new ArrayList<String>(parameters.keySet());
        Collections.sort(arr);

    }

    public void CloseActivity(View v){
        finish();
    }

    public static void setID(int newid){
        id = newid;
    }

    public static void setType(String newtype){
        type = newtype;
    }

    public static void setPageTitle(String pagetitle){
        title = pagetitle;
    }
}
