package com.example.foremanproject.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 6/20/2017.
 */

public class HostDetail extends AppCompatActivity {
    private static String name;
    private static String status;
    private static String configuration;
    private static String mac;
    private static String ip;
    private static String puppetEnvironment;
    private static String hostArchitecture;
    private static String os;
    private static String owner;
    private static String hostgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(name);
        setPropertyTable();
        sendRequest();
    }

    @Override
    public void onBackPressed() { finish();}

    private void sendRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + "api/hosts/" + name + "/config_reports"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            setReports(response);
                        } catch (JSONException | ParseException e) {
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

    private void setPropertyTable(){
        LinearLayout list = (LinearLayout) findViewById(R.id.list);
        LinearLayout layout;
        TextView text;
        ImageView image;

        text = new TextView(this);
        text.setText(" Properties");
        text.setTextColor(Color.BLACK);
        text.setTextSize(21);
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout = new LinearLayout(this);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Status");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        image = new ImageView(this);
        if(status.equals("OK"))
            image.setImageResource(R.drawable.ok_icon);
        else image.setImageResource(R.drawable.exclamation_icon);
        image.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.1), LinearLayout.LayoutParams.MATCH_PARENT));
        image.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(image);
        text = new TextView(this);
        text.setText(" " + status);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.4), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Configuration");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        image = new ImageView(this);
        if(status.equals("OK"))
            image.setImageResource(R.drawable.ok_icon);
        else image.setImageResource(R.drawable.exclamation_icon);
        image.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.1), LinearLayout.LayoutParams.MATCH_PARENT));
        image.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(image);
        text = new TextView(this);
        text.setText(" " + configuration);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" IP Address");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + ip);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" MAC Address");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + mac);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Puppet Environment");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + puppetEnvironment);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Host Architecture");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + hostArchitecture);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Operating System");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + os);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Host group");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + hostgroup);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Owner");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.MATCH_PARENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + owner);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        text = new TextView(this);
        text.setText("");
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(Configuration.getHeight()* 0.1)));
        list.addView(text); // space
    }

    private void setReports(JSONObject response) throws JSONException, ParseException {
        LinearLayout list = (LinearLayout) findViewById(R.id.list);
        LinearLayout layout;
        TextView text;

        JSONArray arr = response.getJSONArray("results");
        int num = arr.length();
        if(num > 10) num = 10;

        text = new TextView(this);
        if(num == 0)
            text.setText("no reports");
        text.setText("Last " + num + " reports");
        text.setTextSize(22);
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout = new LinearLayout(this);
        layout.addView(text);
        list.addView(layout);

        if(num == 0)
            return;

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Last Report");
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("A");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("R");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("F");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("FR");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("S");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText("P");
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextColor(Color.BLACK);
        text.setTextSize(19);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

        for(int i=0;i<num;i++){
            Date time = sdf.parse(arr.getJSONObject(i).getString("reported_at").substring(0,10) + " " + arr.getJSONObject(i).getString("created_at").substring(11,19));
            long timeDifference = currentTime.getTime() - time.getTime();
            timeDifference /= 1000;

            JSONObject status = arr.getJSONObject(i).getJSONObject("status");

            layout = new LinearLayout(this);
            text = new TextView(this);
            text.setBackgroundResource(R.drawable.cell_shape);
            text.setTextColor(Color.BLACK);
            if(timeDifference < 60)
                text.setText(" " + timeDifference + " seconds ago");
            else {
                timeDifference = (timeDifference+30)/60;
                if(timeDifference < 60)
                    text.setText(" " + timeDifference + " minutes ago" );
                else {
                    timeDifference = (timeDifference+30)/60;
                    if(timeDifference < 24)
                        text.setText(" about " + timeDifference + " hours ago" );
                    else {
                        timeDifference /= 24;
                        text.setText(" about " + timeDifference + " days ago" );
                    }
                }
            }
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setTag(arr.getJSONObject(i).getInt("id"));
            final Intent intent = new Intent(this, ConfigReportDetail.class);
            text.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startActivity(intent);
                }
            });
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("applied"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("restarted"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("failed"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("failed_restarts"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("skipped"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            text = new TextView(this);
            text.setText(status.getString("pending"));
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextColor(Color.BLACK);
            text.setTextSize(17);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);
            list.addView(layout);
        }

        text = new TextView(this);
        text.setText("");
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(Configuration.getHeight()* 0.05)));
        list.addView(text); // space
    }

    public static void setInfo(String _status, String _configuration, String _ip, String _mac,
                               String _puppetEnvironment, String _hostArchitecture, String _os,
                               String _owner, String _hostgroup, String _name)
    {
        status = _status;
        configuration = _configuration;
        ip = _ip;
        mac = _mac;
        puppetEnvironment = _puppetEnvironment;
        hostArchitecture = _hostArchitecture;
        os = _os;
        owner = _owner;
        hostgroup =_hostgroup;
        name = _name;
    }
}
