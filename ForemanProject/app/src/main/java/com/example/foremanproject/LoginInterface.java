package com.example.foremanproject;

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

    public static String domain;
    public static String userName;
    public static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_interface);

        button = (Button)findViewById(R.id.button);
        domainEdit   = (EditText)findViewById(R.id.DOMAIN);
        userNameEdit   = (EditText)findViewById(R.id.USERNAME);
        passwordEdit   = (EditText)findViewById(R.id.PASSWORD);

        //when click LOGIN
        button.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        domain = domainEdit.getText().toString();
                        userName = userNameEdit.getText().toString();
                        password = passwordEdit.getText().toString();

                        try{
                            if((domain.equals("172.29.32.41")||domain.equals("https://172.29.32.41"))
                                    &&(userName.equals("admin"))
                                    &&(password.equals("012278")))
                                Toast.makeText(LoginInterface.this, "Successful", Toast.LENGTH_LONG).show();

                            else if(domain.equals(""))
                                Toast.makeText(LoginInterface.this, "Please Enter Domain", Toast.LENGTH_LONG).show();

                            else if(userName.equals(""))
                                Toast.makeText(LoginInterface.this, "Please Enter Username", Toast.LENGTH_LONG).show();

                            else if(password.equals(""))
                                Toast.makeText(LoginInterface.this, "Please Enter Password", Toast.LENGTH_LONG).show();

                            else Toast.makeText(LoginInterface.this, "Incorrect Domain/Username/Password", Toast.LENGTH_LONG).show();


                        }catch(Exception e){
                            System.out.println(1);
                        }
                    }
                });
    }
}
