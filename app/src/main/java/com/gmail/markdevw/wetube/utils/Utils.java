package com.gmail.markdevw.wetube.utils;

/**
 * Created by Mark on 4/4/2016.
 */
public final class Utils {
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    private Utils(){}
}
