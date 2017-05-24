package com.example.foremanproject.fragment;

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
import com.example.foremanproject.other.UserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by labuser on 5/24/2017.
 */

public class HostGroups extends Fragment {
    public static AllHosts newInstance() {
        return new AllHosts();
    }
    LinearLayout grouplist;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.host_group, container, false);
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
            textView.setText((String) obj.get("name"));
            textView.setTextSize(16);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            Button button1 = new Button(getActivity());
            button1.setLayoutParams(new LinearLayout.LayoutParams(200, 160));
            button1.setText("All");
            button1.setTag(obj.get("name") + "showall");

            Button button2 = new Button(getActivity());
            button2.setLayoutParams(new LinearLayout.LayoutParams(200, 160));
            button2.setText("EDIT");
            button2.setTag(obj.get("name") + "edit");

            linearlayout.addView(textView);
            linearlayout.addView(button1);
            linearlayout.addView(button2);

            grouplist.addView(new LinearLayout(getActivity()));
        }
    }
}
