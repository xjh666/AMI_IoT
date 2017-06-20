package com.example.foremanproject.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foremanproject.R;
import com.example.foremanproject.other.Configuration;

/**
 * Created by Xie Jihui on 6/20/2017.
 */

public class HostDetail extends AppCompatActivity {
    private static String name;
    private static String status;
    private static String configuration;
    private static String mac;
    private static String ip;
    private static String puppetEnvironment;
    private static String hostArchitecture;
    private static String os;
    private static String owner;
    private static String hostgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(name);
        setProperties();
    }

    @Override
    public void onBackPressed() { finish();}

    private void setProperties(){
        LinearLayout list = (LinearLayout) findViewById(R.id.list);
        LinearLayout layout;
        TextView text;
        ImageView image;

        text = new TextView(this);
        text.setText(" Properties");
        text.setTextColor(Color.BLACK);
        text.setTextSize(21);
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout = new LinearLayout(this);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Status");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        image = new ImageView(this);
        if(status.equals("OK"))
            image.setImageResource(R.drawable.ok_icon);
        else image.setImageResource(R.drawable.exclamation_icon);
        image.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.1), LinearLayout.LayoutParams.MATCH_PARENT));
        image.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(image);
        text = new TextView(this);
        text.setText(" " + status);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.4), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Configuration");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        image = new ImageView(this);
        if(status.equals("OK"))
            image.setImageResource(R.drawable.ok_icon);
        else image.setImageResource(R.drawable.exclamation_icon);
        image.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.1), LinearLayout.LayoutParams.MATCH_PARENT));
        image.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(image);
        text = new TextView(this);
        text.setText(" " + configuration);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" IP Address");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + ip);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" MAC Address");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + mac);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Puppet Environment");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + puppetEnvironment);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Host Architecture");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + hostArchitecture);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Operating System");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + os);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Host group");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + hostgroup);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        layout = new LinearLayout(this);
        text = new TextView(this);
        text.setText(" Owner");
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        text = new TextView(this);
        text.setText(" " + owner);
        text.setTextColor(Color.BLACK);
        text.setTextSize(18);
        text.setLayoutParams(new LinearLayout.LayoutParams((int)(Configuration.getWidth()* 0.5), LinearLayout.LayoutParams.WRAP_CONTENT));
        text.setBackgroundResource(R.drawable.cell_shape);
        layout.addView(text);
        list.addView(layout);

        text = new TextView(this);
        text.setText("");
        text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(Configuration.getHeight()* 0.1)));
        list.addView(text); // space
    }

    public static void setInfo(String _status, String _configuration, String _ip, String _mac,
                               String _puppetEnvironment, String _hostArchitecture, String _os,
                               String _owner, String _hostgroup, String _name)
    {
        status = _status;
        configuration = _configuration;
        ip = _ip;
        mac = _mac;
        puppetEnvironment = _puppetEnvironment;
        hostArchitecture = _hostArchitecture;
        os = _os;
        owner = _owner;
        hostgroup =_hostgroup;
        name = _name;
    }
}
