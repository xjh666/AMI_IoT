package com.example.foremanproject.fragment;

import android.content.Intent;
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
import com.example.foremanproject.activity.HostDetail;
import com.example.foremanproject.activity.HostOfAConfigurationStatus;
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
    Handler mHandler;

    final int ThirtyMinutesInMilliseconds = 1800000;
    final int ThreeMinutesInMilliseconds = 180000;
    final int numLatestEventTableRows = 10;

    boolean isShowDetail;

    String nameOfHostToShowDetail;
    String configurationLabelOfHosts;

    LinearLayout histogramContainer;
    LinearLayout chartContainer;
    LinearLayout statusTable;
    LinearLayout latestEventTable;

    TextView percent;
    TextView latestEventTableText[][];
    TextView configurationStatusTable[][];


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mHandler = new Handler();
        percent = (TextView) view.findViewById(R.id.percentage);
        statusTable = (LinearLayout) view.findViewById(R.id.StatusTable);
        latestEventTable = (LinearLayout) view.findViewById(R.id.LatestEvents);
        chartContainer = (LinearLayout) view.findViewById(R.id.chart_container);
        histogramContainer = (LinearLayout) view.findViewById(R.id.histogram_container);

        initializeHostConfigurationStatusTable();
        initializeLatestEventTable();

        startRepeatingTask();

        return view;
    }

    public static Dashboard newInstance() { return new Dashboard(); }

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
                            switch (api) {
                                case "reports":
                                    getReports(response);
                                    break;
                                case "hosts":
                                    if(isShowDetail) getHostDetail(response);
                                    else getHostsOfAConfigurationType(response);
                                    break;
                                default:
                                    getInfoForChartAndStatus(response);
                                    break;
                            }
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
        int num = 9;

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
            setLatestEvent(i+1, hostName, status);
        }
        drawHistogram(runDistribution);
    }

    private void setLatestEvent(int i, final String hostName, Map<String, Map<String, Integer>> status ){
        for(int j=0;j<7;j++){
            switch (j){
                case 0:
                    latestEventTableText[i][j].setText(" " + hostName);
                    latestEventTableText[i][j].setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            nameOfHostToShowDetail= hostName;
                            isShowDetail = true;
                            sendRequest("hosts");
                        }
                    });
                    break;
                case 1:
                    latestEventTableText[i][j].setText(status.get(hostName).get("applied").toString());
                    break;
                case 2:
                    latestEventTableText[i][j].setText(status.get(hostName).get("restarted").toString());
                    break;
                case 3:
                    latestEventTableText[i][j].setText(status.get(hostName).get("failed").toString());
                    break;
                case 4:
                    latestEventTableText[i][j].setText(status.get(hostName).get("failed_restarts").toString());
                    break;
                case 5:
                    latestEventTableText[i][j].setText(status.get(hostName).get("skipped").toString());
                    break;
                case 6:
                    latestEventTableText[i][j].setText(status.get(hostName).get("pending").toString());
                    break;
            }
        }
    }

    private void getInfoForChartAndStatus(JSONObject response) throws JSONException {
        String[] labels = new String[]{"Active ", "Error ", "OK ", "Pending changes ", "Out of sync ", "No reports ", "Notification... "};

        int totalHosts = response.getInt("total_hosts");
        int data[] = {  response.getInt("active_hosts_ok_enabled"),
                        response.getInt("bad_hosts_enabled"),
                        response.getInt("ok_hosts_enabled"),
                        response.getInt("pending_hosts_enabled"),
                        response.getInt("out_of_sync_hosts_enabled"),
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
        configurationStatusTable = new TextView[8][2];
        LinearLayout row;

        for(int i=0;i<8;i++){
            row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            for(int j=0;j<2;j++){
                configurationStatusTable[i][j] = new TextView(getActivity());

                if(j==1) {
                    configurationStatusTable[i][j].setText("");
                } else {
                    switch (i){
                        case 0:
                            configurationStatusTable[i][j].setText("Total Hosts");
                            break;
                        case 1:
                            configurationStatusTable[i][j].setText("Hosts that had performed modifications without error");
                            break;
                        case 2:
                            configurationStatusTable[i][j].setText("Hosts in error state");
                            break;
                        case 3:
                            configurationStatusTable[i][j].setText("Good host reports in the last 35 minutes");
                            break;
                        case 4:
                            configurationStatusTable[i][j].setText("Hosts that had pending changes");
                            break;
                        case 5:
                            configurationStatusTable[i][j].setText("Out of sync hosts");
                            break;
                        case 6:
                            configurationStatusTable[i][j].setText("Hosts with no reports");
                            break;
                        case 7:
                            configurationStatusTable[i][j].setText("Hosts with Alerts disabled");
                            break;
                    }
                }

                switch (i){
                    case 0:
                        configurationStatusTable[i][j].setTextColor(Color.BLACK);
                        break;
                    case 1:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorHostsThatHadPerformedModificationsWithoutError));
                        break;
                    case 2:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorHostsInErrorState));
                        break;
                    case 3:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorGoodHostReportsInTheLast35Minutes));
                        break;
                    case 4:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorHostsThatHadPendingChanges));
                        break;
                    case 5:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorOutOfSyncHosts));
                        break;
                    case 6:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorHostsWithNoReports));
                        break;
                    case 7:
                        configurationStatusTable[i][j].setTextColor(getResources().getColor(R.color.colorHostsWithAlertsDisabled));
                        break;
                }

                if(i==0 && j==0) {
                    configurationStatusTable[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.92), (int) (Configuration.getHeight() * 0.05)));
                    configurationStatusTable[i][j].setTextSize(20);
                } else if(i==0 && j==1){
                    configurationStatusTable[i][j].setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.08), (int)(Configuration.getHeight()* 0.05)));
                    configurationStatusTable[i][j].setTextSize(25);
                } else if(j==0) {
                    configurationStatusTable[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.93), (int) (Configuration.getHeight() * 0.04)));
                    configurationStatusTable[i][j].setTextSize(14);
                } else {
                    configurationStatusTable[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.07), (int) (Configuration.getHeight() * 0.04)));
                    configurationStatusTable[i][j].setTextSize(20);
                }

                if(j==1)
                    configurationStatusTable[i][j].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                row.addView(configurationStatusTable[i][j]);
            }
            statusTable.addView(row);
        }

        final String configurationLabels[] = {"", "Active", "Error", "No changes", "Pending", "Out of sync", "No reports", "Alerts disabled"};
        for(int i=1;i<8;i++){
            final int finalI = i;
            configurationStatusTable[i][0].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    configurationLabelOfHosts = configurationLabels[finalI];
                    isShowDetail = false;
                    sendRequest("hosts");
                }
            });
        }
    }

    private void initializeLatestEventTable(){
        latestEventTableText = new TextView[numLatestEventTableRows][7];
        LinearLayout row;
        for(int i=0;i<numLatestEventTableRows;i++){
            row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);

            for(int j=0;j<7;j++) {
                latestEventTableText[i][j] = new TextView(getActivity());
                latestEventTableText[i][j].setBackgroundResource(R.drawable.cell_shape);
                latestEventTableText[i][j].setTextColor(Color.BLACK);

                if (i == 0) {
                    switch (j) {
                        case 0:
                            latestEventTableText[i][j].setText(" Host");
                            break;
                        case 1:
                            latestEventTableText[i][j].setText("A");
                            break;
                        case 2:
                            latestEventTableText[i][j].setText("R");
                            break;
                        case 3:
                            latestEventTableText[i][j].setText("F");
                            break;
                        case 4:
                            latestEventTableText[i][j].setText("FR");
                            break;
                        case 5:
                            latestEventTableText[i][j].setText("S");
                            break;
                        case 6:
                            latestEventTableText[i][j].setText("P");
                            break;
                    }
                } else latestEventTableText[i][j].setText("");

                if (j == 0) {
                    latestEventTableText[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
                } else if (j == 6) {
                    latestEventTableText[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
                } else
                    latestEventTableText[i][j].setLayoutParams(new LinearLayout.LayoutParams((int) (Configuration.getWidth() * 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));

                if(i==0){
                    latestEventTableText[i][j].setTextSize(19);
                } else latestEventTableText[i][j].setTextSize(16);

                if(j!=0)
                    latestEventTableText[i][j].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                row.addView(latestEventTableText[i][j]);
            }

            latestEventTable.addView(row);
        }
    }

    private void setHostConfigurationStatusTable(int total, int[] data, String percentage){
        for(int i=0;i<8;i++){
            if(i==0) {
                configurationStatusTable[i][1].setText(total + "");
            } else{
                configurationStatusTable[i][1].setText(data[i-1] + "");
            }
        }
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
        mRenderer.setXTitle("Within x Minutes");
        mRenderer.setAxisTitleTextSize(60);
        mRenderer.setLabelsColor(0xFF000000);

        GraphicalView chartView = ChartFactory.getBarChartView(getActivity(), dataset, mRenderer, BarChart.Type.DEFAULT);
        histogramContainer.addView(chartView,0);
    }

    private void getHostDetail(JSONObject response) throws JSONException {
        JSONArray arr = response.getJSONArray("results");

        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            if (obj.getString("name").equals(nameOfHostToShowDetail)){
                HostDetail.setInfo(obj.getString("global_status_label"),
                                obj.getString("configuration_status_label"),
                                obj.getString("ip"),
                                obj.getString("mac"),
                                obj.getString("environment_name"),
                                obj.getString("architecture_name"),
                                obj.getString("operatingsystem_name"),
                                obj.getString("owner_type"),
                                obj.getString("hostgroup_title"),
                                nameOfHostToShowDetail
                );
                startActivity(new Intent(getActivity(), HostDetail.class));
                break;
            }
        }
    }

    private void getHostsOfAConfigurationType(JSONObject response) throws JSONException {
        JSONArray arr = response.getJSONArray("results");
        ArrayList<String> hosts = new ArrayList<>();
        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("configuration_status_label").equals(configurationLabelOfHosts))
                hosts.add(obj.getString("name"));
        }
        HostOfAConfigurationStatus.setInfo(hosts, configurationLabelOfHosts);
        startActivity(new Intent(getActivity(), HostOfAConfigurationStatus.class));
    }
}