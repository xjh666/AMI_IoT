package com.example.foremanproject;

/**
 * Created by labuser on 5/17/2017.
 */

class SendReqeust {
    private static String url;
    private static String username;
    private static String password;

    static void setUrl(String newUrl) {
        url = newUrl;
        if(!url.substring(0,4).equals("https"))
            url = "https://" + url;
        if(!url.substring(url.length()-1).equals("/"))
            url = url + "/";
    }

    static void setUsername(String newUsername) { username = newUsername; }

    static void setPassword(String newPassword) { password = newPassword; }
}
