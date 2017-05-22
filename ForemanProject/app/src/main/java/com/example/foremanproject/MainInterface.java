package com.example.foremanproject;

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

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainInterface extends AppCompatActivity {
    String api;
    String title;
    int request = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.main_interface);
        sendRequest(request);
    }
    public void showAllHosts(View view){

    }

    public void showHostGroups(View view){

    }

    public void showHostConfigurationStatus(View view){
        request = 1;
        sendRequest(request);
    }

    public void showHostConfigurationChart(View view){
        request = 2;
        title = "Host Configuration Chart";
        sendRequest(request);
    }

    public void showRunDistribution(View view){
        request = 3;
        sendRequest(request);
    }

    public void showLatestEvents(View view){
        request = 4;
        sendRequest(request);
    }

    public void refresh(View view){
        sendRequest(request);
    }

    private void sendRequest(int request) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final int drawChart;
        switch(request) {
            case 1:
                drawChart = 2;
                break;
            case 2:
                drawChart = 2;
                api = "api/dashboard";
                break;
            case 3:
                drawChart = 2;
                break;
            default:
                drawChart = 2;
                break;
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            switch(drawChart) {
                                case 1:
                                    break;
                                case 2:
                                    drawPieChart(response);
                                    break;
                                case 3:
                                    break;
                                case 4:
                                    break;
                            }
                            drawPieChart(response);
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


    private void drawPieChart(JSONObject response) throws JSONException {
        int COLOR[] = {0xFF4572A7, 0xFFAA4643, 0xFF89A541, 0xFF80699B, 0xFF3D96AE, 0xFFDB843D, 0xFF92A8CD};
        String[] labels = new String[]{"Active ", "Error ", "OK ", "Pending changes ", "Out of sync ", "No reports ", "Notification... "};

        int totalHosts = response.getInt("total_hosts");
        int activeHosts = response.getInt("active_hosts");
        int badHosts = response.getInt("bad_hosts");
        int okHosts = response.getInt("ok_hosts");
        int pendingHosts = response.getInt("pending_hosts");
        int outOfSyncHosts = response.getInt("out_of_sync_hosts");
        int reportsMissing = response.getInt("reports_missing");
        int notification = response.getInt("disabled_hosts");
        int data[] = {activeHosts, badHosts, okHosts, pendingHosts, outOfSyncHosts, reportsMissing, notification};

        String display = labels[0];
        int max = data[0];
        for(int i=1;i<7;i++){
            if(max < data[i]){
                max = data[i];
                display = labels[i];
            }
        }
        TextView text = (TextView) findViewById(R.id.ActivePercentage);
        text.setText(max*100/totalHosts + "%  " + display);

        TextView titleText = (TextView) findViewById(R.id.title);
        titleText.setText(title);

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
