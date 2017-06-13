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

    //for Host Configuration Status Table
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
    LinearLayout statusTable;

    //for Latest Event Table
    LinearLayout eventTable;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;
    TextView id1;
    TextView a1;
    TextView r1;
    TextView f1;
    TextView fr1;
    TextView s1;
    TextView p1;
    TextView id2;
    TextView a2;
    TextView r2;
    TextView f2;
    TextView fr2;
    TextView s2;
    TextView p2;
    TextView id3;
    TextView a3;
    TextView r3;
    TextView f3;
    TextView fr3;
    TextView s3;
    TextView p3;
    TextView id4;
    TextView a4;
    TextView r4;
    TextView f4;
    TextView fr4;
    TextView s4;
    TextView p4;
    TextView id5;
    TextView a5;
    TextView r5;
    TextView f5;
    TextView fr5;
    TextView s5;
    TextView p5;
    TextView id6;
    TextView a6;
    TextView r6;
    TextView f6;
    TextView fr6;
    TextView s6;
    TextView p6;
    TextView id7;
    TextView a7;
    TextView r7;
    TextView f7;
    TextView fr7;
    TextView s7;
    TextView p7;
    TextView id8;
    TextView a8;
    TextView r8;
    TextView f8;
    TextView fr8;
    TextView s8;
    TextView p8;
    TextView id9;
    TextView a9;
    TextView r9;
    TextView f9;
    TextView fr9;
    TextView s9;
    TextView p9;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mHandler = new Handler();
        statusTable = (LinearLayout) view.findViewById(R.id.tablelayout);
        eventTable = (LinearLayout) view.findViewById(R.id.LatestEvents);
        initializeHostConfigurationStatusTable();
        initializeLatestEventTable();
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
            setLatestEvent(i, hostName, status);
        }
        drawHistogram(runDistribution);
    }

    private void setLatestEvent(int i, String hostName, Map<String, Map<String, Integer>> status ){
        switch (i){
            case 0:
                id1.setText(" " + hostName);
                a1.setText(status.get(hostName).get("applied").toString());
                r1.setText(status.get(hostName).get("restarted").toString());
                f1.setText(status.get(hostName).get("failed").toString());
                fr1.setText(status.get(hostName).get("failed_restarts").toString());
                s1.setText(status.get(hostName).get("skipped").toString());
                p1.setText(status.get(hostName).get("pending").toString());
                break;
            case 1:
                id2.setText(" " + hostName);
                a2.setText(status.get(hostName).get("applied").toString());
                r2.setText(status.get(hostName).get("restarted").toString());
                f2.setText(status.get(hostName).get("failed").toString());
                fr2.setText(status.get(hostName).get("failed_restarts").toString());
                s2.setText(status.get(hostName).get("skipped").toString());
                p2.setText(status.get(hostName).get("pending").toString());
                break;
            case 2:
                id3.setText(" " + hostName);
                a3.setText(status.get(hostName).get("applied").toString());
                r3.setText(status.get(hostName).get("restarted").toString());
                f3.setText(status.get(hostName).get("failed").toString());
                fr3.setText(status.get(hostName).get("failed_restarts").toString());
                s3.setText(status.get(hostName).get("skipped").toString());
                p3.setText(status.get(hostName).get("pending").toString());
                break;
            case 3:
                id4.setText(" " + hostName);
                a4.setText(status.get(hostName).get("applied").toString());
                r4.setText(status.get(hostName).get("restarted").toString());
                f4.setText(status.get(hostName).get("failed").toString());
                fr4.setText(status.get(hostName).get("failed_restarts").toString());
                s4.setText(status.get(hostName).get("skipped").toString());
                p4.setText(status.get(hostName).get("pending").toString());
                break;
            case 4:
                id5.setText(" " + hostName);
                a5.setText(status.get(hostName).get("applied").toString());
                r5.setText(status.get(hostName).get("restarted").toString());
                f5.setText(status.get(hostName).get("failed").toString());
                fr5.setText(status.get(hostName).get("failed_restarts").toString());
                s5.setText(status.get(hostName).get("skipped").toString());
                p5.setText(status.get(hostName).get("pending").toString());
                break;
            case 5:
                id6.setText(" " + hostName);
                a6.setText(status.get(hostName).get("applied").toString());
                r6.setText(status.get(hostName).get("restarted").toString());
                f6.setText(status.get(hostName).get("failed").toString());
                fr6.setText(status.get(hostName).get("failed_restarts").toString());
                s6.setText(status.get(hostName).get("skipped").toString());
                p6.setText(status.get(hostName).get("pending").toString());
                break;
            case 6:
                id7.setText(" " + hostName);
                a7.setText(status.get(hostName).get("applied").toString());
                r7.setText(status.get(hostName).get("restarted").toString());
                f7.setText(status.get(hostName).get("failed").toString());
                fr7.setText(status.get(hostName).get("failed_restarts").toString());
                s7.setText(status.get(hostName).get("skipped").toString());
                p7.setText(status.get(hostName).get("pending").toString());
                break;
            case 7:
                id8.setText(" " + hostName);
                a8.setText(status.get(hostName).get("applied").toString());
                r8.setText(status.get(hostName).get("restarted").toString());
                f8.setText(status.get(hostName).get("failed").toString());
                fr8.setText(status.get(hostName).get("failed_restarts").toString());
                s8.setText(status.get(hostName).get("skipped").toString());
                p8.setText(status.get(hostName).get("pending").toString());
                break;
            case 8:
                id9.setText(" " + hostName);
                a9.setText(status.get(hostName).get("applied").toString());
                r9.setText(status.get(hostName).get("restarted").toString());
                f9.setText(status.get(hostName).get("failed").toString());
                fr9.setText(status.get(hostName).get("failed_restarts").toString());
                s9.setText(status.get(hostName).get("skipped").toString());
                p9.setText(status.get(hostName).get("pending").toString());
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
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        activeHostText = new TextView(getActivity());
        activeHostText.setTextColor(getResources().getColor(R.color.colorHostsThatHadPerformedModificationsWithoutError));
        activeHostText.setTextSize(14);
        activeHostText.setText("Hosts that had performed modifications without error");
        activeHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        activeHost = new TextView(getActivity());
        activeHost.setText("");
        activeHost.setTextColor(getResources().getColor(R.color.colorHostsThatHadPerformedModificationsWithoutError));
        activeHost.setTextSize(20);
        activeHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        activeHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(activeHostText);
        row.addView(activeHost);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        badHostText = new TextView(getActivity());
        badHostText.setTextColor(getResources().getColor(R.color.colorHostsInErrorState));
        badHostText.setTextSize(14);
        badHostText.setText("Hosts in error state");
        badHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        badHost = new TextView(getActivity());
        badHost.setText("");
        badHost.setTextColor(getResources().getColor(R.color.colorHostsInErrorState));
        badHost.setTextSize(20);
        badHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        badHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(badHostText);
        row.addView(badHost);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        okHostText = new TextView(getActivity());
        okHostText.setTextColor(getResources().getColor(R.color.colorGoodHostReportsInTheLast35Minutes));
        okHostText.setTextSize(14);
        okHostText.setText("Good host reports in the last 35 minutes");
        okHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        okHost = new TextView(getActivity());
        okHost.setText("");
        okHost.setTextColor(getResources().getColor(R.color.colorGoodHostReportsInTheLast35Minutes));
        okHost.setTextSize(20);
        okHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        okHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(okHostText);
        row.addView(okHost);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        pendingHostText = new TextView(getActivity());
        pendingHostText.setTextColor(getResources().getColor(R.color.colorHostsThatHadPendingChanges));
        pendingHostText.setTextSize(14);
        pendingHostText.setText("Hosts that had pending changes");
        pendingHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        pendingHost = new TextView(getActivity());
        pendingHost.setText("");
        pendingHost.setTextColor(getResources().getColor(R.color.colorHostsThatHadPendingChanges));
        pendingHost.setTextSize(20);
        pendingHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        pendingHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(pendingHostText);
        row.addView(pendingHost);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        outOfSyncHostText = new TextView(getActivity());
        outOfSyncHostText.setTextColor(getResources().getColor(R.color.colorOutOfSyncHosts));
        outOfSyncHostText.setTextSize(14);
        outOfSyncHostText.setText("Out of sync hosts");
        outOfSyncHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        outOfSyncHost = new TextView(getActivity());
        outOfSyncHost.setText("");
        outOfSyncHost.setTextColor(getResources().getColor(R.color.colorOutOfSyncHosts));
        outOfSyncHost.setTextSize(20);
        outOfSyncHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        outOfSyncHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(outOfSyncHostText);
        row.addView(outOfSyncHost);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        reportMissingText = new TextView(getActivity());
        reportMissingText.setTextColor(getResources().getColor(R.color.colorHostsWithNoReports));
        reportMissingText.setTextSize(14);
        reportMissingText.setText("Hosts with no reports");
        reportMissingText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        reportMissing = new TextView(getActivity());
        reportMissing.setText("");
        reportMissing.setTextColor(getResources().getColor(R.color.colorHostsWithNoReports));
        reportMissing.setTextSize(20);
        reportMissing.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        reportMissing.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(reportMissingText);
        row.addView(reportMissing);
        statusTable.addView(row);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        disabledHostText = new TextView(getActivity());
        disabledHostText.setTextColor(getResources().getColor(R.color.colorHostsWithAlertsDisabled));
        disabledHostText.setTextSize(14);
        disabledHostText.setText("Hosts with Alerts disabled");
        disabledHostText.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.93), (int)(Configuration.getHeight()* 0.04)));
        disabledHost = new TextView(getActivity());
        disabledHost.setText("");
        disabledHost.setTextColor(getResources().getColor(R.color.colorHostsWithAlertsDisabled));
        disabledHost.setTextSize(20);
        disabledHost.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        disabledHost.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.07), (int)(Configuration.getHeight()* 0.04)));
        row.addView(disabledHostText);
        row.addView(disabledHost);
        statusTable.addView(row);
    }

    private void initializeLatestEventTable(){
        textView1 = new TextView(getActivity());
        textView1.setText(" Host");
        textView1.setTextColor(Color.BLACK);
        textView1.setTextSize(19);
        textView1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView1.setBackgroundResource(R.drawable.cell_shape);

        textView2 = new TextView(getActivity());
        textView2.setText("A");
        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView2.setTextColor(Color.BLACK);
        textView2.setTextSize(19);
        textView2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView2.setBackgroundResource(R.drawable.cell_shape);

        textView3 = new TextView(getActivity());
        textView3.setText("R");
        textView3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView3.setTextColor(Color.BLACK);
        textView3.setTextSize(19);
        textView3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView3.setBackgroundResource(R.drawable.cell_shape);

        textView4 = new TextView(getActivity());
        textView4.setText("F");
        textView4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView4.setTextColor(Color.BLACK);
        textView4.setTextSize(19);
        textView4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView4.setBackgroundResource(R.drawable.cell_shape);

        textView5 = new TextView(getActivity());
        textView5.setText("FR");
        textView5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView5.setTextColor(Color.BLACK);
        textView5.setTextSize(19);
        textView5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView5.setBackgroundResource(R.drawable.cell_shape);

        textView6 = new TextView(getActivity());
        textView6.setText("S");
        textView6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView6.setTextColor(Color.BLACK);
        textView6.setTextSize(19);
        textView6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView6.setBackgroundResource(R.drawable.cell_shape);

        textView7 = new TextView(getActivity());
        textView7.setText("P");
        textView7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView7.setTextColor(Color.BLACK);
        textView7.setTextSize(19);
        textView7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        textView7.setBackgroundResource(R.drawable.cell_shape);

        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(textView1);
        row.addView(textView2);
        row.addView(textView3);
        row.addView(textView4);
        row.addView(textView5);
        row.addView(textView6);
        row.addView(textView7);
        eventTable.addView(row);

        id1 = new TextView(getActivity());
        id1.setText("");
        id1.setTextColor(Color.BLACK);
        id1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id1.setBackgroundResource(R.drawable.cell_shape);

        a1 = new TextView(getActivity());
        a1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a1.setText("");
        a1.setTextColor(Color.BLACK);
        a1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a1.setBackgroundResource(R.drawable.cell_shape);

        r1 = new TextView(getActivity());
        r1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r1.setText("");
        r1.setTextColor(Color.BLACK);
        r1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r1.setBackgroundResource(R.drawable.cell_shape);

        f1 = new TextView(getActivity());
        f1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f1.setText("");
        f1.setTextColor(Color.BLACK);
        f1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f1.setBackgroundResource(R.drawable.cell_shape);

        fr1 = new TextView(getActivity());
        fr1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr1.setText("");
        fr1.setTextColor(Color.BLACK);
        fr1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr1.setBackgroundResource(R.drawable.cell_shape);

        s1 = new TextView(getActivity());
        s1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s1.setText("");
        s1.setTextColor(Color.BLACK);
        s1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s1.setBackgroundResource(R.drawable.cell_shape);

        p1 = new TextView(getActivity());
        p1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p1.setText("");
        p1.setTextColor(Color.BLACK);
        p1.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p1.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id1);
        row.addView(a1);
        row.addView(r1);
        row.addView(f1);
        row.addView(fr1);
        row.addView(s1);
        row.addView(p1);
        eventTable.addView(row);

        id2 = new TextView(getActivity());
        id2.setText("");
        id2.setTextColor(Color.BLACK);
        id2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id2.setBackgroundResource(R.drawable.cell_shape);

        a2 = new TextView(getActivity());
        a2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a2.setText("");
        a2.setTextColor(Color.BLACK);
        a2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a2.setBackgroundResource(R.drawable.cell_shape);

        r2 = new TextView(getActivity());
        r2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r2.setText("");
        r2.setTextColor(Color.BLACK);
        r2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r2.setBackgroundResource(R.drawable.cell_shape);

        f2 = new TextView(getActivity());
        f2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f2.setText("");
        f2.setTextColor(Color.BLACK);
        f2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f2.setBackgroundResource(R.drawable.cell_shape);

        fr2 = new TextView(getActivity());
        fr2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr2.setText("");
        fr2.setTextColor(Color.BLACK);
        fr2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr2.setBackgroundResource(R.drawable.cell_shape);

        s2 = new TextView(getActivity());
        s2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s2.setText("");
        s2.setTextColor(Color.BLACK);
        s2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s2.setBackgroundResource(R.drawable.cell_shape);

        p2 = new TextView(getActivity());
        p2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p2.setText("");
        p2.setTextColor(Color.BLACK);
        p2.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p2.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id2);
        row.addView(a2);
        row.addView(r2);
        row.addView(f2);
        row.addView(fr2);
        row.addView(s2);
        row.addView(p2);
        eventTable.addView(row);

        id3 = new TextView(getActivity());
        id3.setText("");
        id3.setTextColor(Color.BLACK);
        id3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id3.setBackgroundResource(R.drawable.cell_shape);

        a3 = new TextView(getActivity());
        a3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a3.setText("");
        a3.setTextColor(Color.BLACK);
        a3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a3.setBackgroundResource(R.drawable.cell_shape);

        r3 = new TextView(getActivity());
        r3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r3.setText("");
        r3.setTextColor(Color.BLACK);
        r3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r3.setBackgroundResource(R.drawable.cell_shape);

        f3 = new TextView(getActivity());
        f3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f3.setText("");
        f3.setTextColor(Color.BLACK);
        f3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f3.setBackgroundResource(R.drawable.cell_shape);

        fr3 = new TextView(getActivity());
        fr3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr3.setText("");
        fr3.setTextColor(Color.BLACK);
        fr3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr3.setBackgroundResource(R.drawable.cell_shape);

        s3 = new TextView(getActivity());
        s3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s3.setText("");
        s3.setTextColor(Color.BLACK);
        s3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s3.setBackgroundResource(R.drawable.cell_shape);

        p3 = new TextView(getActivity());
        p3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p3.setText("");
        p3.setTextColor(Color.BLACK);
        p3.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p3.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id3);
        row.addView(a3);
        row.addView(r3);
        row.addView(f3);
        row.addView(fr3);
        row.addView(s3);
        row.addView(p3);
        eventTable.addView(row);

        id4 = new TextView(getActivity());
        id4.setText("");
        id4.setTextColor(Color.BLACK);
        id4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id4.setBackgroundResource(R.drawable.cell_shape);

        a4 = new TextView(getActivity());
        a4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a4.setText("");
        a4.setTextColor(Color.BLACK);
        a4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a4.setBackgroundResource(R.drawable.cell_shape);

        r4 = new TextView(getActivity());
        r4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r4.setText("");
        r4.setTextColor(Color.BLACK);
        r4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r4.setBackgroundResource(R.drawable.cell_shape);

        f4 = new TextView(getActivity());
        f4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f4.setText("");
        f4.setTextColor(Color.BLACK);
        f4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f4.setBackgroundResource(R.drawable.cell_shape);

        fr4 = new TextView(getActivity());
        fr4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr4.setText("");
        fr4.setTextColor(Color.BLACK);
        fr4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr4.setBackgroundResource(R.drawable.cell_shape);

        s4 = new TextView(getActivity());
        s4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s4.setText("");
        s4.setTextColor(Color.BLACK);
        s4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s4.setBackgroundResource(R.drawable.cell_shape);

        p4 = new TextView(getActivity());
        p4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p4.setText("");
        p4.setTextColor(Color.BLACK);
        p4.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p4.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id4);
        row.addView(a4);
        row.addView(r4);
        row.addView(f4);
        row.addView(fr4);
        row.addView(s4);
        row.addView(p4);
        eventTable.addView(row);

        id5 = new TextView(getActivity());
        id5.setText("");
        id5.setTextColor(Color.BLACK);
        id5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id5.setBackgroundResource(R.drawable.cell_shape);

        a5 = new TextView(getActivity());
        a5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a5.setText("");
        a5.setTextColor(Color.BLACK);
        a5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a5.setBackgroundResource(R.drawable.cell_shape);

        r5 = new TextView(getActivity());
        r5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r5.setText("");
        r5.setTextColor(Color.BLACK);
        r5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r5.setBackgroundResource(R.drawable.cell_shape);

        f5 = new TextView(getActivity());
        f5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f5.setText("");
        f5.setTextColor(Color.BLACK);
        f5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f5.setBackgroundResource(R.drawable.cell_shape);

        fr5 = new TextView(getActivity());
        fr5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr5.setText("");
        fr5.setTextColor(Color.BLACK);
        fr5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr5.setBackgroundResource(R.drawable.cell_shape);

        s5 = new TextView(getActivity());
        s5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s5.setText("");
        s5.setTextColor(Color.BLACK);
        s5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s5.setBackgroundResource(R.drawable.cell_shape);

        p5 = new TextView(getActivity());
        p5.setText("");
        p5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p5.setTextColor(Color.BLACK);
        p5.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p5.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id5);
        row.addView(a5);
        row.addView(r5);
        row.addView(f5);
        row.addView(fr5);
        row.addView(s5);
        row.addView(p5);
        eventTable.addView(row);

        id6 = new TextView(getActivity());
        id6.setText("");
        id6.setTextColor(Color.BLACK);
        id6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id6.setBackgroundResource(R.drawable.cell_shape);

        a6 = new TextView(getActivity());
        a6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a6.setText("");
        a6.setTextColor(Color.BLACK);
        a6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a6.setBackgroundResource(R.drawable.cell_shape);

        r6 = new TextView(getActivity());
        r6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r6.setText("");
        r6.setTextColor(Color.BLACK);
        r6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r6.setBackgroundResource(R.drawable.cell_shape);

        f6 = new TextView(getActivity());
        f6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f6.setText("");
        f6.setTextColor(Color.BLACK);
        f6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f6.setBackgroundResource(R.drawable.cell_shape);

        fr6 = new TextView(getActivity());
        fr6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr6.setText("");
        fr6.setTextColor(Color.BLACK);
        fr6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr6.setBackgroundResource(R.drawable.cell_shape);

        s6 = new TextView(getActivity());
        s6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s6.setText("");
        s6.setTextColor(Color.BLACK);
        s6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s6.setBackgroundResource(R.drawable.cell_shape);

        p6 = new TextView(getActivity());
        p6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p6.setText("");
        p6.setTextColor(Color.BLACK);
        p6.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p6.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id6);
        row.addView(a6);
        row.addView(r6);
        row.addView(f6);
        row.addView(fr6);
        row.addView(s6);
        row.addView(p6);
        eventTable.addView(row);

        id7 = new TextView(getActivity());
        id7.setText("");
        id7.setTextColor(Color.BLACK);
        id7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id7.setBackgroundResource(R.drawable.cell_shape);

        a7 = new TextView(getActivity());
        a7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a7.setText("");
        a7.setTextColor(Color.BLACK);
        a7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a7.setBackgroundResource(R.drawable.cell_shape);

        r7 = new TextView(getActivity());
        r7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r7.setText("");
        r7.setTextColor(Color.BLACK);
        r7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r7.setBackgroundResource(R.drawable.cell_shape);

        f7 = new TextView(getActivity());
        f7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f7.setText("");
        f7.setTextColor(Color.BLACK);
        f7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f7.setBackgroundResource(R.drawable.cell_shape);

        fr7 = new TextView(getActivity());
        fr7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr7.setText("");
        fr7.setTextColor(Color.BLACK);
        fr7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr7.setBackgroundResource(R.drawable.cell_shape);

        s7 = new TextView(getActivity());
        s7.setText("");
        s7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s7.setTextColor(Color.BLACK);
        s7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s7.setBackgroundResource(R.drawable.cell_shape);

        p7 = new TextView(getActivity());
        p7.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p7.setText("");
        p7.setTextColor(Color.BLACK);
        p7.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p7.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id7);
        row.addView(a7);
        row.addView(r7);
        row.addView(f7);
        row.addView(fr7);
        row.addView(s7);
        row.addView(p7);
        eventTable.addView(row);

        id8 = new TextView(getActivity());
        id8.setText("");
        id8.setTextColor(Color.BLACK);
        id8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id8.setBackgroundResource(R.drawable.cell_shape);

        a8 = new TextView(getActivity());
        a8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a8.setText("");
        a8.setTextColor(Color.BLACK);
        a8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a8.setBackgroundResource(R.drawable.cell_shape);

        r8 = new TextView(getActivity());
        r8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r8.setText("");
        r8.setTextColor(Color.BLACK);
        r8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r8.setBackgroundResource(R.drawable.cell_shape);

        f8 = new TextView(getActivity());
        f8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f8.setText("");
        f8.setTextColor(Color.BLACK);
        f8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f8.setBackgroundResource(R.drawable.cell_shape);

        fr8 = new TextView(getActivity());
        fr8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr8.setText("");
        fr8.setTextColor(Color.BLACK);
        fr8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr8.setBackgroundResource(R.drawable.cell_shape);

        s8 = new TextView(getActivity());
        s8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s8.setText("");
        s8.setTextColor(Color.BLACK);
        s8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s8.setBackgroundResource(R.drawable.cell_shape);

        p8 = new TextView(getActivity());
        p8.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p8.setText("");
        p8.setTextColor(Color.BLACK);
        p8.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p8.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id8);
        row.addView(a8);
        row.addView(r8);
        row.addView(f8);
        row.addView(fr8);
        row.addView(s8);
        row.addView(p8);
        eventTable.addView(row);

        id9 = new TextView(getActivity());
        id9.setText("");
        id9.setTextColor(Color.BLACK);
        id9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.49), LinearLayout.LayoutParams.WRAP_CONTENT));
        id9.setBackgroundResource(R.drawable.cell_shape);

        a9 = new TextView(getActivity());
        a9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        a9.setText("");
        a9.setTextColor(Color.BLACK);
        a9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        a9.setBackgroundResource(R.drawable.cell_shape);

        r9 = new TextView(getActivity());
        r9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        r9.setText("");
        r9.setTextColor(Color.BLACK);
        r9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        r9.setBackgroundResource(R.drawable.cell_shape);

        f9 = new TextView(getActivity());
        f9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        f9.setText("");
        f9.setTextColor(Color.BLACK);
        f9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        f9.setBackgroundResource(R.drawable.cell_shape);

        fr9 = new TextView(getActivity());
        fr9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        fr9.setText("");
        fr9.setTextColor(Color.BLACK);
        fr9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        fr9.setBackgroundResource(R.drawable.cell_shape);

        s9 = new TextView(getActivity());
        s9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        s9.setText("");
        s9.setTextColor(Color.BLACK);
        s9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.085), LinearLayout.LayoutParams.WRAP_CONTENT));
        s9.setBackgroundResource(R.drawable.cell_shape);

        p9 = new TextView(getActivity());
        p9.setText("");
        p9.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        p9.setTextColor(Color.BLACK);
        p9.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.09), LinearLayout.LayoutParams.WRAP_CONTENT));
        p9.setBackgroundResource(R.drawable.cell_shape);

        row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(id9);
        row.addView(a9);
        row.addView(r9);
        row.addView(f9);
        row.addView(fr9);
        row.addView(s9);
        row.addView(p9);
        eventTable.addView(row);
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



