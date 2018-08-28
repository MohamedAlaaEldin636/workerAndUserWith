package com.mohamed.mario.worker.viewModelMa;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MALoginActivityViewModel extends AndroidViewModel {

    // --- Constants

    private static final int USER_TYPE = 1;
    private static final int WORKER_TYPE = 0;

    // --- Private Methods

    private Listener listener;

    // ---- Constructor

    public MALoginActivityViewModel(@NonNull Application application, Listener listener) {
        super(application);

        this.listener = listener;
    }

    // ---- Public Methods

    public void checkLoginCredentials(String phone, String password, String type) {
        // -- Show loading and disable clickable views
        listener.makeUIChangesBasedOnLoadingData(true);

        Context appContext = getApplication().getApplicationContext();

        if (NetworkUtils.isCurrentlyOnline(appContext)){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(type);
            if (type.equals(USERES_DATABASE_NAME)) {
                Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(
                        new CustomValueEventListenerImplementation(true, password));
            }else if (type.equals(WORKER_DATABASE_NAME)) {
                Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(
                        new CustomValueEventListenerImplementation(false, password));
            }
        }else {
            ((BaseApplication) appContext)
                    .showToast(appContext.getString(R.string.No_Internet_connection));

            listener.makeUIChangesBasedOnLoadingData(false);
        }
    }

    // ----- Helper Inner Classes

    private class CustomValueEventListenerImplementation implements ValueEventListener {

        private boolean isUser;
        private String password;

        CustomValueEventListenerImplementation(boolean isUser, String password) {
            this.isUser = isUser;
            this.password = password;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // -- change ui back to default state by hiding loading and enabling buttons
            listener.makeUIChangesBasedOnLoadingData(false);

            Context appContext = getApplication().getApplicationContext();
            BaseApplication baseApplication = ((BaseApplication) appContext);

            if (isUser){
                List<User> usersList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    usersList.add(singleSnapshot.getValue(User.class));
                }
                if (usersList.size() == 0) {
                    baseApplication.showToast(appContext.getString(R.string.phonedontexsist));
                }else {
                    if (usersList.get(0).getPassword().equals(password)) {
                        baseApplication.showToast(appContext.getString(R.string.successlogin));
                        listener.performLogin(USER_TYPE);
                    }else {
                        baseApplication.showToast(appContext.getString(R.string.passwordwrong));
                    }
                }
            }else {
                List<Worker> workersList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    workersList.add(singleSnapshot.getValue(Worker.class));
                }
                if (workersList.size() == 0) {
                    baseApplication.showToast(appContext.getString(R.string.phonedontexsist));
                }else {
                    if (workersList.get(0).getPassword().equals(password)) {
                        baseApplication.showToast(appContext.getString(R.string.successlogin));
                        listener.performLogin(WORKER_TYPE);
                    }else {
                        baseApplication.showToast(appContext.getString(R.string.passwordwrong));
                    }
                }
            }
        }

        /**
         * This method will be triggered in the event that this listener either failed at the
         *      server, or is removed as a result of the security and Firebase Database rules.
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.v("isUser == " + isUser + ", onCancelled" + databaseError.toException());

            listener.makeUIChangesBasedOnLoadingData(false);

            Context appContext = getApplication().getApplicationContext();
            ((BaseApplication) appContext).showToast(
                    appContext.getString(R.string.try_again_and_check_internet_connection));
        }
    }

    // ----- View Model Listener interface

    public interface Listener {

        /** Type if 0 -> Worker, if 1  -> User */
        void performLogin(int type);

        void makeUIChangesBasedOnLoadingData(boolean isLoading);

    }
}
