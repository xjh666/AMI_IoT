package com.example.foremanproject.activity;

import android.os.Bundle;
import android.os.Handler;
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
import com.example.foremanproject.other.UserInfo;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Dashboard extends AppCompatActivity {
    private Handler mHandler;
    private int mInterval = 30000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Dashboard");
        setContentView(R.layout.activity_dashboard);
        mHandler = new Handler();

        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            sendRequestForConfigChartAndStatus();
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void showAllHosts(View view){
    }

    public void showHostGroups(View view){
    }

    public void refresh(View view){
        sendRequestForConfigChartAndStatus();
        sendRequestForTimeAndEvent();
    }

    private void sendRequestForTimeAndEvent(){
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/hosts"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getInfoForTimeAndEvent(response);
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

    private void getInfoForTimeAndEvent(JSONObject response) throws JSONException{
        JSONArray result = sort((JSONArray)response.get("results"));
    }

    private JSONArray sort(JSONArray arr){

        return arr;
    }

    private void sendRequestForConfigChartAndStatus() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/dashboard"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getInfoForConfigChartAndStatus(response);
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

    private void getInfoForConfigChartAndStatus(JSONObject response) throws JSONException {
        String[] labels = new String[]{"Active ", "Error ", "OK ", "Pending changes ", "Out of sync ", "No reports ", "Notification... "};

        int totalHosts = response.getInt("total_hosts");
        int data[] = {  response.getInt("active_hosts"),
                        response.getInt("bad_hosts"),
                        response.getInt("ok_hosts"),
                        response.getInt("pending_hosts"),
                        response.getInt("out_of_sync_hosts"),
                        response.getInt("reports_missing"),
                        response.getInt("disabled_hosts")};

        String display = labels[0];
        int max = data[0];
        for(int i=1;i<7;i++) {
            if (max < data[i]) {
                max = data[i];
                display = labels[i];
            }
        }

        setText(totalHosts, data, (max*100/totalHosts + "%  " + display));
        drawPieChart(labels,data);
    }

    private void setText(int totalHost, int[] data, String percentage){
        TextView total = (TextView) findViewById(R.id.totalHost);
        total.setText(totalHost + "");

        TextView active = (TextView) findViewById(R.id.activeHost);
        active.setText(data[0] + "");

        TextView bad = (TextView) findViewById(R.id.badHost);
        bad.setText(data[1] + "");

        TextView ok = (TextView) findViewById(R.id.okHost);
        ok.setText(data[2] + "");

        TextView pending = (TextView) findViewById(R.id.pendingHost);
        pending.setText(data[3] + "");

        TextView outOfSync = (TextView) findViewById(R.id.outOfSyncHost);
        outOfSync.setText(data[4] + "");

        TextView miss = (TextView) findViewById(R.id.reportMissing);
        miss.setText(data[5] + "");

        TextView disabled = (TextView) findViewById(R.id.disabledHost);
        disabled.setText(data[6] + "");

        TextView percent = (TextView) findViewById(R.id.percentage);
        percent.setText(percentage);
    }

    private void drawPieChart(String[] labels, int[] data){
        int COLOR[] = {0xFF4572A7, 0xFFAA4643, 0xFF89A541, 0xFF80699B, 0xFF3D96AE, 0xFFDB843D, 0xFF92A8CD};
        CategorySeries distributionSeries = new CategorySeries("PieChart");
        for(int i=0;i<7;i++) {
            if(data[i] == 0) continue;
            distributionSeries.add(labels[i], data[i]);
        }
        DefaultRenderer defaultRenderer = new DefaultRenderer();
        for (int i = 0; i < 7; i++) {
            if(data[i]==0) continue;
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(COLOR[i]);
            seriesRenderer.setDisplayBoundingPoints(true);

            defaultRenderer.setLabelsTextSize(40f);
            defaultRenderer.setLegendTextSize(50f);
            defaultRenderer.setApplyBackgroundColor(false);
            defaultRenderer.addSeriesRenderer(seriesRenderer);
        }
        defaultRenderer.setZoomButtonsVisible(false);
        LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_container);
        // remove any views before u paint the chart
        chartContainer.removeAllViews();
        // drawing pie chart
        View mChart = ChartFactory.getPieChartView(getBaseContext(),
                distributionSeries, defaultRenderer);
        // adding the view to the linearlayout
        chartContainer.addView(mChart);
    }
}



