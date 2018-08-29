package com.mohamed.mario.worker.viewMA;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.view.UserMainActivity;
import com.mohamed.mario.worker.view.WorkerMainActivity;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewMA.dialogs.RegisterCustomDialog;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MARegisterActivity extends AppCompatActivity implements RegisterCustomDialog.Listener{

    // --- Constants

    private static final int LOCATION_PERMISSION_INT = 100;

    // --- Private Variables

    private RegisterCustomDialog registerCustomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_ma_register);

        // -- Check for location permission
        checkLocationPermissionAndRequestItIfNotGranted();
    }

    // ---- Inside onCreate()

    private void checkLocationPermissionAndRequestItIfNotGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, show an explanation dialog even if it is not asked before
            //      since audience might not understand english dialog.

            final CustomDialog customDialog = new CustomDialog(
                    this,
                    0,
                    getString(R.string.location_access),
                    getString(R.string.location_access_message),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                    "Custom");

            customDialog.show();

            customDialog.setOnCancelClickLisner( v
                    -> customDialog.dismiss());

            customDialog.setOnPositiveClickLisner( v -> {
                ActivityCompat.requestPermissions(MARegisterActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_INT);

                customDialog.dismiss();
            });
        }// Else permission is already granted, so do nothing.
    }

    // ---- Xml Direct Click Methods

    public void userClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            checkLocationPermissionAndRequestItIfNotGranted();
        }else {
            registerCustomDialog = new RegisterCustomDialog(
                    this,
                    R.layout.user_register,
                    getString(R.string.registeruser),
                    getString(R.string.data_enter),
                    getString(R.string.submit),
                    getString(R.string.cancel),
                    this,
                    true);

            registerCustomDialog.show();
        }
    }

    public void workerClick(View view){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            checkLocationPermissionAndRequestItIfNotGranted();
        }else {
            registerCustomDialog = new RegisterCustomDialog(
                    this,
                    R.layout.worker_register,
                    getString(R.string.workerregistration),
                    getString(R.string.data_enter),
                    getString(R.string.submit),
                    getString(R.string.cancel),
                    this,
                    false);

            registerCustomDialog.show();
        }
    }

    // ---- Register Custom Dialog Listener Implementation

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void loginAndStartHomeActivityAccordingTo(boolean isUser) {
        // Note finishAffinity() is called to ensure LoginActivity is finished as well.

        if(isUser){
            finishAffinity();

            Intent intent = new Intent(this, UserMainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }else {
            finishAffinity();

            Intent intent = new Intent(this, MAWorkerHomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    // ---- Overriding onActivityResult() -> image result from camera, or gallery

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null){
            Timber.v("Intent is null that came from onActivityResult()");

            return;
        }

        if (requestCode == CommonIntentsUtils.REQUEST_CODE_GALLERY
                && resultCode == RESULT_OK) {
            registerCustomDialog.handleOnActivityResult(
                    true, data);
        }else if (requestCode == CommonIntentsUtils.REQUEST_CODE_CAMERA
                && resultCode == RESULT_OK) {
            registerCustomDialog.handleOnActivityResult(
                    false, data);
        }
    }

    // ---- Overriding onRequestPermissionsResult() -> location permission

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        BaseApplication baseApplication = ((BaseApplication) getApplicationContext());

        switch (requestCode) {
            case LOCATION_PERMISSION_INT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    baseApplication.showToast(
                            getString(R.string.succesful_loaction));
                }else {
                    baseApplication.showToast(
                            getString(R.string.no_loaction_user));
                }
            }
        }
    }

}
