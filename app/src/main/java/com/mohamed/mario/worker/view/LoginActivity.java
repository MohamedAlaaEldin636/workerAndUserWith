package com.mohamed.mario.worker.view;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityLoginBinding;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;
import com.mohamed.mario.worker.viewmodel.LoginActivityViewModel;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

public class LoginActivity extends AppCompatActivity implements LoginActivityViewModel.Listener {


    //for Know Type
    int[] type = {0};
    //For Frame Loading
    FrameLayout frame_loading;

    //For Toast
    Toast msToast;
    //button For Login
    Button button_login;

    //For Views
    EditText login_phone, login_password;
    Spinner spinnerSletor_login;
    CheckBox checkbox_login;

    //For ExraData
    public static final String GET_PHONE_FROM_EXTRA = "EXTRA_PHONE";
    public static final String GET_PASSWORD_FROM_EXTRA = "EXTRA_PASSWORD";
    public static final String GET_TRPE_FROM_EXTRA = "EXTRA_TYPE";

    private ActivityLoginBinding binding;
    private LoginActivityViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        viewModel = ViewModelProviders.of(this).get(LoginActivityViewModel.class);
        viewModel.initSetup(this);
        binding.setViewModel(viewModel);
        initViews();


    }

    private void initViews() {
        //For Loading Options
        frame_loading = findViewById(R.id.frame_loading);
        button_login = findViewById(R.id.button_login);
        //For Init Views
        login_phone = findViewById(R.id.login_phone);
        login_password = findViewById(R.id.login_password);
        spinnerSletor_login = findViewById(R.id.spinnerSletor_login);
        checkbox_login = findViewById(R.id.checkbox_login);
        //

        Spinner spinnerSletor_login;
        spinnerSletor_login = findViewById(R.id.spinnerSletor_login);
        //region Spinner Listner
        spinnerSletor_login.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //endregion

        if (getIntent().getExtras() != null) {
            login_phone.setText(getIntent().getStringExtra(GET_PHONE_FROM_EXTRA));
            login_password.setText(getIntent().getStringExtra(GET_PASSWORD_FROM_EXTRA));
            String typeOFUserWorker=getIntent().getStringExtra(GET_TRPE_FROM_EXTRA);
            checkbox_login.setChecked(true);
            if (typeOFUserWorker.equals(USERES_DATABASE_NAME)) {
                spinnerSletor_login.setSelection(1);


            } else if (typeOFUserWorker.equals(WORKER_DATABASE_NAME)) {
                spinnerSletor_login.setSelection(0);

            }

        }

        //viewModel.CheckPref(frame_loading,button_login);

    }

    @Override
    public void LogIn(boolean IsLogin, int type) {
        //frame Loading
        frame_loading.setVisibility(View.GONE);
        button_login.setClickable(true);

        if (IsLogin) {
            if (LoginActivityViewModel.WOKRER_TYPE == type) {
                //Lacnh Worker System
                Intent intent = new Intent(this, WorkerMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();


            } else if (LoginActivityViewModel.USER_TYPE == type) {

            }
        }

    }

    @Override
    public void showToast(String msg) {

        if (msToast != null) {
            msToast.cancel();
        }
        msToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        msToast.show();

    }

    public void loginClick(View view) {


        login_phone = findViewById(R.id.login_phone);
        login_password = findViewById(R.id.login_password);
        spinnerSletor_login = findViewById(R.id.spinnerSletor_login);
        checkbox_login = findViewById(R.id.checkbox_login);
        //frame Loading


        String phone, password, typeOf;

        if (TextUtils.isEmpty(login_password.getText().toString())) {
            showToast(getResources().getString(R.string.emptyuserprPhone));
            return;
        } else if (TextUtils.isEmpty(login_phone.getText().toString())) {
            showToast(getResources().getString(R.string.phonempty));
            return;
        } else if (!NetworkUtils.isCurrentlyOnline(this)) {
            showToast(getResources().getString(R.string.No_Internet_connection));
            return;
        } else {
            phone = login_phone.getText().toString();
            password = login_password.getText().toString();
            if (type[0] == 0) {
                typeOf = WORKER_DATABASE_NAME;
            } else {
                typeOf = USERES_DATABASE_NAME;
            }

            if (checkbox_login.isChecked()) {
                SharedPrefUtils.setLoginData(this, phone, password, typeOf, true);
                viewModel.doLogin(phone, password, typeOf, frame_loading, button_login);

            } else {
                SharedPrefUtils.setLoginData(this, phone, password, typeOf, false);
                viewModel.doLogin(phone, password, typeOf, frame_loading, button_login);
            }


        }


    }

    public void newUserWorkerScreen(View view) {
        //Go To New User Worker
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }


}
