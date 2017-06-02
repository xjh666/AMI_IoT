package com.example.foremanproject.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Dashboard extends Fragment {
    private Handler mHandler;
    private int mInterval = 30000;
    final int ThirtyMinutesInMilliseconds = 1800000;
    final int ThreeMinutesInMilliseconds = 180000;
    public static Dashboard newInstance() { return new Dashboard(); }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mHandler = new Handler();
        startRepeatingTask();
        return view;
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
            sendRequestForReports();
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() { mStatusChecker.run(); }

    void stopRepeatingTask() { mHandler.removeCallbacks(mStatusChecker); }

    private void sendRequestForReports(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/reports/"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getReports(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
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

    private void getReports(JSONObject response) throws JSONException, ParseException {
        Map<String, Map<String, Integer>> status = new HashMap<>();
        ArrayList<String> order = new ArrayList<>();
        int [] runDistribution = {0,0,0,0,0,0,0,0,0,0};
        int num = 10;

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

        JSONArray arr = (JSONArray)response.get("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = (JSONObject) arr.get(i);
            String host = obj.getString("host_name");
            if(obj.getJSONObject("metrics").getJSONObject("changes").getInt("total") > 0 && num-->0){
                order.add(host);
                status.put(host,new HashMap<String, Integer>());
                JSONObject hostStatus = (JSONObject) obj.get("status");
                if((hostStatus.getInt("applied")) > 0)
                    status.get(host).put("appiled",(hostStatus.getInt("applied")));

                if((hostStatus.getInt("restarted")) > 0)
                    status.get(host).put("restarted",(hostStatus.getInt("restarted")));

                if((hostStatus.getInt("failed")) > 0)
                    status.get(host).put("failed",(hostStatus.getInt("failed")));

                if((hostStatus.getInt("failed_restarts")) > 0)
                    status.get(host).put("failed_restarts",(hostStatus.getInt("failed_restarts")));

                if((hostStatus.getInt("skipped")) > 0)
                    status.get(host).put("skipped",(hostStatus.getInt("skipped")));

                if((hostStatus.getInt("pending")) > 0)
                    status.get(host).put("pending",(hostStatus.getInt("pending")));
            }

            Date time = sdf.parse(obj.getString("created_at").substring(0,10) + " " + obj.getString("created_at").substring(11,19));
            long timeDifference = currentTime.getTime() - time.getTime();
            if(timeDifference >= ThirtyMinutesInMilliseconds)
                continue;
            runDistribution[(int)timeDifference/ThreeMinutesInMilliseconds]++;
        }
    }

    private void sendRequestForConfigChartAndStatus() {
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (UserInfo.getUrl() + "api/dashboard"), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getInfoForChartAndStatus(response);
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

    private void getInfoForChartAndStatus(JSONObject response) throws JSONException {
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
        TextView total = (TextView) getView().findViewById(R.id.totalHost);
        total.setText(totalHost + "");

        TextView active = (TextView) getView().findViewById(R.id.activeHost);
        active.setText(data[0] + "");

        TextView bad = (TextView) getView().findViewById(R.id.badHost);
        bad.setText(data[1] + "");

        TextView ok = (TextView) getView().findViewById(R.id.okHost);
        ok.setText(data[2] + "");

        TextView pending = (TextView) getView().findViewById(R.id.pendingHost);
        pending.setText(data[3] + "");

        TextView outOfSync = (TextView) getView().findViewById(R.id.outOfSyncHost);
        outOfSync.setText(data[4] + "");

        TextView miss = (TextView) getView().findViewById(R.id.reportMissing);
        miss.setText(data[5] + "");

        TextView disabled = (TextView) getView().findViewById(R.id.disabledHost);
        disabled.setText(data[6] + "");

        TextView percent = (TextView) getView().findViewById(R.id.percentage);
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
            defaultRenderer.setLabelsColor(0xff000000);
            defaultRenderer.setLegendTextSize(50f);
            defaultRenderer.setApplyBackgroundColor(false);
            defaultRenderer.addSeriesRenderer(seriesRenderer);
        }
        defaultRenderer.setZoomButtonsVisible(false);
        LinearLayout chartContainer = (LinearLayout) getView().findViewById(R.id.chart_container);
        // remove any views before u paint the chart
        chartContainer.removeAllViews();
        // drawing pie chart
        View mChart = ChartFactory.getPieChartView(getActivity(),
                distributionSeries, defaultRenderer);
        // adding the view to the linearlayout
        chartContainer.addView(mChart);
    }

    public interface OnDashboardSelected {
        void OnDashboardSelected(int imageResId, String name,
                                 String description, String url);
    }
}



