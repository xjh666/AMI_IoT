package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
    private static String type = "";
    private HashMap<String, ArrayList<JSONObject>> parameters;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
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

    private void displayParameters() throws JSONException {
        List<String> arr = new ArrayList<String>(parameters.keySet());
        Collections.sort(arr);
        LinearLayout list = (LinearLayout)findViewById(R.id.paramlist);


        for(String key: arr){
            boolean hasEnableVariable = false;
            boolean isEnabled = false;
            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            list.addView(linearlayout);

            List<JSONObject> parameter = parameters.get(key);
            for(JSONObject obj: parameter){
                System.out.println(obj.get("description").toString().substring(0,5));
                if(obj.get("description").toString().substring(0,6).equals("Enable")){
                    hasEnableVariable = true;
                    isEnabled = (boolean) obj.get("default_value");
                    break;
                }
            }

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(this);
            textView.setText(key);
            textView.setTextSize(25);
            textView.setLayoutParams(new LinearLayout.LayoutParams(800, LinearLayout.LayoutParams.MATCH_PARENT));
            layout.addView(textView);

            if(hasEnableVariable) {
                ToggleButton button = new ToggleButton(this);
                button.setTextOn("Enabled");
                button.setTextOff("Disabled");
                button.setChecked(isEnabled);
                layout.addView(button);
            }
            linearlayout.addView(layout);

            for(JSONObject obj: parameter){
                if(!obj.get("description").toString().substring(0,6).equals("Enable")){
                    LinearLayout pLayout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

                    TextView pTextView = new TextView(this);
                    pTextView.setText(obj.get("parameter").toString());
                    pTextView.setTextSize(20);
                    pTextView.setLayoutParams(new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT));

                    EditText editText = new EditText(this);
                    editText.setText(obj.get("default_value").toString());
                    editText.setTextSize(18);
                    editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    pLayout.addView(pTextView);
                    pLayout.addView(editText);

                    linearlayout.addView(pLayout);
                }
            }
            TextView space = new TextView(this);
            space.setText("");
            linearlayout.addView(space);
        }
    }

    public void closeActivity(View v){
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
