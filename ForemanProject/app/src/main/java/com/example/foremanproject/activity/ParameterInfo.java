package com.example.foremanproject.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foremanproject.R;

/**
 * Created by Xie Jihui on 6/5/2017.
 */

public class ParameterInfo extends AppCompatActivity {
    private static String description;
    private static String type;
    private static String matcher;
    private static Object value;
    private static String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameter_info);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.3));

        TextView text;
        text = (TextView) findViewById(R.id.description);
        text.setText(description);
        text = (TextView) findViewById(R.id.type);
        text.setText(type);
        text = (TextView) findViewById(R.id.matcher);
        text.setText(matcher);
        text = (TextView) findViewById(R.id.value);
        if(value != null)
            text.setText(value.toString());
        if(!tag.equals("PuppetDefault"))
        {
            LinearLayout layout;
            layout = (LinearLayout) findViewById(R.id.PuppetInfo1);
            layout.setVisibility(View.INVISIBLE);
            layout = (LinearLayout) findViewById(R.id.PuppetInfo2);
            layout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() { finish();}

    public static void setInfo(String _description, String _type, String _matcher, Object _value, String _tag){
        description = _description;
        type = _type;
        matcher = _matcher;
        value = _value;
        tag = _tag;
    }
}
