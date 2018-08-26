package com.mohamed.mario.worker.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

public class SplashActivityViewModel extends AndroidViewModel {

    private final int SECOND_FRO_LUNCH = 5;
    long realsecond = SECOND_FRO_LUNCH * 1000;


    Listener listener;

    //Constants
    public static final int USER_TYPE = 1;
    public static final int WOKRER_TYPE = 0;

    public SplashActivityViewModel(@NonNull Application application) {
        super(application);
    }

    public void initSetup(Listener listener) {
        this.listener = listener;
    }

    public void StartAnimation(ImageView animated_Image, TextView animated_text) {

        Animation animS = AnimationUtils.loadAnimation(getApplication().getApplicationContext(), R.anim.scale_splashscreen_anim);
        animS.setDuration(realsecond);
        //    animated_Image.setAnimation(animS);
        //      animated_text.setAnimation(animS);

        //For Animation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CheckPref();
            }
        }, realsecond);
    }


    public void doLogin(String phone, String password, String type) {
        if (NetworkUtils.isCurrentlyOnline(getApplication().getApplicationContext())) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(type);
            if (type.equals(USERES_DATABASE_NAME)) {
                Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If user added no need to add more we will print message
                        List<User> users_list = new ArrayList<>();
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            users_list.add(singleSnapshot.getValue(User.class));
                        }
                        if (users_list.size() == 0) {
                            listener.showToast(getApplication().getApplicationContext().getResources().getString(R.string.phonedontexsist));
                            listener.LogIn(false, 0);

                        } else {
                            if (users_list.get(0).getPassword().equals(password)) {
                                listener.LogIn(true, USER_TYPE);
                            } else {
                                listener.showToast(getApplication().getApplicationContext().getResources().getString(R.string.passwordwrong));
                                listener.LogIn(false, 0);

                            }


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("Mario", "onCancelled", databaseError.toException());
                    }
                });

            } else if (type.equals(WORKER_DATABASE_NAME)) {
                Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone);
                phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If user added no need to add more we will print message
                        List<Worker> users_list = new ArrayList<>();
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            users_list.add(singleSnapshot.getValue(Worker.class));
                        }
                        if (users_list.size() == 0) {
                            listener.showToast(getApplication().getApplicationContext().getResources().getString(R.string.phonedontexsist));
                            listener.LogIn(false, 0);

                        } else {
                            if (users_list.get(0).getPassword().equals(password)) {
                                listener.LogIn(true, WOKRER_TYPE);
                            } else {
                                listener.showToast(getApplication().getApplicationContext().getResources().getString(R.string.passwordwrong));
                                listener.LogIn(false, 0);

                            }


                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {

            listener.showToast(getApplication().getApplicationContext().getResources().getString(R.string.No_Internet_connection));
            listener.LogIn(false, type,phone,password);

        }
    }


    public void CheckPref() {
        if (SharedPrefUtils.getRemmeberMe(getApplication().getApplicationContext()))
            CheckSharedPre();
        else
            listener.startLoginScreen();

    }

    public void CheckSharedPre() {
        String phone, password, typeOf;
        phone = SharedPrefUtils.getLoginPhone(getApplication().getApplicationContext());
        password = SharedPrefUtils.getLoginPassword(getApplication().getApplicationContext());
        typeOf = SharedPrefUtils.getLoginType(getApplication().getApplicationContext());
        doLogin(phone, password, typeOf);
    }


    public interface Listener {
        void startLoginScreen();

        void LogIn(boolean IsLogin, String type,String phone,String password);
        void LogIn(boolean IsLogin, int type);

        void showToast(String msg);
    }


}
