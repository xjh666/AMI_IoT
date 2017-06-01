package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private static Map<String,HashMap<String, String>> _tag;
    private static Map<String, HashMap<String, Object>> parameters;
    private static Map<String, HashMap<String, Object>> _parameters;
    private static Map<String, HashMap<String, Integer>> parameterID;
    private static int requestReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        setTitle(name);
        parameters = new HashMap<>();
        _parameters = new HashMap<>();
        tag = new HashMap<>();
        _tag = new HashMap<>();
        parameterID = new HashMap<>();
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
                        Toast.makeText(Parameters.this, "Network Error. Please try again.", Toast.LENGTH_LONG).show();
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

            if(!parameterID.containsKey(puppetClassName))
                parameterID.put(puppetClassName,new HashMap<String, Integer>());
            parameterID.get(puppetClassName).put(obj.get("parameter").toString(),parameter_id);

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
                                displayParametersAndMonitorChange();
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
                    tag.get(puppetClassName).put(parameter,"DefaultValue");
                    if (match.substring(0, 9).equals("hostgroup") && match.substring(10).equals(name)){
                        if(!((boolean) obj.get("use_puppet_default"))) {
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter,"Override");
                        } else{
                            tag.get(puppetClassName).put(parameter,"PuppetDefault");
                            value = null;
                        }
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

    private void displayParametersAndMonitorChange() throws JSONException {
        List<String> arr = new ArrayList<>(parameters.keySet());
        Collections.sort(arr);
        LinearLayout list = (LinearLayout)findViewById(R.id.paramlist);

        for(final String key: arr){
            if(!_parameters.containsKey(key))
                _parameters.put(key,new HashMap<String, Object>());

            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            list.addView(linearlayout);

            TextView space = new TextView(this);
            space.setText("");
            linearlayout.addView(space);

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

            for(final String obj: parameter){
                final EditText parameterValue = new EditText(this);
                final Spinner spinner = new Spinner(this);
                final Spinner _spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter;

                LinearLayout pLayout = new LinearLayout(this);
                pLayout.setOrientation(LinearLayout.HORIZONTAL);
                pLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80));

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
                spinner.setLayoutParams(new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, 120));

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!_tag.containsKey(key))
                            _tag.put(key,new HashMap<String, String>());

                        switch(spinner.getSelectedItem().toString()){
                            case "Override":
                                _tag.get(key).put(obj,"Override");
                                break;
                            case "HostGroup Value":
                                _tag.get(key).put(obj,"GroupValue");
                                break;
                            case "Puppet Default":
                                _tag.get(key).put(obj,"PuppetDefault");
                                break;
                            case "Parent Value":
                                _tag.get(key).put(obj,"ParentValue");
                                break;
                        }

                        if(spinner.getSelectedItem().toString().equals("Override")){
                            parameterValue.setEnabled(true);
                            _spinner.setEnabled(true);
                        }
                        else {
                            parameterValue.setEnabled(false);
                            _spinner.setEnabled(false);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                mLayout.addView(spinner);

                if(!obj.equals("enabled")){
                    parameterValue.setText(parameters.get(key).get(obj).toString());
                    parameterValue.setTextSize(15);
                    parameterValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120));
                    parameterValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                    parameterValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    switch (tag.get(key).get(obj)) {
                        case "Override":
                            spinner.setSelection(2);
                            parameterValue.setEnabled(true);
                            break;
                        case "ParentValue":
                        case "GroupValue":
                        case "DefaultValue":
                            spinner.setSelection(0);
                            parameterValue.setEnabled(false);
                            break;
                        default:
                            spinner.setSelection(1);
                            parameterValue.setEnabled(false);
                            parameterValue.setText("");
                            break;
                    }

                    parameterValue.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {
                            _parameters.get(key).put(obj,parameterValue.getText());
                        }

                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });
                    mLayout.addView(parameterValue);
                }
                else{
                    ArrayAdapter<String> _spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selectionsOfEnabled));
                    _spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    _spinner.setAdapter(_spinnerArrayAdapter);
                    _spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120));
                    _spinner.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

                    if((boolean)parameters.get(key).get(obj))
                        _spinner.setSelection(1);
                    else _spinner.setSelection(0);

                    switch (tag.get(key).get(obj)) {
                        case "Override":
                            spinner.setSelection(2);
                            _spinner.setEnabled(true);
                            break;
                        case "ParentValue":
                        case "GroupValue":
                        case "DefaultValue":
                            spinner.setSelection(0);
                            _spinner.setEnabled(false);
                            break;
                        default:
                            spinner.setSelection(1);
                            _spinner.setEnabled(false);
                            break;
                    }

                    _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            switch(_spinner.getSelectedItem().toString()){
                                case "Disabled":
                                    _parameters.get(key).put(obj,false);
                                    break;
                                case "Enabled":
                                    _parameters.get(key).put(obj,true);
                                    break;
                            }

                            if(spinner.getSelectedItem().toString().equals("Override")){
                                parameterValue.setEnabled(true);
                                _spinner.setEnabled(true);
                            }
                            else {
                                parameterValue.setEnabled(false);
                                _spinner.setEnabled(false);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });

                    mLayout.addView(_spinner);
                }
                linearlayout.addView(pLayout);
                linearlayout.addView(mLayout);
            }
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView space1 = new TextView(this);
        space1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        space1.setText("   ");
        TextView space2 = new TextView(this);
        space2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        space2.setText("   ");
        TextView space3 = new TextView(this);
        space3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        space3.setText("   ");

        Button submit = new Button(this);
        submit.setText("SUBMIT");
        submit.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        submit.setLayoutParams(new LinearLayout.LayoutParams(440, 250));
        submit.setBackground(getResources().getDrawable(R.drawable.button_icon));
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    updateInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button cancel = new Button(this);
        cancel.setText("CANCEL");
        cancel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cancel.setLayoutParams(new LinearLayout.LayoutParams(440, 250));
        cancel.setBackground(getResources().getDrawable(R.drawable.button_icon));
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        layout.addView(space1);
        layout.addView(submit);
        layout.addView(space2);
        layout.addView(cancel);
        layout.addView(space3);
        list.addView(layout);

    }

    public void updateInfo() throws JSONException {
        System.out.println(parameters);
        System.out.println(_parameters);
        for (String puppetClass : _tag.keySet())
            for (String parameterName : _tag.get(puppetClass).keySet()){
                switch(tag.get(puppetClass).get(parameterName)) {
                    case "PuppetDefault":
                        {
                            if(_tag.get(puppetClass).get(parameterName).equals("Override")){
                                JSONObject obj = new JSONObject();
                                obj.put("use_puppet_default",0);
                                if(_parameters.get(puppetClass).containsKey(parameterName))
                                    obj.put("value",_parameters.get(puppetClass).get(parameterName));
                                else obj.put("value",parameters.get(puppetClass).get(parameterName));
                                sendRequestTogGetOverrideID(obj, puppetClass, parameterName);
                            } else if(_tag.get(puppetClass).get(parameterName).equals("GroupValue")
                                    || _tag.get(puppetClass).get(parameterName).equals("DefaultValue")
                                    || _tag.get(puppetClass).get(parameterName).equals("ParentValue")){
                                sendRequestTogGetOverrideID(null, puppetClass, parameterName);
                            }
                            break;
                        }

                    case "Override":
                        {
                            if(_tag.get(puppetClass).get(parameterName).equals("Override")){
                                if(_parameters.get(puppetClass).containsKey(parameterName) && !(_parameters.get(puppetClass).get(parameterName).equals(parameters.get(puppetClass).get(parameterName)))) {
                                    JSONObject obj = new JSONObject();
                                    obj.put("value", _parameters.get(puppetClass).get(parameterName));
                                    sendRequestTogGetOverrideID(obj, puppetClass, parameterName);
                                }
                            } else if(_tag.get(puppetClass).get(parameterName).equals("PuppetDefault")){
                                JSONObject obj = new JSONObject();
                                obj.put("use_puppet_default",1);
                                obj.put("value",JSONObject.NULL);
                                sendRequestTogGetOverrideID(obj, puppetClass, parameterName);
                            } else {
                                sendRequestTogGetOverrideID(null, puppetClass, parameterName);
                            }
                            break;
                        }

                    default:
                        {
                            if(_tag.get(puppetClass).get(parameterName).equals("Override")){
                                JSONObject obj = new JSONObject();
                                obj.put("use_puppet_default",0);
                                if(_parameters.get(puppetClass).containsKey(parameterName))
                                    obj.put("value",_parameters.get(puppetClass).get(parameterName));
                                else obj.put("value",parameters.get(puppetClass).get(parameterName));
                                switch (type) {
                                    case "HOST":
                                        obj.put("match", "fqdn=" + name);
                                        break;
                                    case "HOSTGROUPS":
                                        obj.put("match", "hostgroup=" + name);
                                        break;
                                    default:
                                        obj.put("match", "hostgroup=" + parent + "/" + name);
                                        break;
                                }
                                sendRequestToPost(obj, puppetClass, parameterName);
                            } else if(_tag.get(puppetClass).get(parameterName).equals("PuppetDefault")){
                                JSONObject obj = new JSONObject();
                                obj.put("use_puppet_default",1);
                                switch (type) {
                                    case "HOST":
                                        obj.put("match", "fqdn=" + name);
                                        break;
                                    case "HOSTGROUPS":
                                        obj.put("match", "hostgroup=" + name);
                                        break;
                                    default:
                                        obj.put("match", "hostgroup=" + parent + "/" + name);
                                        break;
                                }
                                sendRequestToPost(obj, puppetClass, parameterName);
                            }
                        }
                }
        }
        finish();
    }

    private void sendRequestToPost(JSONObject obj, String puppetClass, String parameterName){
        int parameter_id = parameterID.get(puppetClass).get(parameterName);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, (UserInfo.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/"), obj, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
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

    private void sendRequestTogGetOverrideID(final JSONObject obj, final String puppetClass, final String parameterName){
        int parameter_id = parameterID.get(puppetClass).get(parameterName);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray arr = (JSONArray) response.get("results");
                            int overrideID = 0;
                            label1:
                            for(int i=0;i<arr.length();i++) {
                                JSONObject result = (JSONObject) arr.get(i);
                                String match = result.get("match").toString();
                                switch (type) {
                                    case "HOST":
                                        if (match.equals("fqdn=" + name)) {
                                            overrideID = (int) result.get("id");
                                            break label1;
                                        }
                                        break;
                                    case "HOSTGROUPS":
                                        if (match.equals("hostgroup=" + name)) {
                                            overrideID = (int) result.get("id");
                                            break label1;
                                        }
                                        break;
                                    default:
                                        if (match.equals("hostgroup=" + parent + "/" + name)) {
                                            overrideID = (int) result.get("id");
                                            break label1;
                                        }
                                        break;
                                }
                            }
                            if(obj==null)
                                sendRequestToDelete(puppetClass, parameterName, overrideID);
                            else sendRequestToPut(obj,puppetClass, parameterName, overrideID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
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

    private void sendRequestToPut(JSONObject obj, String puppetClass, String parameterName, int overrideID){
        int parameter_id = parameterID.get(puppetClass).get(parameterName);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, (UserInfo.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/" + overrideID), obj, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
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

    private void sendRequestToDelete(String puppetClass, String parameterName, int overrideID){
        int parameter_id = parameterID.get(puppetClass).get(parameterName);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.DELETE, (UserInfo.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/" + overrideID), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
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

    public void closeActivity(View v){ finish(); }

    public static void setID(int _id){ id = _id; }

    public static void setType(String _type){ type = _type; }

    public static void setName(String _name){ name = _name; }

    public static void setHostGroup(String _hostgroup) { hostgroup = _hostgroup; }

    public static void setParent(String _parent) { parent = _parent; }
}