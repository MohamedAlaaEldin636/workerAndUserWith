package com.mohamed.mario.worker.viewModelMa;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
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
        // Else then fullPhotoUri should already have the Uri isa.

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

    // ----- Listener Interface

    public interface Listener {

        Activity getActivity();

        void callOnBackPressed();

    }
}
