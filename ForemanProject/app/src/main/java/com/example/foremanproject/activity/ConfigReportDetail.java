package com.example.foremanproject.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 6/21/2017.
 */

public class ConfigReportDetail extends AppCompatActivity {
    private static String name;
    private static int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(name);
        sendRequest();
    }

    @Override
    public void onBackPressed() { finish();}

    private void sendRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + "api/config_reports/" + id), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getReport(response);
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

    private void getReport(JSONObject response) throws JSONException {
        LinearLayout list = (LinearLayout) findViewById(R.id.list);
        LinearLayout layout;
        TextView text;

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Level");
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.16), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);

        text = new TextView(this);
        text.setText(" Resource");
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.42), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);

        text = new TextView(this);
        text.setText(" Message");
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.425), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);

        list.addView(layout);

        JSONArray logs = response.getJSONArray("logs");
        for(int i=0;i<logs.length();i++){
            layout = new LinearLayout(this);
            JSONObject obj = logs.getJSONObject(i);
            String level = obj.getString("level");
            String source = obj.getJSONObject("source").getString("source");
            String message = obj.getJSONObject("message").getString("message");

            text = new TextView(this);
            text.setText(level);
            if (level.equals("notice"))
                text.setTextColor(0xff006e9c);
            if(level.equals(("warning")))
                text.setTextColor(0xffee593a);
            text.setTextSize(16);
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.16), LinearLayout.LayoutParams.MATCH_PARENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);

            text = new TextView(this);
            text.setText(source);
            text.setTextSize(16);
            text.setTextColor(Color.BLACK);
            if(source.length() > message.length())
                text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.42), LinearLayout.LayoutParams.WRAP_CONTENT));
            else text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.42), LinearLayout.LayoutParams.MATCH_PARENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);

            text = new TextView(this);
            text.setText(message);
            text.setTextSize(16);
            text.setTextColor(Color.BLACK);
            if(source.length() < message.length())
                text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.42), LinearLayout.LayoutParams.WRAP_CONTENT));
            else text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.42), LinearLayout.LayoutParams.MATCH_PARENT));
            text.setBackgroundResource(R.drawable.cell_shape);
            layout.addView(text);

            list.addView(layout);
        }

    }

    public static void setInfo(Map<String, Object> tag){
        name = tag.get("name").toString();
        id = (int) tag.get("id");
    }
}
