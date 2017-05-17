package com.example.foremanproject;

/**
 * Created by labuser on 5/17/2017.
 */

public class SendReqeust {
    private static String domain;
    private static String username;
    private static String password;

    public static void setDomain(String newDomain){
        domain = newDomain;
    }

    public static void setUsername(String newUsername){
        username = newUsername;
    }

    public static void setPassword(String newPassword){
        password = newPassword;
    }

    public static String getDomain(){
        return domain;
    }

    public static String getUsername(){
        return username;
    }

    public static String getPassword(){
        return password;
    }
}
