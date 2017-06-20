package com.example.foremanproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.foremanproject.activity.HostDetail;
import com.example.foremanproject.activity.Parameters;
import com.example.foremanproject.other.Configuration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/19/2017.
 */

/**
 * This class is a fragment to show all hosts in the system
 * This class is similar to the HostsOfAHostGroup
 */

public class AllHosts extends Fragment  {
    LinearLayout totalList;
    String api;
    ArrayList<String> hostgroup;
    Map<String, String> ip;
    Map<String, String> mac;
    Map<String, String> status;
    Map<String, String> configuration;
    Map<String, String> puppetEnvironment;
    Map<String, String> hostArchitecture;
    Map<String, String> os;
    Map<String, String> owner;
    Map<String, String> hostgroupName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.list, container, false);
        api = "api/hosts";

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
        return view;
    }

    private void sendRequest(final String name) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            switch (api) {
                                case "api/hosts":
                                    if (name.equals(""))
                                        getHosts(response);
                                    else getInfoOfHost(response, name);
                                    break;
                                case "api/hostgroups":
                                    getAllHostGroup(response);
                                    break;
                                default:
                                    setHostGroup(response);
                                    break;
                            }
                        } catch (JSONException | java.lang.InstantiationException | IllegalAccessException e) {
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

    private void getHosts(JSONObject response) throws JSONException {
        JSONArray arr = response.getJSONArray("results");
        totalList = (LinearLayout) getView().findViewById(R.id.list);
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            LinearLayout linearlayout = new LinearLayout(getActivity());
            linearlayout.setOrientation(LinearLayout.HORIZONTAL);
            String name = obj.getString("name");

            ip.put(name, obj.getString("ip"));
            mac.put(name, obj.getString("mac"));
            status.put(name, obj.getString("global_status_label"));
            configuration.put(name, obj.getString("configuration_status_label"));
            puppetEnvironment.put(name, obj.getString("environment_name"));
            hostArchitecture.put(name, obj.getString("architecture_name"));
            os.put(name, obj.getString("operatingsystem_name"));
            owner.put(name, obj.getString("owner_type"));
            hostgroupName.put(name, obj.getString("hostgroup_title"));

            ImageView imageView = new ImageView(getActivity());
            if(obj.get("global_status_label").equals("OK"))
                imageView.setImageResource(R.drawable.ok_icon);
            else imageView.setImageResource(R.drawable.exclamation_icon);
            imageView.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.1)));

            final Button hostName = new Button(getActivity());
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

                    startActivity(new Intent(getActivity(), HostDetail.class));
                }
            });

            final Button button = new Button(getActivity());
            button.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.2), (int)(Configuration.getHeight()* 0.1)));
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
            totalList.addView(linearlayout);
        }
        TextView space = new TextView(getActivity());
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
                if(!obj.isNull("hostgroup_id")) {
                    api = "api/hostgroups/" + obj.getInt("hostgroup_id");
                    sendRequest("");
                } else {
                    startActivity(new Intent(getActivity(), Parameters.class));
                }
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
            api = "api/hostgroups/" + response.getInt("parent_id");
            sendRequest("");
        }
    }

    private void getAllHostGroup(JSONObject response) throws JSONException {
        Map<String, Integer> allHostGroup = new HashMap<>();
        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++)
            allHostGroup.put(arr.getJSONObject(i).getString("name"),1);
        Parameters.setAllHosGroup(allHostGroup);
        startActivity(new Intent(getActivity(), Parameters.class));
    }
}