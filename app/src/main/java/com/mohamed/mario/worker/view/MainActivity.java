package com.mohamed.mario.worker.view;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMainBinding;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewmodel.MainActivityViewModel;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements MainActivityViewModel.Listener {

    //For Loaction Permission
    private static final int LOCATION_PERMISSON_INT = 100;
    //Toast
    Toast msToast;
    //private Binding
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // initialize view model
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        viewModel.initSetup(this);
        binding.setViewModel(viewModel);

        StartLoactionGetting();

    }

    //region Listeneres From Model
    @Override
    public void launchActivity(boolean isUser) {
        if (isUser) {
            // todo
        } else {

        }
    }

    @Override
    public void loginToStartActivity(boolean isUserWorker) {
        // True --> User
        // False --> Worker
        if(isUserWorker){
            Intent intent = new Intent(this, UserMainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
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

    //For Getting Image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonIntentsUtils.REQUSER_CODE_Gallery) {
            if (resultCode == RESULT_OK) {
                //Remove label from layout
                viewModel.fullPhotoUri = data.getData();
                CommonIntentsUtils.layout.findViewById(CommonIntentsUtils.Txt_ID).setVisibility(View.GONE);
                Picasso.get().load(viewModel.fullPhotoUri).into((ImageView)
                        CommonIntentsUtils.layout.findViewById(CommonIntentsUtils.IMAGE_ID));
            }
        }
        if (requestCode == CommonIntentsUtils.REQUSER_CODE_CAMERA &&
                resultCode == RESULT_OK) {
            //Remove label from layout
            CommonIntentsUtils.layout.findViewById(CommonIntentsUtils.Txt_ID).setVisibility(View.GONE);
            Picasso.get().load(viewModel.fullPhotoUri).into((ImageView)
                    CommonIntentsUtils.layout.findViewById(CommonIntentsUtils.IMAGE_ID));

        }
    }

    //region Permssion Area
    //For Loaction Permissions
    private void StartLoactionGetting() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?

            final CustomDialog customDialog = new CustomDialog(this, 0,
                    this.getResources().getString(R.string.location_access), this.getResources().getString(R.string.location_access_message)
                    , this.getResources().getString(R.string.ok), this.getResources().getString(R.string.cancel), "Custom");

            customDialog.show();
            customDialog.setOnCancelClickLisner(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
            customDialog.setOnPositiveClickLisner(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                    }, LOCATION_PERMISSON_INT);
                    customDialog.dismiss();
                }
            });


        } else {
            // Permission has already been granted

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSON_INT: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showToast(MainActivity.this.getResources().getString(R.string.succesful_loaction));
                } else {
                    showToast(MainActivity.this.getResources().getString(R.string.no_loaction_user));
                }
                return;
            }

        }
    }
//endregion
}
