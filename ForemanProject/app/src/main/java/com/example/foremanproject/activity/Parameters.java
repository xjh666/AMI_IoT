package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/25/2017.
 */

public class Parameters extends AppCompatActivity {
    private static int id;
    private static String name;
    private static String hostGroup;
    private static String type;
    private static Map<String, HashMap<String, Object>> parameters;
    private static int requestReceive;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        setTitle(name);
        parameters = new HashMap<>();
        requestReceive = 0;
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
                        } catch (InterruptedException e) {
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

    private void getParameters(JSONObject response) throws JSONException, InterruptedException {
        JSONArray arr = (JSONArray) response.get("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);
            JSONObject puppetClass = (JSONObject) obj.get("puppetclass_name");
            String puppetClassName = (String) puppetClass.get("name");
            int parameter_id = (Integer) obj.get("id");
            sendRequestForValue(arr, parameter_id,puppetClassName);
        }
    }

    private void sendRequestForValue(final JSONArray arr, final int parameter_id, final String puppetClassName) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/smart_class_parameters/" + parameter_id), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getValue(response, puppetClassName);
                            if(requestReceive == arr.length())
                                displayParameters();
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

    private void getValue(JSONObject response, String puppetClassName) throws JSONException {
        JSONArray arr  = (JSONArray) response.get("override_values");
        Object value = response.get("default_value");
        String parameter = (String)response.get("parameter");

        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);
            String match = (String) obj.get("match");
            if(match.substring(0,4).equals("fqdn") && match.substring(5).equals(name) && !((boolean)obj.get("use_puppet_default"))){
                value = obj.get("value");
                break;
            }
            else if (match.substring(0,9).equals("hostgroup") && match.substring(10).equals(hostGroup) && !((boolean)obj.get("use_puppet_default"))){
                value = obj.get("value");
            }
        }
        if(!parameters.containsKey(puppetClassName))
            parameters.put(puppetClassName,new HashMap<String, Object>());
        parameters.get(puppetClassName).put(parameter,value);
        requestReceive++;
    }

    private void displayParameters() throws JSONException {
        List<String> arr = new ArrayList<>(parameters.keySet());
        Collections.sort(arr);
        LinearLayout list = (LinearLayout)findViewById(R.id.paramlist);

        for(String key: arr){
            boolean hasEnableVariable = false;
            boolean isEnabled = false;
            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            list.addView(linearlayout);

            List<String> parameter = new ArrayList<>(parameters.get(key).keySet());
            Collections.sort(parameter);
            for(String obj: parameter){
                if(obj.equals("enabled")){
                    hasEnableVariable = true;
                    if((boolean)parameters.get(key).get(obj))
                        isEnabled = true;
                    break;
                }
            }

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TextView textView = new TextView(this);
            textView.setText(key);
            textView.setTextSize(23);
            textView.setLayoutParams(new LinearLayout.LayoutParams(700, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(textView);
            linearlayout.addView(layout);

            if(hasEnableVariable) {

            }

            for(String obj: parameter){
                LinearLayout pLayout = new LinearLayout(this);
                pLayout.setOrientation(LinearLayout.HORIZONTAL);
                pLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

                TextView pTextView = new TextView(this);
                pTextView.setText("- " + obj);
                pTextView.setTextSize(19);
                pTextView.setLayoutParams(new LinearLayout.LayoutParams(680, LinearLayout.LayoutParams.WRAP_CONTENT));
                pLayout.addView(pTextView);

                if(!obj.equals("enabled")){
                    LinearLayout mLayout = new LinearLayout(this);
                    mLayout.setOrientation(LinearLayout.VERTICAL);
                    mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

                    ArrayAdapter<String> spinnerArrayAdapter;
                    Spinner spinner = new Spinner(this);
                    if(type.equals("HOSTGROUPS"))
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHostGroup));
                    else
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHosts));
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                    spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    EditText editText = new EditText(this);
//                    editText.setText(parameters.get(key).get(obj).toString());
                    editText.setTextSize(15);
                    editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


                    pLayout.addView(editText);
                }
                else{
                    ArrayAdapter<String> spinnerArrayAdapter;
                    Spinner spinner = new Spinner(this);
                    if(type.equals("HOSTGROUPS"))
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHostGroupOfEnabled));
                    else
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHostsOfEnabled));
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                    spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if(isEnabled)
                        spinner.setSelection(0);
                    else spinner.setSelection(1);
                    pLayout.addView(spinner);
                }

                linearlayout.addView(pLayout);
            }
            TextView space = new TextView(this);
            space.setText("");
            linearlayout.addView(space);
        }
    }

    public void closeActivity(View v){ finish(); }

    public static void setID(int newid){ id = newid; }

    public static void setType(String newtype){ type = newtype; }

    public static void setName(String pagetitle){ name = pagetitle; }

    public static void setHostGroup(String hostgroup) { hostGroup = hostgroup; }
}
