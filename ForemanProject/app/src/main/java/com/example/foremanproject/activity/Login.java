package com.example.foremanproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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
import com.example.foremanproject.other.NukeSSLCerts;
import com.example.foremanproject.R;
import com.example.foremanproject.other.UserInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Xie Jihui on 5/16/2017.
 */

/**
 * This class is an activity for the Login Page
 * When the user click "Login" button, use the url, username and password to send the a request.
 * If getting response correctly, then treat login successfully and save the information in UserInfo Class
 * and open the BasicActivity page
 */

public class Login extends AppCompatActivity {

    Button button;
    EditText urlEdit;
    EditText userNameEdit;
    EditText passwordEdit;

    @Override
    public void onBackPressed() { finish(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NukeSSLCerts.nuke();
        setTitle("Welcome to Foreman");
        setContentView(R.layout.activity_login);

        button = (Button)findViewById(R.id.button);
        urlEdit   = (EditText)findViewById(R.id.URL);
        userNameEdit   = (EditText)findViewById(R.id.USERNAME);
        passwordEdit   = (EditText)findViewById(R.id.PASSWORD);
    }

    public void LogIn(View view) throws AuthFailureError {
        String url = urlEdit.getText().toString();
        final Intent intent = new Intent(this, BasicActivity.class);

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
                            UserInfo.setUrl(urlEdit.getText().toString());
                            UserInfo.setUsername(userNameEdit.getText().toString());
                            UserInfo.setPassword(passwordEdit.getText().toString());
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
