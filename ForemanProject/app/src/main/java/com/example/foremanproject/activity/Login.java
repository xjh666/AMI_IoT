package com.example.foremanproject.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.foremanproject.R;
import com.example.foremanproject.other.Configuration;
import com.example.foremanproject.other.NukeSSLCerts;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/16/2017.
 */

/**
 * This class is an activity for the Login Page
 * When the user click "Login" button, use the url, username and password to send the a request.
 * If getting response correctly, then treat login successfully and save the information in Configuration Class
 * and open the MonitorPage page
 */

public class Login extends AppCompatActivity {

    Button button;
    EditText urlEdit;
    EditText userNameEdit;
    EditText passwordEdit;

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NukeSSLCerts.nuke();
        setTitle("Welcome to Foreman");
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Configuration.setWidth(dm.widthPixels);
        Configuration.setHeight(dm.heightPixels);

        button = (Button)findViewById(R.id.button);
        urlEdit   = (EditText)findViewById(R.id.URL);
        userNameEdit   = (EditText)findViewById(R.id.USERNAME);
        passwordEdit   = (EditText)findViewById(R.id.PASSWORD);
    }

    public void LogIn(View view) throws AuthFailureError {
        Toast.makeText(Login.this, "Logging in...", Toast.LENGTH_LONG).show();

        String url = urlEdit.getText().toString();
        final Intent intent = new Intent(this, MonitorPage.class);

        if(url.equals(""))
            Toast.makeText(Login.this, "Please Enter url", Toast.LENGTH_LONG).show();

        else if(userNameEdit.getText().toString().equals(""))
            Toast.makeText(Login.this, "Please Enter Username", Toast.LENGTH_LONG).show();

        else if(passwordEdit.getText().toString().equals(""))
            Toast.makeText(Login.this, "Please Enter Password", Toast.LENGTH_LONG).show();

        else {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            if(!url.substring(0,4).equals("https"))
                url = "https://" + url;
            if(!url.substring(url.length()-1).equals("/"))
                url = url + "/";

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url + "api/architectures", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(Login.this, "Successful", Toast.LENGTH_LONG).show();
                            Configuration.setUrl(urlEdit.getText().toString());
                            Configuration.setUsername(userNameEdit.getText().toString());
                            Configuration.setPassword(passwordEdit.getText().toString());
                            startActivity(intent);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Login.this, "Incorrect url/Username/Password", Toast.LENGTH_LONG).show();
                        }
                    }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> headers = new HashMap<>();
                        // add headers <key,value>
                        String auth = Base64.encodeToString((userNameEdit.getText().toString() + ":" + passwordEdit.getText().toString()).getBytes(),Base64.NO_WRAP);
                        headers.put("Authorization", "Basic "+ auth);

                        return headers;
                    }
            };
            // Add the request to the RequestQueue.
            queue.add(jsObjRequest);
        }
    }
}
