package com.example.foremanproject.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/24/2017.
 */


/**
 * This class is for an activity to show the page of hosts in a particular group.
 * The api used is "GET /api/hostgroups/:hostgroup_id/hosts" to list all hosts for a host group
 * hostgroup_id is gotten from the last activity while clicking the "ALL" button of a host
 */

public class HostsOfAHostGroup extends AppCompatActivity {
    private static String api = "";
    private static String title = "";
    private int hostgroupID;
    private ArrayList<String> hostgroup;
    /**
     * Created the activity and then send request to get information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostsofahost_group);
        setTitle(title);
        sendRequest("");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() { finish(); }

    private void sendRequest(final String name) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(api.substring(api.length()-5).equals("hosts")) {
                                if (name.equals("")) {
                                    getHosts(response);
                                } else getInfoOfHost(response, name);
                            } else if(api.equals("api/hostgroups")){
                                getAllHostGroup(response);
                            } else {
                                setHostGroup(response);
                            }
                        } catch (JSONException | InstantiationException | IllegalAccessException e) {
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

    /**
     * Created the activity and then send request to get information.
     * ImageView is to show the status of the hosts (OK/Warning), textView is the name of the host.
     *
     * Clicking the "EDIT" button will open Parameter activity to show the parameters and corresponding information of the host.
     * "GET /api/hosts" is to list all hosts and get the id of the selected host
     * Then set id and other variables of parameters
     */
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

            //The tag of a button is the name of the related host for the further use
            final Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams(210, 180));
            button.setText("Edit");
            button.setTag(obj.get("name"));
            button.setBackground(getResources().getDrawable(R.drawable.button_icon));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    hostgroup = new ArrayList<>();
                    api = "api/hosts";
                    sendRequest((String) button.getTag());
                }
            });
            linearlayout.addView(imageView);
            linearlayout.addView(textView);
            linearlayout.addView(button);

            totalList.addView(new LinearLayout(this));
        }
    }

    private void getInfoOfHost(JSONObject response, String name) throws JSONException, java.lang.InstantiationException, IllegalAccessException {
        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            String objName = obj.getString("name");
            if(objName.equals(name)){
                Parameters.setID(obj.getInt("id"));
                Parameters.setType("HOST");
                Parameters.setName(name);
                hostgroupID = obj.getInt("hostgroup_id");
                api = "api/hostgroups/" + hostgroupID;
                sendRequest("");
                break;
            }
        }
    }

    private void setHostGroup(JSONObject response) throws JSONException {
        hostgroup.add(0,response.getString("name"));
        if(response.isNull("parent_id")){
            Parameters.setHostGroup(hostgroup);
            api = "api/hostgroups";
            sendRequest("");
        } else{
            hostgroupID = response.getInt("parent_id");
            api = "/api/hostgroups/" + hostgroupID;
            sendRequest("");
        }
    }

    private void getAllHostGroup(JSONObject response) throws JSONException {
        Map<String, Integer> allHostGroup = new HashMap<>();
        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++)
            allHostGroup.put(arr.getJSONObject(i).getString("name"),1);
        Parameters.setAllHosGroup(allHostGroup);
        startActivity(new Intent(this, Parameters.class));
    }

    public static void setAPI(int id){
        api = "api/hostgroups/" + id + "/hosts";
    }

    public static void setPageTitle(String str){
        title = str;
    }
}
