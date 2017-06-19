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
    public static AllHosts newInstance() {
        return new AllHosts();
    }
    LinearLayout totalList;
    String api;
    ArrayList<String> hostgroup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.list, container, false);
        api = "api/hosts";
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
            totalList.addView(linearlayout);

            ImageView imageView = new ImageView(getActivity());
            if(obj.get("global_status_label").equals("OK"))
                imageView.setImageResource(R.drawable.ok_icon);
            else imageView.setImageResource(R.drawable.exclamation_icon);
            imageView.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.08), (int)(Configuration.getHeight()* 0.1)));

            TextView textView = new TextView(getActivity());
            textView.setText(" " + obj.get("name"));
            textView.setTextSize(24);
            textView.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.72), LinearLayout.LayoutParams.WRAP_CONTENT));

            final Button button = new Button(getActivity());
            button.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.2), (int)(Configuration.getHeight()* 0.1)));
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