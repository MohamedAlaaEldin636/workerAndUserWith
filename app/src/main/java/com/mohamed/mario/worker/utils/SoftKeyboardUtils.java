package com.mohamed.mario.worker.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Mohamed on 8/31/2018.
 *
 */
public class SoftKeyboardUtils {

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (imm == null){
            return;
        }

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
