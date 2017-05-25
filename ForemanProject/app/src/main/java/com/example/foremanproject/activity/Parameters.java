package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.foremanproject.R;

/**
 * Created by Xie Jihui on 5/25/2017.
 */

public class Parameters extends AppCompatActivity {
    private static int id;
    private static String title;
    private static String type;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters);
        setTitle(title);
    }

    public void CloseActivity(View v){
        finish();
    }

    public static void setID(int newid){
        id = newid;
    }

    public static void setType(String newtype){
        type = newtype;
    }

    public static void setPageTitle(String pagetitle){
        title = pagetitle;
    }
}
