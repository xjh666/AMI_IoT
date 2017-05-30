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
import com.example.foremanproject.activity.HostsOfAHostGroup;
import com.example.foremanproject.activity.Parameters;
import com.example.foremanproject.other.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/24/2017.
 */

public class HostGroups extends Fragment {
    public static HostGroups newInstance() {return new HostGroups(); }
    LinearLayout grouplist;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_hostgroup, container, false);
        sendRequest();
        return view;
    }

    private void sendRequest() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/hostgroups"), null, new Response.Listener<JSONObject>() {
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
        JSONArray arr = (JSONArray) response.get("results");
        grouplist = (LinearLayout) getView().findViewById(R.id.grouplist);
        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);

            LinearLayout linearlayout = new LinearLayout(getActivity());
            linearlayout.setOrientation(LinearLayout.HORIZONTAL);
            grouplist.addView(linearlayout);

            TextView textView = new TextView(getActivity());
            textView.setText(obj.get("title").toString());
            textView.setTextSize(20);
            textView.setLayoutParams(new LinearLayout.LayoutParams(720, LinearLayout.LayoutParams.MATCH_PARENT));

            final Button button1 = new Button(getActivity());
            button1.setLayoutParams(new LinearLayout.LayoutParams(180, 150));
            button1.setText("All");
            button1.setTag(obj.get("name") + "s");
            button1.setBackground(getResources().getDrawable(R.drawable.button_icon));
            button1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendRequestForID(button1.getTag().toString());
                }
            });


            final Button button2 = new Button(getActivity());
            button2.setLayoutParams(new LinearLayout.LayoutParams(180, 150));
            button2.setText("EDIT");
            button2.setTag(obj.get("name") + "e");
            button2.setBackground(getResources().getDrawable(R.drawable.button_icon));
            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendRequestForID(button2.getTag().toString());
                }
            });
            linearlayout.addView(textView);
            linearlayout.addView(button1);
            linearlayout.addView(button2);

            grouplist.addView(new LinearLayout(getActivity()));
        }
    }

    private void sendRequestForID(final String tag)  {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/hostgroups"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getID(response, tag);
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
                String auth = Base64.encodeToString(UserInfo.getUNandPW().getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void getID(JSONObject response, String tag) throws JSONException, java.lang.InstantiationException, IllegalAccessException {
        JSONArray arr = (JSONArray) response.get("results");
        String name = tag.substring(0,tag.length()-1);
        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);
            String objName = obj.get("name").toString();
            if(objName.equals(name)){
                selectInstruction((Integer)obj.get("id"),tag.substring(tag.length()-1),obj);
                break;
            }
        }
    }

    private void selectInstruction(int id, String tag, JSONObject obj) throws IllegalAccessException, java.lang.InstantiationException, JSONException {
        String name = obj.get("name").toString();

        if (tag.equals("s")) {
            HostsOfAHostGroup.setAPI(id);
            HostsOfAHostGroup.setPageTitle(name);
            startActivity(new Intent(getActivity(), HostsOfAHostGroup.class));
        } else {
            Parameters.setID(id);
            Parameters.setName(name);
            Parameters.setType("HOSTGROUPS");
            if(!obj.isNull("parent_name")){
                Parameters.setType("HOSTGROUPSWITHPARENT");
                Parameters.setParent(obj.get("parent_name").toString());
            }
            startActivity(new Intent(getActivity(), Parameters.class));
        }
    }
}
