package com.example.foremanproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginInterface extends AppCompatActivity {

    Button button;
    EditText domainEdit;
    EditText userNameEdit;
    EditText passwordEdit;

    private static String domain;
    private static String username;
    private static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Welcome to Foreman");
        setContentView(R.layout.activity_login_interface);

        button = (Button)findViewById(R.id.button);
        domainEdit   = (EditText)findViewById(R.id.DOMAIN);
        userNameEdit   = (EditText)findViewById(R.id.USERNAME);
        passwordEdit   = (EditText)findViewById(R.id.PASSWORD);
    }

    public void Login(View view){
        domain = domainEdit.getText().toString();
        username = userNameEdit.getText().toString();
        password = passwordEdit.getText().toString();
        Intent intent = new Intent(this, MainInterface.class);
        try{
            if((domain.equals("172.29.32.41")||domain.equals("https://172.29.32.41"))
                    &&(username.equals("admin"))
                    &&(password.equals("012278"))) {
                Toast.makeText(LoginInterface.this, "Successful", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }

            else if(domain.equals(""))
                Toast.makeText(LoginInterface.this, "Please Enter Domain", Toast.LENGTH_LONG).show();

            else if(username.equals(""))
                Toast.makeText(LoginInterface.this, "Please Enter Username", Toast.LENGTH_LONG).show();

            else if(password.equals(""))
                Toast.makeText(LoginInterface.this, "Please Enter Password", Toast.LENGTH_LONG).show();

            else Toast.makeText(LoginInterface.this, "Incorrect Domain/Username/Password", Toast.LENGTH_LONG).show();
        }catch(Exception e){
            System.out.println(1);
        }
    }

    public static String getDomain(){
        return domain;
    }

    public static String getUserName(){
        return username;
    }

    public static String getPassword(){
        return password;
    }
}
