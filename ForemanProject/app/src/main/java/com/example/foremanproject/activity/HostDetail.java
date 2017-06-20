package com.example.foremanproject.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.foremanproject.R;

/**
 * Created by Xie Jihui on 6/20/2017.
 */

public class HostDetail extends AppCompatActivity {
    private static String mac;
    private static String ip;
    private static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(name);
        showInfo();
    }

    @Override
    public void onBackPressed() { finish();}

    private void showInfo(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.list);

        TextView ipText = new TextView(this);
        ipText.setText("  IP Address: " + ip);
        ipText.setTextSize(21);
        ipText.setTextColor(Color.BLACK);
        TextView macText = new TextView(this);
        macText.setText("  MAC Address: " + mac);
        macText.setTextSize(21);
        macText.setTextColor(Color.BLACK);

        layout.addView(ipText);
        layout.addView(macText);

        TableLayout table = new TableLayout(this);
        layout.addView(table);
    }

    public static void setInfo(String _ip, String _mac, String _name){
        ip = _ip;
        mac = _mac;
        name = _name;
    }
}
