package com.mohamed.mario.worker.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.mohamed.mario.worker.utils.SharedPrefUtils;

public class MainActivityViewModel extends AndroidViewModel {


    private Listener listener;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void initSetup(Listener listener) {
        this.listener = listener;

        // initial checks
        // -- check if previous user or worker
        checkIfPreviouslyRegistered();
    }

    private void checkIfPreviouslyRegistered() {
        String loginInfo = SharedPrefUtils.getLoginData(getApplication().getApplicationContext());

        if (loginInfo.equals(SharedPrefUtils.VALUE_KEY_USER)) {
            listener.launchActivity(true);
        } else if (loginInfo.equals(SharedPrefUtils.VALUE_KEY_WORKER)){
            listener.launchActivity(false);
        } // Else do nothing.
    }


    public interface Listener {
        void launchActivity(boolean isUser);
    }

}
