package com.example.foremanproject.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.foremanproject.R;

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

    }

    public void setInfo(String _name, int _id){
        name = _name;
        id = _id;
    }
}
