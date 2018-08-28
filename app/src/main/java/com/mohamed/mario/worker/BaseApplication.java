package com.mohamed.mario.worker;

import android.app.Application;
import android.widget.Toast;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class BaseApplication extends Application {

    private Toast toast;

    @Override
    public void onCreate() {
        super.onCreate();

        // Planting timber tree
        Timber.plant(new Timber.DebugTree());
    }

    // --- Public Methods

    public void showToast(String msg){
        if (toast != null){
            toast.cancel();
        }

        toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }

}
