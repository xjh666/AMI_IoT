package com.example.foremanproject.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.example.foremanproject.other.Configuration;

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

/**
 * This class is an activity to show the parameters of a host of a host group
 *
 *
 * The type can be three kinds: HOST, HOSTGROUPS, HOSTGROUPSWITHPARENT
 * This variable is used to clarify the type of the object of which parameters are shown
 * Variables, id, name, type and hostgroup, are set in the last step.
 * ArrayList hostgroup is the hierarchy  
 *
 *
 * Maps tag and _tag stores the type of the value of a parameter uses, which can be InheritedValue, PuppetDefault and Override.
 * Maps parameters and _parameters stores the values of a parameter
 * The key is puppetClassName of the parameter and the value is a map, of which the key is the parameterName and the value is the content needed
 * Compare tag and parameters with _tag and _parameters to determine whether to send request to update parameters and  what is the new value of parameters
 *
 *
 * The logic to determine the value display is:
 * set all values as default value, tag DefaultValue
 * if HOST
 *     get the override value order (here is order[] = {fqdn,hostgroup,os,domain})
 *         for all results of the override value array
 *             if match is order[0](fqdn)
 *                 determine whether use puppet default or not
 *                 break the loop
 *             if match is order[1]
 *                 find the parent of the value, the lower rank has the higher priority
 * similar for HOSTGROUPSWITHPARENT
 * if HOSTGROUP
 *     find "hostgroup=name" and determine whether use puppet default or not
 *
 *
 * Maps description, parameterType, matcher and inheritedValue are for the popup window
 * Matcher and inheritedValue are gotten similarly as the the values, while description and parameterType are easily gotten
 *
 *
 * The logic to update parameters is:
 *    (tag -> _tag)
 * 1. PuppetDefault -> PuppetDefault: do nothing
 *    PuppetDefault -> InheritedValue: DELETE
 *    PuppetDefault -> Override: PUT
 *
 * 2. InheritedValue -> InheritedValue: do nothing
 *    InheritedValue -> PuppetDefault: POST
 *    InheritedValue -> Override: POST
 *
 * 3. Override -> InheritedValue: DELETE
 *    Override -> PuppetDefault: PUT
 *    Override -> Override: PUT
 *
 *
 * API used:
 * GET /api/hosts/:host_id/smart_class_parameters
 * GET /api/hostgroups/:hostgroup_id/smart_class_parameters
 * GET /api/smart_class_parameters/:id
 * GET /api/smart_class_parameters/:smart_class_parameter_id/override_values
 * POST /api/smart_class_parameters/:smart_class_parameter_id/override_values
 * PUT /api/smart_class_parameters/:smart_class_parameter_id/override_values/:id
 * DELETE /api/smart_class_parameters/:smart_class_parameter_id/override_values/:id
 *
 */

public class Parameters extends AppCompatActivity {
    private static int id;
    private static String name;
    private static String type;
    private static ArrayList<String> hostgroup;

    private static Map<String, Map<String, String>> tag;
    private static Map<String, Map<String, String>> _tag;
    private static Map<String, Map<String, Object>> parameters;
    private static Map<String, Map<String, Object>> _parameters;
    private static Map<String, Map<String, Integer>> parameterID;
    private static int requestReceive; //to ensure all requests have received responses and then go to next step

    private static Map<String, Map<String, String>> description;
    private static Map<String, Map<String, String>> parameterType;
    private static Map<String, Map<String, String>> matcher;
    private static Map<String, Map<String, Object>> inheritedValue;
    private static Map<String, Integer> allHostGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setTitle(name);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        parameters = new HashMap<>();
        _parameters = new HashMap<>();
        tag = new HashMap<>();
        _tag = new HashMap<>();
        parameterID = new HashMap<>();
        requestReceive = 0;

        description = new HashMap<>();
        parameterType = new HashMap<>();
        matcher = new HashMap<>();
        inheritedValue = new HashMap<>();

        sendRequestToGetParameters();
    }

    @Override
    public void onBackPressed() { finish();}

    private void sendRequestToGetParameters(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String api;

        if(type.equals("HOST"))
            api = "/api/hosts/"+ id +"/smart_class_parameters";
        else
            api = "/api/hostgroups/"+ id +"/smart_class_parameters";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + api), null, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void getParameters(JSONObject response) throws JSONException, InterruptedException {
        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            JSONObject puppetClass = obj.getJSONObject("puppetclass_name");
            String puppetClassName = puppetClass.getString("name");
            String parameterName = obj.getString("parameter");
            int parameter_id = obj.getInt("id");

            if(!parameterID.containsKey(puppetClassName)) {
                parameterID.put(puppetClassName, new HashMap<String, Integer>());
                description.put(puppetClassName, new HashMap<String, String>());
                parameterType.put(puppetClassName, new HashMap<String, String>());
                matcher.put(puppetClassName, new HashMap<String, String>());
                inheritedValue.put(puppetClassName, new HashMap<String, Object>());
            }
            parameterID.get(puppetClassName).put(parameterName,parameter_id);
            description.get(puppetClassName).put(parameterName,obj.getString("description"));
            parameterType.get(puppetClassName).put(parameterName,obj.getString("parameter_type"));
            inheritedValue.get(puppetClassName).put(parameterName, obj.get("default_value"));

            switch (type) {
                case "HOSTGROUPS":
                    matcher.get(puppetClassName).put(parameterName, "Default value");
                    break;
                case "HOSTGROUPSWITHPARENT":
                    matcher.get(puppetClassName).put(parameterName, hostgroup.get(hostgroup.size()-2));
                    break;
                default:
                    if(hostgroup.size() == 1)
                        matcher.get(puppetClassName).put(parameterName, "hostgroup(" + hostgroup.get(hostgroup.size()-1) + ")");
                    else
                        matcher.get(puppetClassName).put(parameterName, "hostgroup(" + hostgroup.get(hostgroup.size()-2) + ")");
                    break;
            }

            sendRequestForValue(arr, parameter_id,puppetClassName);
        }
    }

    private void sendRequestForValue(final JSONArray arr, final int parameter_id, final String puppetClassName) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + "api/smart_class_parameters/" + parameter_id), null, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void getValue(JSONObject response, String puppetClassName) throws JSONException {
        JSONArray arr  = response.getJSONArray("override_values");
        Object value = response.get("default_value");
        String parameter = response.getString("parameter");
        String[] valueOrder = response.getString("override_value_order").split("\n");
        int index = -1;

        if(!tag.containsKey(puppetClassName))
            tag.put(puppetClassName,new HashMap<String, String>());
        tag.get(puppetClassName).put(parameter,"InheritedValue");

        if(type.equals("HOST")) {
            if (valueOrder[0].equals("fqdn"))
                hostgroup.add(name);
            else hostgroup.add(0, name);
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String[] match = obj.getString("match").split("=", 2);
            String valueFrom = "";
            if (!type.equals("HOSTGROUPS")) {
                if(match[0].equals("fqdn"))
                    valueFrom = match[1];
                else {
                    String[] str = match[1].split("/");
                    int j = str.length - 1;
                    String _str = str[j--];
                    while (!allHostGroup.containsKey(_str) && j >= 0) {
                        _str += ("/" + str[j--]);
                    }
                    if (allHostGroup.containsKey(_str))
                        valueFrom = _str;
                }
            }

            switch (type) {
                case "HOST":
                    for (int j = hostgroup.size() - 1; j >= 0; j--) {
                        if (valueFrom.equals(hostgroup.get(j)) && j > index) {
                            index = j;
                            if (!obj.getBoolean("use_puppet_default")) {
                                value = obj.get("value");
                                if (hostgroup.get(j).equals(name))
                                    tag.get(puppetClassName).put(parameter, "Override");
                                else tag.get(puppetClassName).put(parameter, "InheritedValue");
                            } else tag.get(puppetClassName).put(parameter, "PuppetDefault");
                            break;
                        }
                    }
                    break;
                case "HOSTGROUPS":
                    if (match[0].equals("hostgroup") && match[1].equals(name)) {
                        if (!obj.getBoolean("use_puppet_default")) {
                            value = obj.get("value");
                            tag.get(puppetClassName).put(parameter, "Override");
                        } else {
                            value = null;
                            tag.get(puppetClassName).put(parameter, "PuppetDefault");
                        }
                    }
                    break;
                default:
                    for (int j = hostgroup.size() - 1; j >= 0; j--) {
                        if (valueFrom.equals(hostgroup.get(j)) && j > index) {
                            index = j;
                            if (!obj.getBoolean("use_puppet_default")) {
                                value = obj.get("value");
                                if (hostgroup.get(j).equals(name))
                                    tag.get(puppetClassName).put(parameter, "Override");
                                else tag.get(puppetClassName).put(parameter, "InheritedValue");
                            } else tag.get(puppetClassName).put(parameter, "PuppetDefault");
                            break;
                        }
                    }
                    break;
            }
        }

        index = -1;
        if (type.equals("HOST")) {
            if (valueOrder[0].equals("fqdn"))
                hostgroup.remove(hostgroup.size() - 1);
            else hostgroup.remove(0);
        }
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String[] match = obj.getString("match").split("=");
            String valueFrom = "";

            if (!type.equals("HOSTGROUPS")) {
                String[] str = match[1].split("/");
                int j = str.length - 1;
                String _str = str[j--];
                while (!allHostGroup.containsKey(_str) && j>=0) {
                    _str += ("/" + str[j--]);
                }
                if(allHostGroup.containsKey(_str))
                    valueFrom = _str;
            }

            if (type.equals("HOST")) {
                for(int j = hostgroup.size()-1;j>=0;j--){
                    if(valueFrom.equals(hostgroup.get(j)) && j > index){
                        index = j;
                        String str = "";
                        for(int k=0;k<j;k++){
                            str += (hostgroup.get(k) + "/");
                        }
                        str += hostgroup.get(j);
                        matcher.get(puppetClassName).put(parameter, "hostgroup(" + str + ")");
                        inheritedValue.get(puppetClassName).put(parameter, obj.get("value"));
                    }
                }
            } else if(type.equals("HOSTGROUPSWITHPARENT")){
                for(int j = hostgroup.size()-2;j>=0;j--){
                    if(valueFrom.equals(hostgroup.get(j)) && j > index){
                        index = j;
                        String str = "";
                        for(int k=0;k<j;k++){
                            str += (hostgroup.get(k) + "/");
                        }
                        str += hostgroup.get(j);
                        matcher.get(puppetClassName).put(parameter, str);
                        inheritedValue.get(puppetClassName).put(parameter, obj.get("value"));
                    }
                }
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
        LinearLayout list = (LinearLayout)findViewById(R.id.list);

        for(final String puppetClassName: arr){
            if(!_parameters.containsKey(puppetClassName))
                _parameters.put(puppetClassName,new HashMap<String, Object>());

            LinearLayout linearlayout = new LinearLayout(this);
            linearlayout.setOrientation(LinearLayout.VERTICAL);
            list.addView(linearlayout);

            TextView space = new TextView(this);
            space.setText("");
            linearlayout.addView(space);

            List<String> parameter = new ArrayList<>(parameters.get(puppetClassName).keySet());
            Collections.sort(parameter);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);

            TextView puppetclassName = new TextView(this);
            puppetclassName.setText(puppetClassName);
            puppetclassName.setTextSize(26);
            puppetclassName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int)(Configuration.getHeight()* 0.07)));
            layout.addView(puppetclassName);
            linearlayout.addView(layout);

            for(final String parameterName: parameter){
                final EditText parameterValue = new EditText(this);
                final Spinner spinner = new Spinner(this);
                final Spinner _spinner = new Spinner(this);
                ArrayAdapter<String> spinnerArrayAdapter;

                LinearLayout pLayout = new LinearLayout(this);
                pLayout.setOrientation(LinearLayout.HORIZONTAL);
                pLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                final TextView parameterNameText = new TextView(this);
                parameterNameText.setText(" " + parameterName);
                parameterNameText.setTextSize(22);
                parameterNameText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                Button info = new Button(this);
                if(tag.get(puppetClassName).get(parameterName).equals("PuppetDefault"))
                    info.setBackgroundResource(R.drawable.warning_icon);
                else info.setBackgroundResource(R.drawable.i_mark_icon);
                info.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getHeight()* 0.04), (int)(Configuration.getHeight()* 0.04)));
                info.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showInfo(puppetClassName, parameterName);
                    }
                });

                pLayout.addView(info);
                pLayout.addView(parameterNameText);

                LinearLayout mLayout = new LinearLayout(this);
                mLayout.setOrientation(LinearLayout.HORIZONTAL);
                mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.selections));
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerArrayAdapter);
                spinner.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), (int)(Configuration.getHeight()* 0.08)));

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!_tag.containsKey(puppetClassName))
                            _tag.put(puppetClassName,new HashMap<String, String>());

                        switch(spinner.getSelectedItem().toString()){
                            case "Override":
                                _tag.get(puppetClassName).put(parameterName,"Override");
                                break;
                            case "Inherited Value":
                                _tag.get(puppetClassName).put(parameterName,"InheritedValue");
                                break;
                            case "Puppet Default":
                                _tag.get(puppetClassName).put(parameterName,"PuppetDefault");
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

                if(!parameterType.get(puppetClassName).get(parameterName).equals("boolean")){
                    parameterValue.setText(parameters.get(puppetClassName).get(parameterName).toString());
                    parameterValue.setTextSize(15);
                    parameterValue.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), (int)(Configuration.getHeight()* 0.08)));
                    parameterValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                    parameterValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    switch (tag.get(puppetClassName).get(parameterName)) {
                        case "Override":
                            spinner.setSelection(2);
                            parameterValue.setEnabled(true);
                            break;
                        case "InheritedValue":
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
                            _parameters.get(puppetClassName).put(parameterName,parameterValue.getText());
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
                    _spinner.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), (int)(Configuration.getHeight()* 0.08)));
                    _spinner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                    if((boolean)parameters.get(puppetClassName).get(parameterName))
                        _spinner.setSelection(1);
                    else _spinner.setSelection(0);

                    switch (tag.get(puppetClassName).get(parameterName)) {
                        case "Override":
                            spinner.setSelection(2);
                            _spinner.setEnabled(true);
                            break;
                        case "InheritedValue":
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
                                    _parameters.get(puppetClassName).put(parameterName,false);
                                    break;
                                case "Enabled":
                                    _parameters.get(puppetClassName).put(parameterName,true);
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
        submit.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.4), (int)(Configuration.getHeight()* 0.12)));
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
        cancel.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.4), (int)(Configuration.getHeight()* 0.12)));
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

    private void sendRequestToPost(JSONObject obj, String puppetClass, String parameterName){
        int parameter_id = parameterID.get(puppetClass).get(parameterName);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, (Configuration.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/"), obj, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
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
                (Request.Method.GET, (Configuration.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/"), null, new Response.Listener<JSONObject>() {
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
                                        String parent="";
                                        for(int j=0;j<hostgroup.size()-1;j++)
                                            parent = parent + hostgroup.get(j) + "/";
                                        if (match.equals("hostgroup=" + parent + name)) {
                                            overrideID = (int) result.get("id");
                                            break label1;
                                        }
                                        break;
                                }
                            }
                            if(overrideID != 0) {
                                if (obj == null)
                                    sendRequestToDelete(puppetClass, parameterName, overrideID);
                                else sendRequestToPut(obj, puppetClass, parameterName, overrideID);
                            } else {
                                obj.put("match", "fqdn=" + name);
                                sendRequestToPost(obj, puppetClass, parameterName);
                            }
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
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
                (Request.Method.PUT, (Configuration.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/" + overrideID), obj, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
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
                (Request.Method.DELETE, (Configuration.getUrl() + "api/smart_class_parameters/" + parameter_id + "/override_values/" + overrideID), null, new Response.Listener<JSONObject>() {
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
                String auth = Base64.encodeToString(Configuration.getUNandPW().getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + auth);
                return headers;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    private void showInfo(String puppetclassName, String parameterName){
        ParameterInfo.setInfo(description.get(puppetclassName).get(parameterName),
                                parameterType.get(puppetclassName).get(parameterName),
                                matcher.get(puppetclassName).get(parameterName),
                                inheritedValue.get(puppetclassName).get(parameterName),
                                tag.get(puppetclassName).get(parameterName));
        startActivity(new Intent(Parameters.this, ParameterInfo.class));
    }

    public void updateInfo() throws JSONException {
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
                        } else if(_tag.get(puppetClass).get(parameterName).equals("InheritedValue")){
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
                                    String parent="";
                                    for(int i=0;i<hostgroup.size()-1;i++)
                                        parent = parent + hostgroup.get(i) + "/";
                                    obj.put("match", "hostgroup=" + parent + name);
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
                                    String parent="";
                                    for(int i=0;i<hostgroup.size()-1;i++)
                                        parent = parent + hostgroup.get(i) + "/";
                                    obj.put("match", "hostgroup=" + parent + name);
                                    break;
                            }
                            sendRequestToPost(obj, puppetClass, parameterName);
                        }
                    }
                }
            }
        finish();
    }

    public static void setID(int _id){ id = _id; }

    public static void setType(String _type){ type = _type; }

    public static void setName(String _name){ name = _name; }

    public static void setHostGroup(ArrayList<String> _hostgroup) { hostgroup = _hostgroup; }

    public static void setAllHosGroup(Map<String, Integer> _allHostGroup){ allHostGroup = _allHostGroup; }
}