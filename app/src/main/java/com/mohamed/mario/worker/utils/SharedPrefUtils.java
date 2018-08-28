package com.mohamed.mario.worker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefUtils {

//region Login method


    // --- All sharedPref Constants
    //pref file name
    private static final String FILE_GENERAL = "FILE_GENERAL";
    //pref keys
    private static final String KEY_LOGIN = "KEY_LOGIN";
    private static final String KEY_LOGIN_PHONE = "PHONE";
    private static final String KEY_LOGIN_PASSWORD = "PASSWORD";
    private static final String KEY_LOGIN_TYPE = "TYPEPFUSER";
    private static final String KEY_LOGIN_REMMBERME = "REMMBERME";
    //pref constant Values
    public static final String VALUE_KEY_LOGIN = "NONE";
    public static final String VALUE_KEY_WORKER = DatabaseUtils.WORKER_DATABASE_NAME;
    public static final String VALUE_KEY_USER = DatabaseUtils.USERES_DATABASE_NAME;

    public static String getLoginData(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LOGIN, VALUE_KEY_LOGIN);
    }

    public static String getLoginPhone(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LOGIN_PHONE, "");
    }
    public static String getLoginPassword(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LOGIN_PASSWORD, "");
    }
    public static String getLoginType(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LOGIN_TYPE, "");
    }

    public static void setLoginData(Context context, String phone,String password,String type, boolean remmberMe) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(KEY_LOGIN_PHONE, phone);
        editor.putString(KEY_LOGIN_PASSWORD, password);
        editor.putString(KEY_LOGIN_TYPE, type);
        editor.putBoolean(KEY_LOGIN_REMMBERME, remmberMe);

        editor.apply();
    }


    public static void setRemmeberMe(Context context, boolean remmberMe) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(KEY_LOGIN_REMMBERME, remmberMe);
        editor.apply();
    }

    public static boolean getRemmeberMe(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        Log.e("AAA",sharedPref.getBoolean(KEY_LOGIN_REMMBERME, false)+"");
        return sharedPref.getBoolean(KEY_LOGIN_REMMBERME, false);
    }

//endregion

}
