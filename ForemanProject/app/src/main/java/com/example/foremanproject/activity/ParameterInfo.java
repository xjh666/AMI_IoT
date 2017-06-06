package com.example.foremanproject.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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
        getWindow().setLayout((int)(width*0.8), (int)(height*0.35));

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        TextView text;

        text = new TextView(this);
        text.setText("Original value info");
        text.setTextSize(20);
        layout.addView(text);

        if(tag.equals("PuppetDefault"))
        {
            text = new TextView(this);
            text.setText("Optional parameter without value");
            text.setTextColor(Color.BLACK);
            layout.addView(text);

            text = new TextView(this);
            text.setText("Will not be sent to Puppet");
            text.setTextColor(Color.BLACK);
            text.setTypeface(null, Typeface.ITALIC);
            layout.addView(text);

            text = new TextView(this);
            text.setText(" ");
            layout.addView(text);
        };

        text = new TextView(this);
        text.setText("Description: " + description);
        text.setTextColor(Color.BLACK);
        layout.addView(text);

        text = new TextView(this);
        text.setText("Type: " + type);
        text.setTextColor(Color.BLACK);
        layout.addView(text);

        text = new TextView(this);
        text.setText("Matcher: " + matcher);
        text.setTextColor(Color.BLACK);
        layout.addView(text);

        text = new TextView(this);
        if(!value.toString().equals("null"))
            text.setText("Inherited Value: " + value.toString());
        else text.setText("Inherited Value: ");
        text.setTextColor(Color.BLACK);
        layout.addView(text);
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
