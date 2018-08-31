package com.mohamed.mario.worker.viewMA;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMaSubProfileImageViewerBinding;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public class MASubProfileImageViewerActivity extends AppCompatActivity {

    public static final String INTENT_KEY_IMAGE_URL = "INTENT_KEY_IMAGE_URL";
    public static final String INTENT_KEY_IMAGE_POSITION = "INTENT_KEY_IMAGE_POSITION";

    private ActivityMaSubProfileImageViewerBinding binding;

    private ObservableField<String> imageUrl = new ObservableField<>("");

    private boolean changesMade = false;

    private Uri fullPhotoUri;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_ma_sub_profile_image_viewer);

        // -- Set the result as canceled, unless specified otherwise.
        setResult(RESULT_CANCELED);

        // -- On change listener of observables
        respondToObservables();

        // -- Get extras values
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_KEY_IMAGE_URL)){
            String value = intent.getStringExtra(INTENT_KEY_IMAGE_URL);
            imageUrl.set(value);

            position = intent.getIntExtra(INTENT_KEY_IMAGE_POSITION, 0);
        }else {
            finish();

            Timber.v("Strange, no image url found in extras !!!");

            return;
        }

        // -- Setup xml views
        setupViews();

        // -- Setup xml clicks
        setupClicks();
    }

    private void respondToObservables(){
        imageUrl.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                String value = imageUrl.get();

                if (StringUtils.isNullOrEmpty(value)){
                    binding.noImageExistsFrameLayout.setVisibility(View.VISIBLE);
                }else {
                    binding.noImageExistsFrameLayout.setVisibility(View.GONE);

                    // -- Set image
                    Picasso.get()
                            .load(value)
                            .placeholder(R.drawable.workerbanner)
                            .into(binding.imageView);
                }
            }
        });
    }

    private void setupViews() {
        // -- Set image
        Picasso.get()
                .load(imageUrl.get())
                .placeholder(R.drawable.workerbanner)
                .into(binding.imageView);
    }

    private void setupClicks() {
        // -- delete
        binding.deleteFab.setOnClickListener(v -> {
            // تم مسح الصورة بنجاح
            imageUrl.set("");

            changesMade = true;
        });

        // -- gallery
        binding.galleryFab.setOnClickListener(v -> {
            fullPhotoUri = null;

            CommonIntentsUtils.getImageFromGallery(
                    this,
                    getApplication().getApplicationContext().getString(R.string.Get_From_phone),
                    CommonIntentsUtils.REQUEST_CODE_GALLERY);
        });

        // -- camera
        binding.cameraFab.setOnClickListener(v -> {
            Context appContext = getApplication().getApplicationContext();

            fullPhotoUri = null;

            File imageFile = null;
            try {
                imageFile = CommonIntentsUtils.createImageFile(
                        this);
            }catch (IOException e) {
                Timber.v("Error -> " + e.getMessage());
            }

            if (imageFile != null){
                // to get file path isa. -> imageFile.getAbsolutePath();
                this.fullPhotoUri = FileProvider.getUriForFile(
                        appContext,
                        "com.mohamed.mario.worker.fileprovider",
                        imageFile);

                CommonIntentsUtils.getImageFromCamera(
                        this,
                        appContext.getString(R.string.Get_From_Camera),
                        this.fullPhotoUri,
                        CommonIntentsUtils.REQUEST_CODE_CAMERA);
            }else {
                Timber.v("Unexpected behaviour -> 3232 -> empty imageFile");
            }
        });


        // -- back
        binding.doneFab.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(MAWorkerProfileActivity.ACTIVITY_RESULT_IMAGE_URL, imageUrl.get());
            data.putExtra(MAWorkerProfileActivity.ACTIVITY_RESULT_IMAGE_POSITION, position);

            setResult(RESULT_OK, data);

            finish();
        });
    }

    // ---- Overriding onActivityResult() -> image result from camera, or gallery

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonIntentsUtils.REQUEST_CODE_GALLERY
                && resultCode == RESULT_OK) {
            if (data == null){
                Timber.v("Intent is null that came from onActivityResult()");

                return;
            }

            fullPhotoUri = data.getData();

            if (fullPhotoUri != null){
                imageUrl.set(fullPhotoUri.toString());

                changesMade = true;
            }
        }else if (requestCode == CommonIntentsUtils.REQUEST_CODE_CAMERA
                && resultCode == RESULT_OK) {
            if (fullPhotoUri != null){
                imageUrl.set(fullPhotoUri.toString());

                changesMade = true;
            }
        }
    }

    // -- Override onBackPressed()

    @Override
    public void onBackPressed() {
        if (changesMade){
            new AlertDialog.Builder(this)
                    .setMessage("تم حدوث تغييرات !! هل تريد تجاهلها ام لا؟" + "\n" + "اذا كنت تريد تفعيل التغيرات اضغط على قم بالتغييرات بالاسفل.")
                    .setPositiveButton("تجاهلها", (dialog, which) -> {
                        dialog.dismiss();

                        finish();
                    })
                    .setNegativeButton("ارجع للصورة", (dialog, which)
                            -> dialog.dismiss())
                    .setNeutralButton("قم بالتغييرات", (dialog, which) -> {
                        dialog.dismiss();

                        Intent data = new Intent();
                        data.putExtra(MAWorkerProfileActivity.ACTIVITY_RESULT_IMAGE_URL, imageUrl.get());
                        data.putExtra(MAWorkerProfileActivity.ACTIVITY_RESULT_IMAGE_POSITION, position);

                        setResult(RESULT_OK, data);

                        finish();
                    })
                    .show();
        }else {
            super.onBackPressed();
        }
    }
}
