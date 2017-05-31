package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
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
    private static String hostgroup;
    private static String type;
    private static String parent;

    private static Map<String,HashMap<String, String>> tag;
    private static Map<String, HashMap<String, Object>> parameters;
    private static int requestReceive;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        setTitle(name);
        parameters = new HashMap<>();
        tag = new HashMap<>();
        requestReceive = 0;
        sendRequestToGetParameters();
    }

    private void sendRequestToGetParameters(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String api;

        if(type.equals("HOST"))
            api = "/api/hosts/"+ id +"/smart_class_parameters";
        else
            api = "/api/hostgroups/"+ id +"/smart_class_parameters";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getParameters(response);
                        } catch (JSONException | InterruptedException e) {
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
        label:
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);
            String match = (String) obj.get("match");
            if(!tag.containsKey(puppetClassName))
                tag.put(puppetClassName,new HashMap<String, String>());
            System.out.println(parent);
            System.out.println(match);
            switch (type) {
                case "HOST":
                    tag.get(puppetClassName).put(parameter,"GroupValue");
                    if (match.substring(0, 4).equals("fqdn") && match.substring(5).equals(name)) {
                        if(!((boolean) obj.get("use_puppet_default"))) {
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"Override");
                        } else{
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                        }
                        break label;
                    } else if (match.substring(0, 9).equals("hostgroup") && match.length() > hostgroup.length() &&match.substring(match.length() - hostgroup.length()).equals(hostgroup)) {
                        if(!((boolean) obj.get("use_puppet_default"))){
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"GroupValue");
                        }else {
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                        }
                    } else if(parent != null && match.substring(0, 9).equals("hostgroup") && match.substring(10).equals(parent)){
                        if(!((boolean) obj.get("use_puppet_default"))){
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"GroupValue");
                        }else {
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                        }
                    }
                    break;
                case "HOSTGROUPS":
                    tag.get(puppetClassName).put(parameter,"PuppetDefault");
                    if (match.substring(0, 9).equals("hostgroup") && match.substring(10).equals(name) && !((boolean) obj.get("use_puppet_default"))) {
                        value = obj.get("value");
                        tag.get(puppetClassName).put(parameter,"Override");
                        break label;
                    }
                    break;
                default:
                    tag.get(puppetClassName).put(parameter,"ParentValue");
                    if (match.substring(0, 9).equals("hostgroup") && match.substring(10).equals(parent + "/" + name)) {
                        if(!((boolean) obj.get("use_puppet_default"))){
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"Override");
                        }
                        else {
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                        }
                        break label;
                    }
                    else if (match.substring(0, 9).equals("hostgroup") && match.length() > parent.length() &&match.substring(match.length() - parent.length()).equals(parent)) {
                        if(!((boolean) obj.get("use_puppet_default"))){
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"ParentValue");
                        } else {
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                        }
                    }
                    break;
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
            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            list.addView(linearlayout);

            List<String> parameter = new ArrayList<>(parameters.get(key).keySet());
            Collections.sort(parameter);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TextView puppetclassName = new TextView(this);
            puppetclassName.setText(key);
            puppetclassName.setTextSize(25);
            puppetclassName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(puppetclassName);
            linearlayout.addView(layout);

            for(String obj: parameter){
                final EditText parameterValue = new EditText(this);
                final Spinner spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter;

                LinearLayout pLayout = new LinearLayout(this);
                pLayout.setOrientation(LinearLayout.HORIZONTAL);
                pLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                TextView parameterName = new TextView(this);
                parameterName.setText("- " + obj);
                parameterName.setTextSize(20);
                pLayout.addView(parameterName);

                LinearLayout mLayout = new LinearLayout(this);
                mLayout.setOrientation(LinearLayout.HORIZONTAL);
                mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                switch (type) {
                    case "HOSTGROUPS":
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHostGroup));
                        break;
                    case "HOST":
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHosts));
                        break;
                    default:
                        spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsForHostGroupWithParent));
                        break;
                }

                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(spinner.getSelectedItem().toString().equals("Override")){
                            parameterValue.setEnabled(true);
                        }
                        else parameterValue.setEnabled(false);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                mLayout.addView(spinner);

                if(!obj.equals("enabled")){
                    parameterValue.setText(parameters.get(key).get(obj).toString());
                    parameterValue.setTextSize(15);
                    parameterValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    parameterValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                    parameterValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    if(type.equals("HOSTGROUPS")) {
                        if (tag.get(key).get(obj).equals("Override")) {
                            spinner.setSelection(1);
                            parameterValue.setEnabled(true);
                        } else {
                            spinner.setSelection(0);
                            parameterValue.setText("");
                            parameterValue.setEnabled(false);
                        }
                    } else {
                        switch (tag.get(key).get(obj)) {
                            case "Override":
                                spinner.setSelection(2);
                                parameterValue.setEnabled(true);
                                break;
                            case "ParentValue":
                            case "GroupValue":
                                spinner.setSelection(0);
                                parameterValue.setEnabled(false);
                                break;
                            default:
                                spinner.setSelection(1);
                                parameterValue.setEnabled(false);
                                parameterValue.setText("");
                                break;
                        }
                    }
                    mLayout.addView(parameterValue);
                }
                else{
                    ArrayAdapter<String> _spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsOfEnabled));
                    Spinner _spinner = new Spinner(this);
                    _spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    _spinner.setAdapter(_spinnerArrayAdapter);
                    _spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    if((boolean)parameters.get(key).get(obj))
                        _spinner.setSelection(1);
                    else _spinner.setSelection(0);

                    if(type.equals("HOSTGROUPS")) {
                        if (tag.get(key).get(obj).equals("Override")) {
                            spinner.setSelection(1);
                            _spinner.setEnabled(true);
                        } else {
                            spinner.setSelection(0);
                            _spinner.setEnabled(false);
                        }
                    }else {
                        switch (tag.get(key).get(obj)) {
                            case "Override":
                                spinner.setSelection(2);
                                _spinner.setEnabled(true);
                                break;
                            case "ParentValue":
                            case "GroupValue":
                                spinner.setSelection(0);
                                _spinner.setEnabled(false);
                                break;
                            default:
                                spinner.setSelection(1);
                                _spinner.setEnabled(false);
                                break;
                        }
                    }
                    mLayout.addView(_spinner);
                }
                linearlayout.addView(pLayout);
                linearlayout.addView(mLayout);
            }

            TextView space = new TextView(this);
            space.setText("");
            linearlayout.addView(space);
        }
    }

    public void closeActivity(View v){ finish(); }

    public static void setID(int _id){ id = _id; }

    public static void setType(String _type){ type = _type; }

    public static void setName(String _name){ name = _name; }

    public static void setHostGroup(String _hostgroup) { hostgroup = _hostgroup; }

    public static void setParent(String _parent) { parent = _parent; }
}