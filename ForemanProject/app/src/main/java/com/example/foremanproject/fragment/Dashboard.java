package com.example.foremanproject.fragment;

import android.graphics.Color;
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
import com.example.foremanproject.other.Configuration;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
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

/**
 * Created by Xie Jihui on 5/22/2017.
 */

/**
 * This class is a fragment for the dashboard
 * The dashboard consists of 4 components: Host Configuration Chart(Pie Chart), Host Configuration Status(Table),
 * Run Distribution in the last 30 minutes(Histogram) and Latest Events(Table)
 * "GET /api/dashboard" is used to get information related to Host Configuration Chart and Host Configuration Status
 * "GET /api/reports" is used to get information related toRun Distribution in the last 30 minutes and Latest Events
 * The requests are sent and the dashboard is updated every 30 seconds
 * "AChartEngine" is used to draw the pie chart and histogram
 */

public class Dashboard extends Fragment {
    private Handler mHandler;
    final int ThirtyMinutesInMilliseconds = 1800000;
    final int ThreeMinutesInMilliseconds = 180000;
    public static Dashboard newInstance() { return new Dashboard(); }

    TextView totalHost;
    TextView totalHostText;
    TextView activeHost;
    TextView activeHostText;
    TextView badHost;
    TextView badHostText;
    TextView okHost;
    TextView okHostText;
    TextView pendingHost;
    TextView pendingHostText;
    TextView outOfSyncHost;
    TextView outOfSyncHostText;
    TextView reportMissing;
    TextView reportMissingText;
    TextView disabledHost;
    TextView disabledHostText;
    LinearLayout table;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mHandler = new Handler();
        table = (LinearLayout) view.findViewById(R.id.tablelayout);
        initializeHostConfigurationStatusTable();
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
            sendRequest("reports");
            sendRequest("dashboard");
            int mInterval = 30000;
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    void startRepeatingTask() { mStatusChecker.run(); }

    void stopRepeatingTask() { mHandler.removeCallbacks(mStatusChecker); }

    private void sendRequest(final String api){
        RequestQueue queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, (Configuration.getUrl() + "api/" + api), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (api.equals("reports"))
                                getReports(response);
                            else  getInfoForChartAndStatus(response);
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

    private void getReports(JSONObject response) throws JSONException, ParseException {
        Map<String, Map<String, Integer>> status = new HashMap<>();
        ArrayList<String> order = new ArrayList<>();
        int [] runDistribution = {0,0,0,0,0,0,0,0,0,0};
        int num = 10;

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

        JSONArray arr = response.getJSONArray("results");
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            String host = obj.getString("host_name");
            if(obj.getJSONObject("metrics").getJSONObject("changes").getInt("total") > 0 && num-->0){
                order.add(host);
                status.put(host,new HashMap<String, Integer>());
                JSONObject hostStatus = obj.getJSONObject("status");
                status.get(host).put("applied",(hostStatus.getInt("applied")));
                status.get(host).put("restarted",(hostStatus.getInt("restarted")));
                status.get(host).put("failed",(hostStatus.getInt("failed")));
                status.get(host).put("failed_restarts",(hostStatus.getInt("failed_restarts")));
                status.get(host).put("skipped",(hostStatus.getInt("skipped")));
                status.get(host).put("pending",(hostStatus.getInt("pending")));
            }

            Date time = sdf.parse(obj.getString("created_at").substring(0,10) + " " + obj.getString("created_at").substring(11,19));
            long timeDifference = currentTime.getTime() - time.getTime();
            if(timeDifference >= ThirtyMinutesInMilliseconds)
                continue;
            runDistribution[(int)timeDifference/ThreeMinutesInMilliseconds]++;
        }

        for(int i=0;i<order.size();i++){
            String hostName = order.get(i);
            setLatestEvent(i, hostName, status);
        }
        drawHistogram(runDistribution);
    }

    private void setLatestEvent(int i, String hostName, Map<String, Map<String, Integer>> status ){
        TextView text;
        switch (i){
            case 0:
                text = (TextView) getView().findViewById(R.id.id1);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a1);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r1);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f1);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr1);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s1);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p1);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 1:
                text = (TextView) getView().findViewById(R.id.id2);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a2);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r2);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f2);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr2);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s2);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p2);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 2:
                text = (TextView) getView().findViewById(R.id.id3);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a3);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r3);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f3);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr3);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s3);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p3);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 3:
                text = (TextView) getView().findViewById(R.id.id4);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a4);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r4);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f4);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr4);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s4);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p4);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 4:
                text = (TextView) getView().findViewById(R.id.id5);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a5);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r5);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f5);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr5);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s5);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p5);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 5:
                text = (TextView) getView().findViewById(R.id.id6);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a6);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r6);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f6);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr6);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s6);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p6);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 6:
                text = (TextView) getView().findViewById(R.id.id7);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a7);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r7);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f7);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr7);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s7);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p7);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 7:
                text = (TextView) getView().findViewById(R.id.id8);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a8);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r8);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f8);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr8);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s8);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p8);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 8:
                text = (TextView) getView().findViewById(R.id.id9);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a9);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r9);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f9);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr9);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s9);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p9);
                text.setText(status.get(hostName).get("pending").toString());
                break;
            case 9:
                text = (TextView) getView().findViewById(R.id.id10);
                text.setText(" " + hostName);
                text = (TextView) getView().findViewById(R.id.a10);
                text.setText(status.get(hostName).get("applied").toString());
                text = (TextView) getView().findViewById(R.id.r10);
                text.setText(status.get(hostName).get("restarted").toString());
                text = (TextView) getView().findViewById(R.id.f10);
                text.setText(status.get(hostName).get("failed").toString());
                text = (TextView) getView().findViewById(R.id.fr10);
                text.setText(status.get(hostName).get("failed_restarts").toString());
                text = (TextView) getView().findViewById(R.id.s10);
                text.setText(status.get(hostName).get("skipped").toString());
                text = (TextView) getView().findViewById(R.id.p10);
                text.setText(status.get(hostName).get("pending").toString());
                break;
        }
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

        setHostConfigurationStatusTable(totalHosts, data, (max*100/totalHosts + "%  " + display));
        drawPieChart(labels,data);
    }

    private void initializeHostConfigurationStatusTable(){
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        totalHostText = new TextView(getActivity());
        totalHostText.setText("Total Hosts");
        totalHostText.setTextColor(Color.BLACK);
        totalHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.92), (int)(Configuration.getHeight()* 0.05)));
        totalHostText.setTextSize(20);
        totalHost = new TextView(getActivity());
        totalHost.setText("");
        totalHost.setTextColor(Color.BLACK);
        totalHost.setTextSize(25);
        totalHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        totalHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.08), (int)(Configuration.getHeight()* 0.05)));
        row.addView(totalHostText);
        row.addView(totalHost);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        activeHostText = new TextView(getActivity());
        activeHostText.setTextColor(0xff4572a7);
        activeHostText.setTextSize(14);
        activeHostText.setText("Hosts that had performed modifications without error");
        activeHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        activeHost = new TextView(getActivity());
        activeHost.setText("");
        activeHost.setTextColor(0xff4572a7);
        activeHost.setTextSize(20);
        activeHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        activeHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(activeHostText);
        row.addView(activeHost);
        table.addView(row);
        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        badHostText = new TextView(getActivity());
        badHostText.setTextColor(0xffaa4343);
        badHostText.setTextSize(14);
        badHostText.setText("Hosts in error state");
        badHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        badHost = new TextView(getActivity());
        badHost.setText("");
        badHost.setTextColor(0xffaa4343);
        badHost.setTextSize(20);
        badHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        badHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(badHostText);
        row.addView(badHost);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        okHostText = new TextView(getActivity());
        okHostText.setTextColor(0xff89a54e);
        okHostText.setTextSize(14);
        okHostText.setText("Good host reports in the last 35 minutes");
        okHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        okHost = new TextView(getActivity());
        okHost.setText("");
        okHost.setTextColor(0xff89a54e);
        okHost.setTextSize(20);
        okHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        okHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(okHostText);
        row.addView(okHost);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        pendingHostText = new TextView(getActivity());
        pendingHostText.setTextColor(0xff80699b);
        pendingHostText.setTextSize(14);
        pendingHostText.setText("Hosts that had pending changes");
        pendingHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        pendingHost = new TextView(getActivity());
        pendingHost.setText("");
        pendingHost.setTextColor(0xff80699b);
        pendingHost.setTextSize(20);
        pendingHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        pendingHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(pendingHostText);
        row.addView(pendingHost);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        outOfSyncHostText = new TextView(getActivity());
        outOfSyncHostText.setTextColor(0xff3d96ae);
        outOfSyncHostText.setTextSize(14);
        outOfSyncHostText.setText("Out of sync hosts");
        outOfSyncHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        outOfSyncHost = new TextView(getActivity());
        outOfSyncHost.setText("");
        outOfSyncHost.setTextColor(0xff3d96ae);
        outOfSyncHost.setTextSize(20);
        outOfSyncHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        outOfSyncHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(outOfSyncHostText);
        row.addView(outOfSyncHost);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        reportMissingText = new TextView(getActivity());
        reportMissingText.setTextColor(0xffdb843d);
        reportMissingText.setTextSize(14);
        reportMissingText.setText("Hosts with no reports");
        reportMissingText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        reportMissing = new TextView(getActivity());
        reportMissing.setText("");
        reportMissing.setTextColor(0xffdb843d);
        reportMissing.setTextSize(20);
        reportMissing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        reportMissing.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(reportMissingText);
        row.addView(reportMissing);
        table.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        disabledHostText = new TextView(getActivity());
        disabledHostText.setTextColor(0xff92abcd);
        disabledHostText.setTextSize(14);
        disabledHostText.setText("Hosts with Alerts disabled");
        disabledHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        disabledHost = new TextView(getActivity());
        disabledHost.setText("");
        disabledHost.setTextColor(0xff92abcd);
        disabledHost.setTextSize(20);
        disabledHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        disabledHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(disabledHostText);
        row.addView(disabledHost);
        table.addView(row);
    }

    private void setHostConfigurationStatusTable(int total, int[] data, String percentage){
        totalHost.setText(total + "");
        activeHost.setText(data[0] + "");
        badHost.setText(data[1] + "");
        okHost.setText(data[2] + "");
        pendingHost.setText(data[3] + "");
        outOfSyncHost.setText(data[4] + "");
        reportMissing.setText(data[5] + "");
        disabledHost.setText(data[6] + "");

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
            defaultRenderer.setPanEnabled(false);
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

    private void drawHistogram(int [] runDistribution){
        XYSeries series = new XYSeries("");
        series.add(0,0);
        for(int i = 0; i<10; i++)
            series.add(3*(i+1),runDistribution[i]);

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(0xff00608a);


        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        mRenderer.setXLabels(0);
        for(int i = 3; i<=30; i+=3)
            mRenderer.addXTextLabel(i, Integer.toString(i));

        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(7);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true);
        mRenderer.setLabelsTextSize(40f);
        mRenderer.setXLabelsColor(0xff000000);
        mRenderer.setBarSpacing(0.1);
        mRenderer.setXTitle("Minutes ago");
        mRenderer.setAxisTitleTextSize(60);
        mRenderer.setLabelsColor(0xFF000000);

        GraphicalView chartView = ChartFactory.getBarChartView(getActivity(), dataset, mRenderer, BarChart.Type.DEFAULT);
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.histogram_container);
        layout.addView(chartView,0);
    }
}



