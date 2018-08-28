package com.mohamed.mario.worker.viewModelMa;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.customClasses.MCHRunnableAndBoolean;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/27/2018.
 *
 * Note
 * 1- we can ensure that work done to check for login (background thread) ends before animation
 *      and to do so we can use {@link MediatorLiveData}.
 */
public class MASplashActivityViewModel extends AndroidViewModel {

    // --- Constants

    private static final long ANIMATION_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(2);

    public static final int USER_TYPE = 1;
    public static final int WORKER_TYPE = 0;

    // --- Public Variables

    public final MediatorLiveData<MCHRunnableAndBoolean> mchRunnableAndBooleanMediatorLiveData
            = new MediatorLiveData<>();

    // --- Private Variables

    private Listener listener;

    private final MutableLiveData<Runnable> runnableMutableLiveData = new MediatorLiveData<>();

    private final MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

    private MCHRunnableAndBoolean ensureAnimEndAndActionExistence = new MCHRunnableAndBoolean(
            null, false);

    // --- Constructor

    public MASplashActivityViewModel(@NonNull Application application, Listener listener) {
        super(application);

        this.listener = listener;

        // -- Observing live data changes
        setupLiveDataObjects();

        // -- Start Initial Operations
        checkSharedPrefForLogin();
    }

    /** Inside {@link #MASplashActivityViewModel(Application, Listener)} */
    private void setupLiveDataObjects() {
        // -- Mediator Live Data setups
        mchRunnableAndBooleanMediatorLiveData.addSource(runnableMutableLiveData, runnable -> {
            ensureAnimEndAndActionExistence.setRunnable(runnable);
            mchRunnableAndBooleanMediatorLiveData.setValue(ensureAnimEndAndActionExistence);
        });
        mchRunnableAndBooleanMediatorLiveData.addSource(booleanMutableLiveData, varBoolean -> {
            ensureAnimEndAndActionExistence.setVarBoolean(varBoolean == null ? false : varBoolean);
            mchRunnableAndBooleanMediatorLiveData.setValue(ensureAnimEndAndActionExistence);
        });
    }

    // ---- Public Methods

    public Animation getImageAnimation(){
        Animation anim = AnimationUtils.loadAnimation(getApplication().getApplicationContext(),
                R.anim.scale_splashscreen_anim);
        anim.setDuration(ANIMATION_DURATION_MILLIS);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                booleanMutableLiveData.setValue(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        return anim;
    }

    // ---- Private Methods

    private void checkSharedPrefForLogin() {
        if (SharedPrefUtils.getRemmeberMe(getApplication().getApplicationContext())){
            Context appContext = getApplication().getApplicationContext();

            String phone = SharedPrefUtils.getLoginPhone(appContext);
            String password = SharedPrefUtils.getLoginPassword(appContext);
            String typeOf = SharedPrefUtils.getLoginType(appContext);

            performLogin(phone, password, typeOf);
        }else {
            Runnable runnable = () -> listener.startLoginActivity();
            runnableMutableLiveData.setValue(runnable);
        }
    }

    /** Inside {@link #checkSharedPrefForLogin()} */
    private void performLogin(String phone, String password, String type) {
        Context appContext = getApplication().getApplicationContext();

        if (NetworkUtils.isCurrentlyOnline(appContext)) {
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

            Runnable runnable = () -> listener.startLoginActivityWithData(type, phone, password);
            runnableMutableLiveData.setValue(runnable);
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
            Context appContext = getApplication().getApplicationContext();

            Runnable runnable;

            if (isUser){
                List<User> userList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    userList.add(singleSnapshot.getValue(User.class));
                }
                if (userList.size() == 0) {
                    ((BaseApplication) appContext)
                            .showToast(appContext.getString(R.string.phonedontexsist));

                    runnable = () -> listener.startLoginActivity();
                }else {
                    if (userList.get(0).getPassword().equals(password)) {
                        runnable = () -> listener.startHomeActivityForUserOrWorker(USER_TYPE);
                    }else {
                        ((BaseApplication) appContext)
                                .showToast(appContext.getString(R.string.passwordwrong));

                        runnable = () -> listener.startLoginActivity();
                    }
                }
            }else {
                List<Worker> workerList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    workerList.add(singleSnapshot.getValue(Worker.class));
                }
                if (workerList.size() == 0) {
                    ((BaseApplication) appContext)
                            .showToast(appContext.getString(R.string.phonedontexsist));

                    runnable = () -> listener.startLoginActivity();
                } else {
                    if (workerList.get(0).getPassword().equals(password)) {
                        runnable = () -> listener.startHomeActivityForUserOrWorker(WORKER_TYPE);
                    }else {
                        ((BaseApplication) appContext)
                                .showToast(appContext.getString(R.string.passwordwrong));

                        runnable = () -> listener.startLoginActivity();
                    }
                }
            }

            runnableMutableLiveData.setValue(runnable);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.v("isUser == " + isUser + ", onCancelled" + databaseError.toException());

            Runnable runnable = () -> listener.startLoginActivity();
            runnableMutableLiveData.setValue(runnable);
        }
    }

    // ----- Listener Interface

    public interface Listener {

        /** Otherwise */
        void startLoginActivity();

        /** Used in case of user has previously logged in and didn't choose rememberMe option */
        void startLoginActivityWithData(String type, String phone, String password);

        /** Used in case of user has previously logged in and chosen rememberMe option */
        void startHomeActivityForUserOrWorker(int type);

    }

}