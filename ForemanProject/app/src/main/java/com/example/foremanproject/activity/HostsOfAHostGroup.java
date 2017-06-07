package com.example.foremanproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/24/2017.
 */

public class HostsOfAHostGroup extends AppCompatActivity {
    private static String api = "";
    private static String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostsofahost_group);
        setTitle(title);
        sendRequest();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void sendRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getHosts(response);
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

    private void getHosts(JSONObject response) throws JSONException {
        JSONArray arr = response.getJSONArray("results");
        LinearLayout totalList = (LinearLayout) findViewById(R.id.totallist);
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);

            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.HORIZONTAL);
            totalList.addView(linearlayout);

            ImageView imageView = new ImageView(this);
            if(obj.get("global_status_label").equals("OK"))
                imageView.setImageResource(R.drawable.ok_icon);
            else imageView.setImageResource(R.drawable.exclamation_icon);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(80, 150));

            TextView textView = new TextView(this);
            textView.setText(" " + obj.get("name"));
            textView.setTextSize(25);
            textView.setLayoutParams(new LinearLayout.LayoutParams(780, LinearLayout.LayoutParams.MATCH_PARENT));

            final Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams(210, 180));
            button.setText("Edit");
            button.setTag(obj.get("name"));
            button.setBackground(getResources().getDrawable(R.drawable.button_icon));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendRequestForID((String) button.getTag());
                }
            });
            linearlayout.addView(imageView);
            linearlayout.addView(textView);
            linearlayout.addView(button);

            totalList.addView(new LinearLayout(this));
        }
    }

    private void sendRequestForID(final String tag)  {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/hosts"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getID(response, tag);
                        } catch (JSONException | IllegalAccessException | InstantiationException e) {
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

    private void getID(JSONObject response, String name) throws JSONException, java.lang.InstantiationException, IllegalAccessException {
        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            String objName = obj.getString("name");
            if(objName.equals(name)){
                Parameters.setID(obj.getInt("id"));
                Parameters.setType("HOST");
                Parameters.setName(name);

                String[] title = obj.getString("hostgroup_title").split("/");
                ArrayList<String> hostgroup = new ArrayList<>();
                hostgroup.addAll(Arrays.asList(title));
                Parameters.setHostGroup(hostgroup);

                startActivity(new Intent(this, Parameters.class));
                break;
            }
        }
    }

    public static void setAPI(int id){
        api = "/api/hostgroups/" + id + "/hosts";
    }

    public static void setPageTitle(String str){
        title = str;
    }
}
