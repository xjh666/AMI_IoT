package com.example.foremanproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainInterface extends AppCompatActivity {
    private String domain;
    private String username;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.main_interface);

        domain = LoginInterface.getDomain();
        username = LoginInterface.getUserName();
        password = LoginInterface.getPassword();
    }
        public void showAllHosts(View view){

        }
}
