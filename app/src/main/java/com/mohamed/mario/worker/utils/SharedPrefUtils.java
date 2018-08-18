package com.mohamed.mario.worker.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

//region Login method


    // --- All sharedPref Constants

    private static final String FILE_GENERAL = "FILE_GENERAL";
    private static final String KEY_LOGIN = "KEY_LOGIN";
    public static final String VALUE_KEY_LOGIN = "NONE";
    public static final String VALUE_KEY_WORKER = "Worker";
    public static final String VALUE_KEY_USER = "User";

    public static String getLoginData(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LOGIN, VALUE_KEY_LOGIN);
    }

    public static void setLoginData(Context context, String string) {
        SharedPreferences sharedPref = context.getSharedPreferences(FILE_GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(KEY_LOGIN, string);

        editor.apply();
    }
//endregion

}
