package com.example.foremanproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainInterface extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.main_interface);
    }
    public void showAllHosts(View view){

    }

    public void showHostGroups(View view){

    }

    public void showHostConfigurationStatus(View view){

    }

    public void showHostConfigurationChart(View view){
        startActivity(new Intent(this, HostConfigurationChart.class));
    }

    public void showRunDistribution(View view){

    }

    public void showLatestEvents(View view){

    }
}
