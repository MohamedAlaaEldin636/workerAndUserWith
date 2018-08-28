package com.mohamed.mario.worker.viewMA;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMaLoginBinding;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;
import com.mohamed.mario.worker.view.WorkerMainActivity;
import com.mohamed.mario.worker.viewModelFactory.MALoginActivityViewModelFactory;
import com.mohamed.mario.worker.viewModelMa.MALoginActivityViewModel;
import com.mohamed.mario.worker.viewmodel.LoginActivityViewModel;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MALoginActivity extends AppCompatActivity implements MALoginActivityViewModel.Listener {

    // --- Constants

    public static final String GET_PHONE_FROM_EXTRA = "EXTRA_PHONE";
    public static final String GET_PASSWORD_FROM_EXTRA = "EXTRA_PASSWORD";
    public static final String GET_TYPE_FROM_EXTRA = "EXTRA_TYPE";

    // --- Private Variables

    private ActivityMaLoginBinding binding;

    private MALoginActivityViewModel viewModel;

    /** for user or worker type */
    private int type = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ma_login);

        // -- init view model
        MALoginActivityViewModelFactory factory = new MALoginActivityViewModelFactory(
                getApplication(), this);
        viewModel = ViewModelProviders.of(this, factory).get(MALoginActivityViewModel.class);

        // -- initial setups for UI views
        xmlViewsSetups();
    }

    //region Inside onCreate()
    private void xmlViewsSetups() {
        //region Spinner Listener
        binding.spinnerSelectorLogin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //endregion

        if (getIntent().getExtras() != null) {
            binding.loginPhone.setText(getIntent().getStringExtra(GET_PHONE_FROM_EXTRA));
            binding.loginPassword.setText(getIntent().getStringExtra(GET_PASSWORD_FROM_EXTRA));
            String typeOFUserWorker = getIntent().getStringExtra(GET_TYPE_FROM_EXTRA);
            binding.checkboxLogin.setChecked(true);
            if (typeOFUserWorker.equals(USERES_DATABASE_NAME)) {
                binding.spinnerSelectorLogin.setSelection(1);
            } else if (typeOFUserWorker.equals(WORKER_DATABASE_NAME)) {
                binding.spinnerSelectorLogin.setSelection(0);
            }
        }
    }
    //endregion

    // ---- Direct Xml Clicks

    public void performLoginIfPossible(View view) {
        Context appContext = getApplicationContext();
        BaseApplication baseApplication = ((BaseApplication) appContext);

        String phone, password, typeOf;

        if (TextUtils.isEmpty(binding.loginPassword.getText().toString())) {
            baseApplication.showToast(getString(R.string.emptyuserprPhone));
        }else if (TextUtils.isEmpty(binding.loginPhone.getText().toString())) {
            baseApplication.showToast(getString(R.string.phonempty));
        }else if (!NetworkUtils.isCurrentlyOnline(this)) {
            baseApplication.showToast(getString(R.string.No_Internet_connection));
        }else {
            phone = binding.loginPhone.getText().toString();
            password = binding.loginPassword.getText().toString();
            if (type == 0) {
                typeOf = WORKER_DATABASE_NAME;
            } else {
                typeOf = USERES_DATABASE_NAME;
            }
            boolean rememberMe = binding.checkboxLogin.isChecked();

            // -- Save login data in sharedPref
            SharedPrefUtils.setLoginData(this, phone, password, typeOf, rememberMe);

            // -- Go to home activity for either worker or user
            viewModel.checkLoginCredentials(phone, password, typeOf);
        }
    }

    public void goToRegistrationActivity(View view) {
        // -- make new registration

        /* Note do not call finish() in case user pressed here by wrong and wants to go back
                this is better then making user starts the app all over again
                and wait for anim again isa */

        Intent intent = new Intent(this, MARegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // ---- MA Login Activity View Model Listener Implementation Methods

    @Override
    public void makeUIChangesBasedOnLoadingData(boolean isLoading) {
        if (isLoading){
            binding.frameLoading.setVisibility(View.VISIBLE);
            binding.buttonLogin.setClickable(false);
        }else {
            binding.frameLoading.setVisibility(View.GONE);
            binding.buttonLogin.setClickable(true);
        }
    }

    @Override
    public void performLogin(int type) {
        if (LoginActivityViewModel.WOKRER_TYPE == type) {
            // -- Launch worker's home activity
            Intent intent = new Intent(this, WorkerMainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }else if (LoginActivityViewModel.USER_TYPE == type) {
            // todo complete home login activity and launch it isa for user.
        }
    }

}