package com.mohamed.mario.worker.viewMA;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMaSplashBinding;
import com.mohamed.mario.worker.view.LoginActivity;
import com.mohamed.mario.worker.view.WorkerMainActivity;
import com.mohamed.mario.worker.viewModelFactory.MASplashActivityViewModelFactory;
import com.mohamed.mario.worker.viewModelMa.MASplashActivityViewModel;

/**
 * Created by Mohamed on 8/27/2018.
 *
 * Notes
 * 1- to survive same animation, even on orientation changes -> you can make use of
 *      viewModel surviving mechanism + {@link #onSaveInstanceState(Bundle, PersistableBundle)}
 *      + some calculations in viewModel, but since it's a lot of work with no importance
 *      it's better to fix this activity's orientation to ( Sensor Portrait in manifest )
 */
public class MASplashActivity extends AppCompatActivity implements MASplashActivityViewModel.Listener {

    // --- Private Variables

    private ActivityMaSplashBinding binding;

    private MASplashActivityViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ma_splash);

        // init viewModel
        MASplashActivityViewModelFactory factory = new MASplashActivityViewModelFactory(
                getApplication(), this);
        viewModel = ViewModelProviders.of(this, factory)
                .get(MASplashActivityViewModel.class);

        // observe live data inside view model
        observeViewModelLiveData();

        // start initial animations
        startInitialAnimations();
    }

    //region Inside onCreate()

    private void observeViewModelLiveData(){
        // -- Ensure anim is complete and action is ready.
        viewModel.mchRunnableAndBooleanMediatorLiveData.observe(this, mchRunnableAndBoolean -> {
            if (mchRunnableAndBoolean == null){
                return;
            }

            Runnable runnable = mchRunnableAndBoolean.getRunnable();
            boolean makeAction = mchRunnableAndBoolean.isVarBoolean()
                    && runnable != null;

            if (makeAction){
                runnable.run();
            }
        });
    }

    /** Since textView is intended to be easily to be read by users so it's better not to anim it */
    private void startInitialAnimations() {
        // -- ImageView anim
        Animation anim = viewModel.getImageAnimation();
        binding.imageViewSplashLogo.setAnimation(anim);

        anim.start();
    }

    //endregion


    //region View Model Listener Implementation Methods

    @Override
    public void startLoginActivity() {
        Intent intent = new Intent(this, MALoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void startLoginActivityWithData(String type, String phone, String password) {
        Intent intent = new Intent(this, MALoginActivity.class);
        intent.putExtra(MALoginActivity.GET_TYPE_FROM_EXTRA,type);
        intent.putExtra(MALoginActivity.GET_PHONE_FROM_EXTRA,phone);
        intent.putExtra(MALoginActivity.GET_PASSWORD_FROM_EXTRA,password);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void startHomeActivityForUserOrWorker(int type) {
        if (type == MASplashActivityViewModel.WORKER_TYPE) {
            // -- Worker
            Intent intent = new Intent(this, WorkerMainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        } else if (type == MASplashActivityViewModel.USER_TYPE) {
            // todo make UserMainActivity and launch it isa.
            // -- User
        }
    }

    //endregion

}
