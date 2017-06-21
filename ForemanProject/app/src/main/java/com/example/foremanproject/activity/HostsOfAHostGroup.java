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
import com.example.foremanproject.other.Configuration;

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

    //The properties of a host
    Map<String, String> ip;
    Map<String, String> mac;
    Map<String, String> status;
    Map<String, String> configuration;
    Map<String, String> puppetEnvironment;
    Map<String, String> hostArchitecture;
    Map<String, String> os;
    Map<String, String> owner;
    Map<String, String> hostgroupName;

    /**
     * Created the activity and then send request to get information
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle(title);

        ip = new HashMap<>();
        mac = new HashMap<>();
        status = new HashMap<>();
        configuration = new HashMap<>();
        puppetEnvironment = new HashMap<>();
        hostArchitecture = new HashMap<>();
        os = new HashMap<>();
        owner = new HashMap<>();
        hostgroupName = new HashMap<>();

        sendRequest("");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() { finish(); }

    private void sendRequest(final String name) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + api), null, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
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
     * Clicking the name of the host will open the HostDetail activity to show the Properties of the host and last several report, if the host has reports.
     * Then, clicking the time of the report will open the ConfigReportDetail activity to show the details of the report
     *
     * Clicking the "EDIT" button will open Parameter activity to show the parameters and corresponding information of the host.
     * "GET /api/hosts" is to list all hosts and get the id of the selected host
     * Then set id and other variables of parameters
     */
    private void getHosts(JSONObject response) throws JSONException {
        JSONArray arr = response.getJSONArray("results");
        LinearLayout totalList = (LinearLayout) findViewById(R.id.list);
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            String name = obj.getString("name");

            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.HORIZONTAL);
            totalList.addView(linearlayout);

            ip.put(name, obj.getString("ip"));
            mac.put(name, obj.getString("mac"));
            status.put(name, obj.getString("global_status_label"));
            configuration.put(name, obj.getString("configuration_status_label"));
            puppetEnvironment.put(name, obj.getString("environment_name"));
            hostArchitecture.put(name, obj.getString("architecture_name"));
            os.put(name, obj.getString("operatingsystem_name"));
            owner.put(name, obj.getString("owner_type"));
            hostgroupName.put(name, obj.getString("hostgroup_title"));

            ImageView imageView = new ImageView(this);
            if(obj.get("global_status_label").equals("OK"))
                imageView.setImageResource(R.drawable.ok_icon);
            else imageView.setImageResource(R.drawable.exclamation_icon);
            imageView.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.1)));

            final TextView hostName = new TextView(this);
            final Intent intent = new Intent(this, HostDetail.class);
            hostName.setText(name);
            hostName.setTextSize(21);
            hostName.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.72), (int)(Configuration.getHeight()* 0.115)));
            hostName.setBackground(getResources().getDrawable(R.drawable.white_background));
            hostName.setAllCaps(false);
            hostName.setTag(name);
            hostName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            hostName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    HostDetail.setInfo(status.get(hostName.getTag().toString()),
                            configuration.get(hostName.getTag().toString()),
                            ip.get(hostName.getTag().toString()),
                            mac.get(hostName.getTag().toString()),
                            puppetEnvironment.get(hostName.getTag().toString()),
                            hostArchitecture.get(hostName.getTag().toString()),
                            os.get(hostName.getTag().toString()),
                            owner.get(hostName.getTag().toString()),
                            hostgroupName.get(hostName.getTag().toString()),
                            hostName.getTag().toString());
                    startActivity(intent);
                }
            });

            //The tag of a button is the name of the related host for the further use
            final Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.19),  (int)(Configuration.getHeight()* 0.1)));
            button.setText("Edit");
            button.setTag(name);
            button.setBackground(getResources().getDrawable(R.drawable.button_icon));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    hostgroup = new ArrayList<>();
                    api = "api/hosts";
                    sendRequest(button.getTag().toString());
                }
            });
            linearlayout.addView(imageView);
            linearlayout.addView(hostName);
            linearlayout.addView(button);
        }
        TextView space = new TextView(this);
        space.setText("");
        totalList.addView(space);
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
