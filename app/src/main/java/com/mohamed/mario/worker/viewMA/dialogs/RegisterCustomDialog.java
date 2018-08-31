package com.mohamed.mario.worker.viewMA.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_LOCATION_USER_PATH;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_PHOTO_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERS_DATABASE_LOCATION_WORKER_PATH;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/28/2018.
 * 
 */
@SuppressWarnings("FieldCanBeLocal")
public class RegisterCustomDialog extends CustomDialog {

    @LayoutRes private int layoutRes;
    private String title;
    private String message;
    private String positiveName;
    private String cancelName;
    
    private View rootView;
    private TextView titleTextView, messageTextView;
    private Button dialogPositiveButton, dialogNegativeButton;
    
    private View registerLayoutView;
    private ImageView userPhoto;
    private TextView txtLabel;
    private EditText ebtUsername, ebtPhone, ebtPassword;
    private FrameLayout loadingFrameLayout;
    
    private Listener listener;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private StorageReference imageRef;

    private FusedLocationProviderClient mFusedLocationClient;

    private Uri fullPhotoUri;

    private Spinner professionSpinner;
    private int professionPosition;

    private boolean isUser;

    public RegisterCustomDialog(@NonNull Context context, int layoutRes, String title, 
                                String message, String positiveName, String cancelName, 
                                Listener listener, boolean isUser) {
        super(context, layoutRes, title, message, positiveName, cancelName);

        this.layoutRes = layoutRes;
        this.title = title;
        this.message = message;
        this.positiveName = positiveName;
        this.cancelName = cancelName;
        
        this.listener = listener;

        this.isUser = isUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom);
        
        rootView = findViewById(R.id.rootView);

        registerLayoutView = LayoutInflater.from(getContext()).inflate(
                layoutRes, rootView.findViewById(R.id.frameLayout), true);
        
        // -- initialize views
        initViews();
        
        titleTextView.setText(title);
        messageTextView.setText(message);
        dialogPositiveButton.setText(positiveName);
        dialogNegativeButton.setText(cancelName);
        
        // -- Handle views clicks
        setupViewsClicks();
    }
    
    //region Inside onCreate()

    private void initViews() {
        titleTextView = findViewById(R.id.dialog_title_txt);
        messageTextView = findViewById(R.id.dialog_message_txt);
        dialogPositiveButton = findViewById(R.id.dialog_postive_button);
        dialogNegativeButton = findViewById(R.id.dialog_negative_button);

        userPhoto = registerLayoutView.findViewById(R.id.imageView);
        txtLabel = registerLayoutView.findViewById(R.id.txt_label);
        ebtUsername = registerLayoutView.findViewById(R.id.ebt_username);
        ebtPhone = registerLayoutView.findViewById(R.id.ebt_phone);
        ebtPassword = registerLayoutView.findViewById(R.id.ebt_password);
        loadingFrameLayout = registerLayoutView.findViewById(R.id.frame_loading);

        if (! isUser){
            professionSpinner = registerLayoutView.findViewById(R.id.spinnerSeletor);
        }
    }

    private void setupViewsClicks() {
        userPhoto.setOnClickListener(showPopupMenuClickListener);
        
        dialogPositiveButton.setOnClickListener(checkRegistrationOnClickListener);

        dialogNegativeButton.setOnClickListener(v -> dismiss());

        if (professionSpinner != null){
            professionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    professionPosition = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }
    
    //endregion
    
    // --- Private Listener Variables

    private ValueEventListener firebaseValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Context appContext = getContext().getApplicationContext();
            BaseApplication baseApplication = ((BaseApplication) appContext);

            if (isUser){
                List<User> usersList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    usersList.add(singleSnapshot.getValue(User.class));
                }
                if (usersList.size() == 0) {
                /* Flow is as follows isa
                1- ensure save of location, if false(save couldn't be done), tell user to try again
                        since this app depend on location as a fundamental feature.
                2- if true, then save in firebaseDatabase the data of the user/worker
                3- then save in sharedPrefs isa
                4- then finally go and save the image, even if error occurs to image uploading,
                        it's ok since we handle no existence of image by a fallback/placeholder
                        image isa, and user can later changes it.
                5- now go to the appropriate -> Home Activity either user or worker
                */

                    saveNewUserOrWorkerLocationThenProceedToOtherSteps();
                }else {
                    baseApplication.showToast(
                            appContext.getString(R.string.this_phone_has_registerd));

                    makeUIChangesAccordingToLoading(false);
                }
            }else {
                List<Worker> workersList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    workersList.add(singleSnapshot.getValue(Worker.class));
                }
                if (workersList.size() == 0) {
                /* Flow is as follows isa
                1- ensure save of location, if false(save couldn't be done), tell user to try again
                        since this app depend on location as a fundamental feature.
                2- if true, then save in firebaseDatabase the data of the user/worker
                3- then save in sharedPrefs dunno what to pass in rememberMe maybe make it here isa.
                4- then finally go and save the image, even if error occurs to image uploading,
                        it's ok since we handle no existence of image by a fallback/placeholder
                        image isa, and user can later changes it.
                5- now go to the appropriate -> Home Activity either user or worker
                */

                    saveNewUserOrWorkerLocationThenProceedToOtherSteps();
                }else {
                    baseApplication.showToast(
                            appContext.getString(R.string.this_phone_has_registerd));

                    makeUIChangesAccordingToLoading(false);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.v("onCancelled" + databaseError.toException());

            Context appContext = getContext().getApplicationContext();
            ((BaseApplication) appContext).showToast(
                    appContext.getString(R.string.try_again_and_check_internet_connection));

            makeUIChangesAccordingToLoading(false);
        }
    };

    private View.OnClickListener showPopupMenuClickListener = v -> {
        // -- ContextWrapper For Theme Background To Style Of Popup menu
        final Context appContext = getContext().getApplicationContext();

        ContextThemeWrapper ctw = new ContextThemeWrapper(
                appContext, R.style.PopupMenu);
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(ctw, userPhoto);
        popupMenu.inflate(R.menu.popup_menu_camera_or_gallery);
        popupMenu.show();         //We need to Show it

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_gallery) {
                fullPhotoUri = null;

                CommonIntentsUtils.getImageFromGallery(
                        listener.getActivity(),
                        appContext.getString(R.string.Get_From_phone),
                        CommonIntentsUtils.REQUEST_CODE_GALLERY);
            }else if (item.getItemId() == R.id.action_camera) {
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

            return true;
        });
    };
    
    private View.OnClickListener checkRegistrationOnClickListener = v -> {
        // -- Show UI loading indication
        makeUIChangesAccordingToLoading(true);

        Context appContext = getContext().getApplicationContext();
        BaseApplication baseApplication = ((BaseApplication) appContext);
        
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(isUser ? USERES_DATABASE_NAME : WORKER_DATABASE_NAME);

        /* Note
        Do not worry even if fullPhotoUri is empty as we handle isa this case when we are
                about to save the data of user/worker */

        if (TextUtils.isEmpty(ebtUsername.getText().toString())
                || ebtUsername.getText().length() < 5) {
            baseApplication.showToast(
                    appContext.getString(R.string.You_Must_youname));
        }else if (TextUtils.isEmpty(ebtPhone.getText().toString())
                || ebtPhone.getText().length() != 11) {
            baseApplication.showToast(
                    appContext.getString(R.string.You_Must_yourphone));
        }else if (TextUtils.isEmpty(ebtPassword.getText().toString())
                || ebtPassword.getText().length() < 8) {
            baseApplication.showToast(
                    appContext.getString(R.string.You_Must_yourpassword));
        }else if (!NetworkUtils.isCurrentlyOnline(appContext)){
            baseApplication.showToast(
                    appContext.getString(R.string.No_Internet_connection));
        }else {
            // -- Check If Same user in database
            Query phoneQuery = ref.orderByChild(
                    USERES_DATABASE_QUERY_PHONE/* Note workers key is phone as well */)
                    .equalTo(ebtPhone.getText().toString());
            phoneQuery.addListenerForSingleValueEvent(firebaseValueEventListener);

            return;
        }

        makeUIChangesAccordingToLoading(false);
    };

    // ---- Private Methods

    private void saveNewUserOrWorkerLocationThenProceedToOtherSteps() {
        final Context appContext = getContext().getApplicationContext();

        // -- Save location
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(
                isUser ? USERES_DATABASE_LOCATION_USER_PATH : WORKERS_DATABASE_LOCATION_WORKER_PATH);
        final GeoFire geoFire = new GeoFire(ref);
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);
            Task<Location> locationTask = mFusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    geoFire.setLocation(
                            ebtPhone.getText().toString(),
                            new GeoLocation(location.getLatitude(), location.getLongitude()),
                            (key, error) -> {
                                // Note DatabaseError returns -> The error or null if no error occurred.
                                if (error == null){
                                    // Location saved successfully go to next steps.
                                    saveNewUserOrWorkerInFirebaseDatabaseAndSharedPrefsThenImage();
                                }else {
                                    makeUIChangesAccordingToLoading(false);

                                    ((BaseApplication) appContext).showToast(
                                            appContext.getString(R.string.try_again_and_check_internet_connection));
                                }
                            });
                }else {
                    makeUIChangesAccordingToLoading(false);

                    ((BaseApplication) appContext).showToast(
                            appContext.getString(R.string.try_again_and_check_internet_connection));
                }
            });
            locationTask.addOnFailureListener(e -> {
                makeUIChangesAccordingToLoading(false);

                ((BaseApplication) appContext).showToast(
                        appContext.getString(R.string.try_again_and_check_internet_connection));
            });
        }
    }

    private void saveNewUserOrWorkerInFirebaseDatabaseAndSharedPrefsThenImage(){
        Context appContext = getContext().getApplicationContext();

        String phone = ebtPhone.getText().toString();
        String password = ebtPassword.getText().toString();
        String type = isUser ? USERES_DATABASE_NAME : WORKER_DATABASE_NAME;

        User user = new User(
                ebtUsername.getText().toString(),
                phone,
                password,
                fullPhotoUri == null ? "def" : fullPhotoUri.toString(),
                null,
                null,
                null,
                "");

        Worker worker = new Worker(
                ebtUsername.getText().toString(),
                password,
                phone,
                "",
                fullPhotoUri == null ? "def" : fullPhotoUri.toString(),
                String.valueOf(professionPosition),
                "",
                null,
                null,
                0,
                0);

        // -- Save in firebase real-time database, and in sharedPrefs.
        FirebaseDatabase.getInstance().getReference().child(
                isUser ? USERES_DATABASE_NAME : WORKER_DATABASE_NAME)
                .child(phone).setValue(isUser ? user : worker);
        // todo always make it rememberMe or ask user isa in this layout isa.
        SharedPrefUtils.setLoginData(appContext, phone, password, type, true);

        // -- Save Image to firebase
        if (fullPhotoUri == null || fullPhotoUri.toString().isEmpty()){
            changeUIAndInformActivityToLaunchHomeActivityAndDismissThisDialog();
        }else {
            if (isUser){
                imageRef = storage.getReference().child(
                        USERES_DATABASE_PHOTO_LOCATION + phone + USERES_DATABASE_PHOTO_NAME);
            }else {
                imageRef = storage.getReference().child(
                        WORKERES_DATABASE_PHOTO_LOCATION + phone + WORKERES_DATABASE_PHOTO_NAME);
            }
            UploadTask uploadTask = imageRef.putFile(fullPhotoUri);
            uploadTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() == null || task.getResult().getDownloadUrl() == null){
                        // we already put "def" in image

                        return;
                    }

                    String personalImage = task.getResult().getDownloadUrl().toString();

                    if (personalImage != null && ! personalImage.isEmpty()){
                        HashMap<String , Object> updatedChildFieldsMap = new HashMap<>();
                        updatedChildFieldsMap.put("personalImage", personalImage);

                        FirebaseDatabase.getInstance().getReference().child(
                                isUser ? USERES_DATABASE_NAME : WORKER_DATABASE_NAME)
                                .child(phone).updateChildren(updatedChildFieldsMap)/*.addOnComplete.. no need*/;
                    }

                    changeUIAndInformActivityToLaunchHomeActivityAndDismissThisDialog();
                }else {
                    changeUIAndInformActivityToLaunchHomeActivityAndDismissThisDialog();
                }
            });
            uploadTask.addOnFailureListener(e -> {
                Timber.v("Error while saving image and error msg is -> " + e.getMessage());

                changeUIAndInformActivityToLaunchHomeActivityAndDismissThisDialog();
            });
        }
    }

    private void changeUIAndInformActivityToLaunchHomeActivityAndDismissThisDialog(){
        // -- Remove loading indication
        makeUIChangesAccordingToLoading(false);

        // -- Tell user to launch activity
        listener.loginAndStartHomeActivityAccordingTo(isUser);

        // -- Dismiss this dialog.
        dismiss();
    }

    private void makeUIChangesAccordingToLoading(boolean isLoading){
        // -- indicate loading to make user wait till a result occur isa, and ensure no disruption
        if (isLoading){
            loadingFrameLayout.setVisibility(View.VISIBLE);
            dialogPositiveButton.setEnabled(false);
            dialogNegativeButton.setEnabled(false);
            setCancelable(false);
        }else {
            loadingFrameLayout.setVisibility(View.GONE);
            dialogPositiveButton.setEnabled(true);
            dialogNegativeButton.setEnabled(true);
            setCancelable(true);
        }
    }

    // ---- Public Methods

    /**
     * @param fromGallery photo came from gallery, otherwise then it came from camera isa.
     * @param data nonNull intent containing data of the photo.
     */
    public void handleOnActivityResult(boolean fromGallery, @NonNull Intent data){
        if (fromGallery){
            // -- set uri of the photo
            fullPhotoUri = data.getData();
            
            // -- Remove label from layout
            txtLabel.setVisibility(View.GONE);
            
            // -- put photo in the image view
            Picasso.get()
                    .load(fullPhotoUri)
                    .into(userPhoto);
        }else {
            // -- Remove label from layout
            txtLabel.setVisibility(View.GONE);

            // -- put photo in the image view
            Picasso.get()
                    .load(fullPhotoUri)
                    .into(userPhoto);
        }
    }

    // ----- Public Interface Listener for this dialog

    public interface Listener {

        Activity getActivity();

        void loginAndStartHomeActivityAccordingTo(boolean isUser);

    }
    
}
