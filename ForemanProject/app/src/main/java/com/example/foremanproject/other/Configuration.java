package com.example.foremanproject.other;

/**
 * Created by Xie Jihui on 5/17/2017.
 */

/**
 * This class is to store the info of user, which is user for requests in other activities.
 */

public class Configuration {
    private static String url;
    private static String username;
    private static String password;

    private static int width;
    private static int height;

    public static void setUrl(String newUrl) {
        url = newUrl;
        if(!url.substring(0,4).equals("https"))
            url = "https://" + url;
        if(!url.substring(url.length()-1).equals("/"))
            url = url + "/";
    }

    public static void setUsername(String newUsername) { username = newUsername; }

    public static void setPassword(String newPassword) { password = newPassword; }

    public static String getUrl(){ return url;}

    public static String getUNandPW(){ return (username + ":" + password);}

    public static void setWidth(int _width){ width = _width; }

    public static void setHeight(int _height){ height = _height; }

    public static int getWidth(){ return width;}

    public static int getHeight(){ return height;}
}
