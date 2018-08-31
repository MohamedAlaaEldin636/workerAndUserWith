package com.mohamed.mario.worker.viewModelMa;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.Intent;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;
import com.mohamed.mario.worker.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/30/2018.
 *
 *
 */
public class MAWorkerProfileActivityViewModel extends AndroidViewModel {

    // --- Direct Xml Observable Variables

    public ObservableBoolean showNoInternetConnection = new ObservableBoolean(false);

    public ObservableBoolean showLoadingObservable = new ObservableBoolean(false);

    public ObservableField<String> personalImageUrlObservable = new ObservableField<>("");
    public ObservableField<String> ratingTextObservable = new ObservableField<>("");
    public ObservableField<String> nameTextObservable = new ObservableField<>("");
    public ObservableField<String> passwordTextObservable = new ObservableField<>("");
    public ObservableField<String> descriptionTextObservable = new ObservableField<>("");
    public ObservableField<ArrayList<String>> previousWorkImagesListObservable = new ObservableField<>();

    // --- Private Variables

    private Listener listener;

    @Nullable private Worker worker;

    private Uri fullPhotoUri;

    public MAWorkerProfileActivityViewModel(@NonNull Application application,
                                            Listener listener,
                                            @Nullable Worker worker) {
        super(application);

        this.listener = listener;

        this.worker = worker;

        // -- Make UI changes based on worker object
        makeUIChangesBaseOnWorkerObject();
    }

    // ---- Inside Constructor

    private void makeUIChangesBaseOnWorkerObject() {
        // -- No internet connection view
        showNoInternetConnection.set(worker == null);

        if (worker != null){
            personalImageUrlObservable.set(worker.getPersonalImage());

            ratingTextObservable.set(worker.getRate() + "/5");

            nameTextObservable.set(worker.getName());

            passwordTextObservable.set(worker.getPassword());

            descriptionTextObservable.set(worker.getDescription());

            previousWorkImagesListObservable.set(worker.getWorkImages());

            // -- add on property change to texts to be saved after change into firebase database isa.
            nameTextObservable.addOnPropertyChangedCallback(onTextChangeObservableCallback);
            passwordTextObservable.addOnPropertyChangedCallback(onTextChangeObservableCallback);
            descriptionTextObservable.addOnPropertyChangedCallback(onTextChangeObservableCallback);
            personalImageUrlObservable.addOnPropertyChangedCallback(personalImageObservableCallback);
        }
    }

    // ---- Public Methods

    /**
     * @param fromGallery photo came from gallery, otherwise then it came from camera isa.
     * @param data intent containing data of the photo, if came from gallery.
     */
    public void handleOnActivityResult(boolean fromGallery, @NonNull Intent data){
        if (fromGallery){
            // -- set uri of the photo
            fullPhotoUri = data.getData();
        }
        /* Else then fullPhotoUri should already have the Uri isa. */

        // -- put photo in the image view
        personalImageUrlObservable.set(fullPhotoUri == null ? "" : fullPhotoUri.toString());
    }

    public Listener getListener() {
        return listener;
    }

    @Nullable
    public Worker getWorker() {
        return worker;
    }

    public void setWorkerPrevWorkImagesList(ArrayList<String> imagesList){
        if (worker != null){
            worker.setWorkImages(imagesList);
        }
    }

    // ---- Direct Xml Methods

    public void cameraFabClick(){
        Context appContext = getApplication().getApplicationContext();

        fullPhotoUri = null;

        File imageFile = null;
        try {
            imageFile = CommonIntentsUtils.createImageFile(
                    listener.getActivity());
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
                    listener.getActivity(),
                    appContext.getString(R.string.Get_From_Camera),
                    this.fullPhotoUri,
                    CommonIntentsUtils.REQUEST_CODE_CAMERA);
        }else {
            Timber.v("Unexpected behaviour -> 3232 -> empty imageFile");
        }
    }

    public void galleryFabClick(){
        fullPhotoUri = null;

        CommonIntentsUtils.getImageFromGallery(
                listener.getActivity(),
                getApplication().getApplicationContext().getString(R.string.Get_From_phone),
                CommonIntentsUtils.REQUEST_CODE_GALLERY);
    }

    public void refreshFabClick(){
        /* ==> Flow
        1- check internet connectivity isa.
        2- show loading view isa.
        3- get from shared pref own pass
        4- request from firebase the worker object isa, and set it to our global one isa.
        5- call the method here called makeUIChangesBaseOnWorkerObject()
        6- hide loading and in every error isa as well
         */
        Context appContext = getApplication().getApplicationContext();

        if (NetworkUtils.isCurrentlyOnline(appContext)){
            // -- show loading indication
            showLoadingObservable.set(true);

            // -- get own phone
            String phone = SharedPrefUtils.getLoginPhone(appContext);

            // -- request worker object from firebase
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(WORKER_DATABASE_NAME);
            Query phoneQuery = ref.orderByChild(
                    USERES_DATABASE_QUERY_PHONE/* Note workers key is phone as well */)
                    .equalTo(phone);
            phoneQuery.addListenerForSingleValueEvent(firebaseValueEventListener);
        }else {
            ((BaseApplication) appContext).showToast(
                    appContext.getString(R.string.try_again_and_check_internet_connection));
        }
    }

    // --- Private Listener Variables

    private ValueEventListener firebaseValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Worker> workersList = new ArrayList<>();
            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                workersList.add(singleSnapshot.getValue(Worker.class));
            }
            if (workersList.size() == 0) {
                Context appContext = getApplication().getApplicationContext();

                ((BaseApplication) appContext).showToast(
                        appContext.getString(R.string.phonedontexsist));
            }else {
                worker = workersList.get(0);

                /* -- Note below method will already isa take care of
                        No Internet Connection view visibility */
                makeUIChangesBaseOnWorkerObject();
            }

            showLoadingObservable.set(false);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Context appContext = getApplication().getApplicationContext();

            ((BaseApplication) appContext).showToast(
                    appContext.getString(R.string.try_again_and_check_internet_connection));

            showLoadingObservable.set(false);
        }
    };

    private Observable.OnPropertyChangedCallback onTextChangeObservableCallback =
            new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (sender == nameTextObservable){
                saveTextToFirebaseDatabase("name", nameTextObservable.get());
            }else if (sender == passwordTextObservable){
                saveTextToFirebaseDatabase("password", nameTextObservable.get());
            }else if (sender == descriptionTextObservable){
                saveTextToFirebaseDatabase("description", nameTextObservable.get());
            }
        }
    };

    private Observable.OnPropertyChangedCallback personalImageObservableCallback
            = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            if (worker == null){
                Timber.v("Worker is done, so cannot proceed to save personal image isa.");

                return;
            }

            String phone = worker.getPhone();

            /* 1- Save image into firebase storage, and if success occurs
                -> then save it in database isa. */
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(
                    WORKERES_DATABASE_PHOTO_LOCATION
                            + phone
                            + WORKERES_DATABASE_PHOTO_NAME);

            if (fullPhotoUri == null
                    || StringUtils.isNullOrEmpty(fullPhotoUri.toString())){
                // Then delete currently existing image in firebase.
                Task<Void> deleteTask = imageRef.delete();
                deleteTask.addOnSuccessListener(task -> {
                    Timber.v("Deleted image successfully el7.");

                    saveTextToFirebaseDatabase("personalImage", "def");
                });
                deleteTask.addOnFailureListener(e
                        -> Timber.v("Cannot delete and reason is -> " + e.getMessage()));
            }else {
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
                            saveTextToFirebaseDatabase("personalImage", imageUrl);
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
    };

    // ---- Private Methods

    private void saveTextToFirebaseDatabase(String key, String value){
        if (worker == null){
            Timber.v("Worker is null, so cannot proceed to save text method in firebase isa.");

            return;
        }

        String phone = worker.getPhone();

        HashMap<String , Object> updatedChildFieldsMap = new HashMap<>();
        updatedChildFieldsMap.put(key, value);

        Task<Void> updateTask = FirebaseDatabase.getInstance().getReference()
                .child(WORKER_DATABASE_NAME).child(phone)
                .updateChildren(updatedChildFieldsMap);
        updateTask.addOnCompleteListener(task1
                -> Timber.v("saved text to firebase correctly el7 to this phone -> " + phone));
        updateTask.addOnFailureListener(e -> {
            /* Maybe because there was no such value in this field in firebase,
                    so we should save it instead of updating it ?!! */
            Timber.v("Error in saving text to firebase -> "
                    + e.getMessage());
        });
    }

    // ----- Listener Interface

    public interface Listener {

        Activity getActivity();

        void callOnBackPressed();

    }
}
