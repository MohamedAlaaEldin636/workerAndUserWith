package com.mohamed.mario.worker.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivitySplashBinding;
import com.mohamed.mario.worker.viewmodel.SplashActivityViewModel;

public class SplashActivity extends AppCompatActivity implements SplashActivityViewModel.Listener {

    //private Binding
    private ActivitySplashBinding binding;
    private SplashActivityViewModel viewModel;

    //Toast
    Toast msToast;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        viewModel = ViewModelProviders.of(this).get(SplashActivityViewModel.class);
        //initviewModel
        viewModel.initSetup(this);
        binding.setViewModel(viewModel);
        ImageView animated_iamge = findViewById(R.id.imageViewSplash_logo);
        TextView animated_text = findViewById(R.id.text_explian_Splash);
        viewModel.startAnimation(animated_iamge, animated_text);
    }


    @Override
    public void startLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void LogIn(boolean IsLogin, String type,String phone,String password) {

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(LoginActivity.GET_TRPE_FROM_EXTRA,type);
            intent.putExtra(LoginActivity.GET_PHONE_FROM_EXTRA,phone);
            intent.putExtra(LoginActivity.GET_PASSWORD_FROM_EXTRA,password);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();



    }

    @Override
    public void LogIn(boolean IsLogin, int type) {
        if (IsLogin) {
            if (type == SplashActivityViewModel.WOKRER_TYPE) {
                //Worker
                Intent intent = new Intent(this, WorkerMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else if (type == SplashActivityViewModel.USER_TYPE) {
                //User
            }

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }
    }

    ///For Preparing Toast
    @Override
    public void showToast(String text) {
        if (msToast != null) {
            msToast.cancel();
        }
        msToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        msToast.show();

    }
    //endregion

}
