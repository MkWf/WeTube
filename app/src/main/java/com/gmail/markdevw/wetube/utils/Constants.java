package com.gmail.markdevw.wetube.utils;

/**
 * Created by Mark on 3/24/2016.
 */
public final class Constants {

    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_FRIENDS = "friends";
    public static final String FIREBASE_LOCATION_NOTIFICATIONS = "notifications";

    public static final String FIREBASE_URL = "https://wetube-mkwf.firebaseio.com";
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_URL_FRIENDS = FIREBASE_URL + "/" + FIREBASE_LOCATION_FRIENDS;
    public static final String FIREBASE_URL_NOTIFICATIONS = FIREBASE_URL + "/" + FIREBASE_LOCATION_NOTIFICATIONS;

    public static final String GOOGLE_PROVIDER = "google";
    public static final String PASSWORD_PROVIDER = "password";

    public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";

}
