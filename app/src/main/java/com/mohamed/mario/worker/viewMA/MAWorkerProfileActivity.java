package com.mohamed.mario.worker.viewMA;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMaWorkerProfileBinding;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.DatabaseUtils;
import com.mohamed.mario.worker.utils.StringUtils;
import com.mohamed.mario.worker.viewMA.adapters.MAWorkerPrevWorkRCAdapter;
import com.mohamed.mario.worker.viewModelFactory.MAWorkerProfileActivityViewModelFactory;
import com.mohamed.mario.worker.viewModelMa.MAWorkerProfileActivityViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * ==> Notes
 * 1- You can only get here from {@link MAWorkerHomeActivity} isa, so you will isa have your own
 *      {@link com.mohamed.mario.worker.model.Worker} object so pass it here as
 *      {@link java.io.Serializable} and make it implement it as well isa.
 */
public class MAWorkerProfileActivity extends AppCompatActivity
        implements MAWorkerProfileActivityViewModel.Listener ,
        MAWorkerPrevWorkRCAdapter.Listener {

    public static final String INTENT_KEY_WORKER_OBJECT = "INTENT_KEY_WORKER_OBJECT";

    private static final int REQUEST_CODE_SUB_PROFILE_IMAGE_VIEWER = 657;
    private static final int REQUEST_CODE_RC_ADAPTER_CAMERA = 99;
    private static final int REQUEST_CODE_RC_ADAPTER_GALLERY = 589;

    public static final String ACTIVITY_RESULT_IMAGE_URL = "ACTIVITY_RESULT_IMAGE_URL";
    public static final String ACTIVITY_RESULT_IMAGE_POSITION = "ACTIVITY_RESULT_IMAGE_POSITION";

    private ActivityMaWorkerProfileBinding binding;

    private MAWorkerProfileActivityViewModel viewModel;

    private MAWorkerPrevWorkRCAdapter prevWorkAdapter;

    private Uri fromRCAdapterFullPhotoUri;
    private int fromRCAdapterImagePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ma_worker_profile);

        // -- Get extra if exists
        Intent intent = getIntent();
        Worker worker = null;
        if (intent != null && intent.hasExtra(INTENT_KEY_WORKER_OBJECT)){
            worker = (Worker) intent.getSerializableExtra(INTENT_KEY_WORKER_OBJECT);
        }

        // -- Setup xml views
        setupXmlViews(worker);

        // -- init view model
        MAWorkerProfileActivityViewModelFactory factory =
                new MAWorkerProfileActivityViewModelFactory(getApplication(), this, worker);
        viewModel = ViewModelProviders.of(this, factory)
                .get(MAWorkerProfileActivityViewModel.class);
        binding.setViewModel(viewModel);
    }

    // ---- Inside onCreate()

    private void setupXmlViews(@Nullable Worker worker){
        // -- Previous Work Images Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        binding.previousWorkImagesRecyclerView.setLayoutManager(layoutManager);
        prevWorkAdapter = new MAWorkerPrevWorkRCAdapter(
                this, worker == null ? null : worker.getWorkImages());
        binding.previousWorkImagesRecyclerView.setAdapter(prevWorkAdapter);

        // -- todo create Reviews Recycler View.
    }

    // ---- View Model Listener Implementation

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void callOnBackPressed() {
        onBackPressed();
    }

    // ---- Previous Work Images RC Adapter Listener Implementation

    @Override
    public void onImageViewClick(String imageUrl, int position) {
        Intent intent = new Intent(this, MASubProfileImageViewerActivity.class);
        intent.putExtra(MASubProfileImageViewerActivity.INTENT_KEY_IMAGE_URL, imageUrl);
        intent.putExtra(MASubProfileImageViewerActivity.INTENT_KEY_IMAGE_POSITION, position);

        startActivityForResult(intent, REQUEST_CODE_SUB_PROFILE_IMAGE_VIEWER);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onCameraFabClick(int position) {
        Context appContext = getApplication().getApplicationContext();

        fromRCAdapterFullPhotoUri = null;
        fromRCAdapterImagePosition = position;

        File imageFile = null;
        try {
            imageFile = CommonIntentsUtils.createImageFile(
                    this);
        }catch (IOException e) {
            Timber.v("Error -> " + e.getMessage());
        }

        if (imageFile != null){
            // to get file path isa. -> imageFile.getAbsolutePath();
            fromRCAdapterFullPhotoUri = FileProvider.getUriForFile(
                    appContext,
                    "com.mohamed.mario.worker.fileprovider",
                    imageFile);

            CommonIntentsUtils.getImageFromCamera(
                    this,
                    appContext.getString(R.string.Get_From_Camera),
                    fromRCAdapterFullPhotoUri,
                    REQUEST_CODE_RC_ADAPTER_CAMERA);
        }else {
            Timber.v("Unexpected behaviour -> 3232 -> empty imageFile");
        }
    }

    @Override
    public void onGalleryFabClick(int position) {
        fromRCAdapterFullPhotoUri = null;
        fromRCAdapterImagePosition = position;

        CommonIntentsUtils.getImageFromGallery(
                this,
                getApplication().getApplicationContext().getString(R.string.Get_From_phone),
                REQUEST_CODE_RC_ADAPTER_GALLERY);
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

            viewModel.handleOnActivityResult(
                    true, data);
        }else if (requestCode == CommonIntentsUtils.REQUEST_CODE_CAMERA
                && resultCode == RESULT_OK) {
            viewModel.handleOnActivityResult(
                    false, data);
        }else if (requestCode == REQUEST_CODE_SUB_PROFILE_IMAGE_VIEWER
                && resultCode == RESULT_OK){
            if (data == null){
                Timber.v("Strange, from sub viewer, null intent.");

                return;
            }

            String imageUrl = data.getStringExtra(ACTIVITY_RESULT_IMAGE_URL);
            int position = data.getIntExtra(ACTIVITY_RESULT_IMAGE_POSITION, 0);

            prevWorkAdapter.swapSingleImageUrl(imageUrl, position);

            // -- update firebase database
            Uri photoUri = null;
            try {
                photoUri = Uri.parse(imageUrl);
            }catch (Exception e){
                Timber.v("Exception caught from parsing string to uri " +
                        "as a result from Sub Activity");
            }
            updateAndSyncPrevWorkImagesForWorkerHereAndInFirebase(
                    photoUri, position);
        }else if (requestCode == REQUEST_CODE_RC_ADAPTER_GALLERY
                && resultCode == RESULT_OK){
            if (data == null){
                Timber.v("Intent is null that came from onActivityResult() FROM RC Adapter");

                return;
            }

            fromRCAdapterFullPhotoUri = data.getData();

            prevWorkAdapter.swapSingleImageUrl(fromRCAdapterFullPhotoUri == null
                    ? "" : fromRCAdapterFullPhotoUri.toString(), fromRCAdapterImagePosition);

            // -- update firebase database
            updateAndSyncPrevWorkImagesForWorkerHereAndInFirebase(
                    fromRCAdapterFullPhotoUri, fromRCAdapterImagePosition);
        }else if (requestCode == REQUEST_CODE_RC_ADAPTER_CAMERA
                && resultCode == RESULT_OK){
            prevWorkAdapter.swapSingleImageUrl(fromRCAdapterFullPhotoUri == null
                    ? "" : fromRCAdapterFullPhotoUri.toString(), fromRCAdapterImagePosition);

            // -- update firebase database
            updateAndSyncPrevWorkImagesForWorkerHereAndInFirebase(
                    fromRCAdapterFullPhotoUri, fromRCAdapterImagePosition);
        }
    }

    // ---- Private Methods

    private void updateAndSyncPrevWorkImagesForWorkerHereAndInFirebase(Uri fullPhotoUri, int position){
        // -- To get the key -> Phone -> use sharedPrefs, or get it from view model
        Worker worker = viewModel.getWorker();
        if (worker == null){
            Timber.v("Strange error, worker in the vm is null, " +
                    "although it must not be null isa.");

            return;
        }

        String phone = worker.getPhone();

        /* 1- Save image into firebase storage, and if success occurs
                -> then save it in database isa. */
        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(
                WORKERES_DATABASE_PHOTO_LOCATION
                        + phone
                        + DatabaseUtils.getWorkerPrevWorkImageNameByPosition(position));

        if (fullPhotoUri == null
                || StringUtils.isNullOrEmpty(fullPhotoUri.toString())){
            // Then delete currently existing image in firebase.
            Task<Void> deleteTask = imageRef.delete();
            deleteTask.addOnSuccessListener(task -> {
                Timber.v("Deleted image successfully el7.");

                ArrayList<String> imagesList = worker.getWorkImages();
                imagesList.remove(position);
                imagesList.add(position, "def");

                HashMap<String , Object> updatedChildFieldsMap = new HashMap<>();
                updatedChildFieldsMap.put("workImages", imagesList);

                Task<Void> updateTask = FirebaseDatabase.getInstance().getReference()
                        .child(WORKER_DATABASE_NAME).child(phone)
                        .updateChildren(updatedChildFieldsMap);
                updateTask.addOnCompleteListener(task1 -> {
                    Timber.v("Images has been updated successfully AFTER DELETION el7, in -> "
                            + phone);

                    // -- Sync here by saving in view model and in adapter as well isa.
                    viewModel.setWorkerPrevWorkImagesList(imagesList);
                    // I think no need for adapter sync as it already have the local uri
                    // and putting the internet uri will make the image appear to reload
                    // so this will be strange behaviour, i guess;
                    //prevWorkAdapter.swapWholeImageUrlList(imagesList);
                });
                updateTask.addOnFailureListener(e -> {
                            /* Maybe because there was no such value in this field in firebase,
                                    so we should save it instead of updating it ?!! */
                    Timber.v("Error in updating work images list AFTER DELETION -> "
                            + e.getMessage());
                });
            });
            deleteTask.addOnFailureListener(e
                    -> Timber.v("Cannot delete and reason is -> " + e.getMessage()));
        }else {
            // Then save new image.
            // also make old arrayListString one with switching to new internet url isa.
            // after update list to firebase update it here in VM so all places are in sync isa.
            UploadTask uploadTask = imageRef.putFile(fullPhotoUri);
            uploadTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() == null || task.getResult().getDownloadUrl() == null){
                        // we already put "def" in image
                        Timber.v("Task or download url is null !!! -> 345");

                        return;
                    }

                    String imageUrl = task.getResult().getDownloadUrl().toString();

                    if (! StringUtils.isNullOrEmpty(imageUrl)){
                        ArrayList<String> imagesList = worker.getWorkImages();
                        imagesList.remove(position);
                        imagesList.add(position, imageUrl);

                        HashMap<String , Object> updatedChildFieldsMap = new HashMap<>();
                        updatedChildFieldsMap.put("workImages", imagesList);

                        Task<Void> updateTask = FirebaseDatabase.getInstance().getReference()
                                .child(WORKER_DATABASE_NAME).child(phone)
                                .updateChildren(updatedChildFieldsMap);
                        updateTask.addOnCompleteListener(task1 -> {
                            Timber.v("Images has been updated successfully el7, in -> "
                                    + phone);

                            // -- Sync here by saving in view model and in adapter as well isa.
                            viewModel.setWorkerPrevWorkImagesList(imagesList);
                            // I think no need for adapter sync as it already have the local uri
                            // and putting the internet uri will make the image appear to reload
                            // so this will be strange behaviour, i guess;
                            //prevWorkAdapter.swapWholeImageUrlList(imagesList);
                        });
                        updateTask.addOnFailureListener(e -> {
                            /* Maybe because there was no such value in this field in firebase,
                                    so we should save it instead of updating it ?!! */
                            Timber.v("Error in updating work images list -> "
                                    + e.getMessage());
                        });
                    }
                }else {
                    Timber.v("Task was not successful, " +
                            "while uploading the image of prev work");
                }
            });
            uploadTask.addOnFailureListener(e ->
                    Timber.v("Error while saving image 3245, and error msg is -> "
                            + e.getMessage()));
        }
    }

    /*private void updatePrevWorkImagesForWorker(ArrayList<String> imagesList){
        // -- To get the key -> Phone -> use sharedPrefs, or get it from view model
        Worker worker = viewModel.getWorker();

        if (worker != null){
            String phone = worker.getPhone();

            HashMap<String , Object> updatedChildFieldsMap = new HashMap<>();
            updatedChildFieldsMap.put("workImages", imagesList);

            Task<Void> updateTask = FirebaseDatabase.getInstance().getReference().child(WORKER_DATABASE_NAME)
                    .child(phone).updateChildren(updatedChildFieldsMap);
            updateTask.addOnCompleteListener(task -> {
                Timber.v("Images has been updated successfully el7, in -> " + phone);
            });
            updateTask.addOnFailureListener(e -> {
//                 Maybe because there was no such value in this field in firebase,
//                        so we should save it instead of updating it ?!!
                Timber.v("Error in updating work images list -> " + e.getMessage());
            });
        }else {
            Timber.v("Strage error, worker in the vm is null, " +
                    "although it must not be null isa.");
        }
    }*/

}
