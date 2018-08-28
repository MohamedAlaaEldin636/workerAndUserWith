package com.mohamed.mario.worker.viewModelMa;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.Review;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.utils.DatabaseUtils;
import com.mohamed.mario.worker.utils.NetworkUtils;
import com.mohamed.mario.worker.utils.SharedPrefUtils;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewmodel.MainActivityViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_LOCATION_USER_PATH;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_PHOTO_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_LOCATION;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKERES_DATABASE_PHOTO_NAME;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;
import static com.mohamed.mario.worker.utils.SharedPrefUtils.VALUE_KEY_USER;
import static com.mohamed.mario.worker.utils.SharedPrefUtils.VALUE_KEY_WORKER;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MARegisterActivityViewModel extends AndroidViewModel {

    // --- Private Variables

    private Listener listener;

    private DatabaseReference mDatabase;

    private FusedLocationProviderClient mFusedLocationClient;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private StorageReference imageRef;

    public Uri fullPhotoUri;

    public MARegisterActivityViewModel(@NonNull Application application, Listener listener) {
        super(application);

        this.listener = listener;

        // -- assign initial values
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                getApplication().getApplicationContext());

        // -- check if previous user or worker todo read comment inside that method
        checkIfPreviouslyRegistered();
    }

    /** Inside {@link #MARegisterActivityViewModel(Application, Listener)} */
    private void checkIfPreviouslyRegistered() {
        // to-do which behaviou you really want, please read below comment ->
        /*
        if user wipes data in settings then one device can have multiple accounts
        or if he removes cache, and that might happen since a log of apps, including system suggests
        removing cahce data in case of low mempry so better save firebase token as list of strings in firebase
        names bothUserAndWorkerFirebaseTokens
        and on app launch, we check and request getting token, and if it matches a specific token
        we will prevent phone from registering new account, and if that is the intended behaviour then add forgot password as well isa.
        bs kda fe moshkela law fe new device eshtarah el user isa, fa seb 1 device can make several accounts 3ade
        or whenever a login occurs we ensure same token and replace with prev if exists isa from firebase.
         */

        /*String loginInfo = SharedPrefUtils.getLoginData(getApplication().getApplicationContext());

        if (loginInfo.equals(VALUE_KEY_USER)) {
            listener.launchActivity(true);
        } else if (loginInfo.equals(VALUE_KEY_WORKER)) {
            listener.launchActivity(false);
        } // Else do nothing.*/
    }

    //region Direct Xml Methods

    public void userClick() {
        Context context = getApplication().getApplicationContext();

        final CustomDialog customDialog = new CustomDialog(
                listener.getActivity(),
                R.layout.user_register,
                context.getResources().getString(R.string.registeruser),
                context.getResources().getString(R.string.data_enter),
                context.getResources().getString(R.string.submit),
                context.getResources().getString(R.string.cancel));

        customDialog.show();

        View register_layout = customDialog.getRootView();
        final ImageView user_photo = register_layout.findViewById(R.id.imageView);
        final TextView txt_label = register_layout.findViewById(R.id.txt_label);
        final EditText ebt_username = register_layout.findViewById(R.id.ebt_username);
        final EditText ebt_phone = register_layout.findViewById(R.id.ebt_phone);
        final EditText ebt_password = register_layout.findViewById(R.id.ebt_password);
        final FrameLayout frame_loading = register_layout.findViewById(R.id.frame_loading);

        // For Handel On Activity Result
        CommonIntentsUtils.layout = register_layout;
        CommonIntentsUtils.IMAGE_ID = user_photo.getId();
        CommonIntentsUtils.Txt_ID = txt_label.getId();

        //region user_photo click
        user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ContextWrapper For Them Background To Style Of Popupmenu
                ContextThemeWrapper ctw = new ContextThemeWrapper(context, R.style.PopupMenu);
                PopupMenu popupMenu = new PopupMenu(ctw, user_photo);
                popupMenu.inflate(R.menu.popup_menu_camera_or_gallery);
                popupMenu.show();         //We need to Show it

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_gallery) {
                            fullPhotoUri = null;
                            CommonIntentsUtils.getImageFromGallery(listener.getActivity(),
                                    context.getResources().getString(R.string.Get_From_phone)
                                    , CommonIntentsUtils.REQUEST_CODE_GALLERY);

                        } else if (item.getItemId() == R.id.action_camera) {
                            fullPhotoUri = null;
                            File imageFile = null;
                            try {
                                imageFile = CommonIntentsUtils.createImageFile(listener.getActivity());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // to get file path isa. -> imageFile.getAbsolutePath();
                            fullPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "com.mohamed.mario.worker.fileprovider",
                                    imageFile);
                            CommonIntentsUtils.getImageFromCamera(listener.getActivity(), context.getResources().getString(R.string.Get_From_Camera)
                                    , fullPhotoUri, CommonIntentsUtils.REQUEST_CODE_CAMERA);
                        }

                        return true;
                    }
                });
            }

        });
        //endregion
        //region Button Clicks
        // oK Button
        customDialog.setOnPositiveClickLisner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //////////////////////////
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(USERES_DATABASE_NAME);
                //////////////////
                if (fullPhotoUri == null) {
                    //We Will Put Default Photo TODO
                    fullPhotoUri=Uri.parse("def");
                }
                if (TextUtils.isEmpty(ebt_username.getText().toString()) || ebt_username.getText().length() < 5) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_youname));
                } else if (TextUtils.isEmpty(ebt_phone.getText().toString()) || ebt_phone.getText().length() != 11) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_yourphone));
                } else if (TextUtils.isEmpty(ebt_password.getText().toString()) || ebt_password.getText().length() < 8) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_yourpassword));
                } else if (!NetworkUtils.isCurrentlyOnline(getApplication().getApplicationContext())) {
                    listener.showToast(context.getResources().getString(R.string.No_Internet_connection));

                } else {
                    ////////////////////////////Check If Same user in database
                    Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(ebt_phone.getText().toString());
                    phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // If user added no need to add more we will print message
                            List<User> users_list = new ArrayList<>();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                users_list.add(singleSnapshot.getValue(User.class));
                            }
                            if (users_list.size() == 0) {
                                customDialog.getDialog_postive_button().setClickable(false);
                                customDialog.getDialog_negative_button().setClickable(false);
                                writeNewUser(ebt_username.getText().toString(),
                                        ebt_phone.getText().toString(), ebt_password.getText().toString()
                                        , fullPhotoUri.toString(), null, null, null, ""
                                        , frame_loading, customDialog);
                                listener.showToast(context.getResources().getString(R.string.successMessage));
                                listener.loginToStartActivity(true);


                            } else {
                                listener.showToast(context.getResources().getString(R.string.this_phone_has_registerd));

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Mario", "onCancelled", databaseError.toException());
                        }
                    });
                    //////////////////////////


                }
            }
        });

        customDialog.setOnCancelClickLisner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
//endregion


    }


    public void wokerClick(View view) {
        Context context = getApplication().getApplicationContext();

        final CustomDialog customDialog = new CustomDialog(listener.getActivity(), R.layout.worker_register,
                context.getResources().getString(R.string.workerregistration), context.getResources().getString(R.string.data_enter)
                , context.getResources().getString(R.string.submit), context.getResources().getString(R.string.cancel));

        customDialog.show();


        View register_layout = customDialog.getRootView();
        final ImageView user_photo = register_layout.findViewById(R.id.imageView);
        final TextView txt_label = register_layout.findViewById(R.id.txt_label);
        final EditText ebt_username = register_layout.findViewById(R.id.ebt_username);
        final EditText ebt_phone = register_layout.findViewById(R.id.ebt_phone);
        final EditText ebt_password = register_layout.findViewById(R.id.ebt_password);
        final FrameLayout frame_loading = register_layout.findViewById(R.id.frame_loading);
        Spinner professionSpinner = register_layout.findViewById(R.id.spinnerSeletor);


        //region Spinner Listner
        final int[] profession = {0};
        professionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profession[0] = position;
                String[] s = getApplication().getBaseContext().getResources().getStringArray(R.array.professions_list);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //endregion

        // For Handel On Activity Result
        CommonIntentsUtils.layout = register_layout;
        CommonIntentsUtils.IMAGE_ID = user_photo.getId();
        CommonIntentsUtils.Txt_ID = txt_label.getId();

        //region user_photo click
        user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ContextWrapper For Them Background To Style Of Popupmenu
                ContextThemeWrapper ctw = new ContextThemeWrapper(context, R.style.PopupMenu);
                PopupMenu popupMenu = new PopupMenu(ctw, user_photo);
                popupMenu.inflate(R.menu.popup_menu_camera_or_gallery);
                popupMenu.show();         //We need to Show it

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_gallery) {
                            fullPhotoUri = null;
                            CommonIntentsUtils.getImageFromGallery(listener.getActivity(),
                                    context.getResources().getString(R.string.Get_From_phone)
                                    , CommonIntentsUtils.REQUEST_CODE_GALLERY);

                        } else if (item.getItemId() == R.id.action_camera) {
                            fullPhotoUri = null;
                            File imageFile = null;
                            try {
                                imageFile = CommonIntentsUtils.createImageFile(listener.getActivity());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // to get file path isa. -> imageFile.getAbsolutePath();
                            fullPhotoUri = FileProvider.getUriForFile(
                                    context,
                                    "com.mohamed.mario.worker.fileprovider",
                                    imageFile);
                            CommonIntentsUtils.getImageFromCamera(listener.getActivity(), context.getResources().getString(R.string.Get_From_Camera)
                                    , fullPhotoUri, CommonIntentsUtils.REQUEST_CODE_CAMERA);
                        }

                        return true;
                    }
                });
            }

        });
        //endregion


        //region Button Clicks
        // oK Button
        customDialog.setOnPositiveClickLisner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //////////////////////////
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WORKER_DATABASE_NAME);
                //////////////////
                if (fullPhotoUri == null) {
                    //We Will Put Default Photo TODO
                    fullPhotoUri=Uri.parse("def");
                }
                if (TextUtils.isEmpty(ebt_username.getText().toString()) || ebt_username.getText().length() < 5) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_youname));
                } else if (TextUtils.isEmpty(ebt_phone.getText().toString()) || ebt_phone.getText().length() != 11) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_yourphone));
                } else if (TextUtils.isEmpty(ebt_password.getText().toString()) || ebt_password.getText().length() < 8) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_yourpassword));

                } else if (TextUtils.isEmpty(ebt_password.getText().toString()) || ebt_password.getText().length() < 8) {
                    listener.showToast(context.getResources().getString(R.string.You_Must_yourpassword));

                } else {
                    ////////////////////////////Check If Same user in database
                    Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(ebt_phone.getText().toString());
                    phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // If user added no need to add more we will print message
                            List<Worker> users_list = new ArrayList<>();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                users_list.add(singleSnapshot.getValue(Worker.class));
                            }
                            if (users_list.size() == 0) {
                                customDialog.getDialog_postive_button().setClickable(false);
                                customDialog.getDialog_negative_button().setClickable(false);
                                writeNewWorker(ebt_username.getText().toString(),
                                        ebt_password.getText().toString(),ebt_phone.getText().toString()
                                        , "", fullPhotoUri.toString(), String.valueOf(profession[0]),
                                        ""
                                        , null, null, 0, 0, frame_loading, customDialog);
                                listener.showToast(context.getResources().getString(R.string.successMessage));
                                listener.loginToStartActivity(false);
                                customDialog.dismiss();
                             /*  customDialog.dismiss();
                                frame_loading.setVisibility(View.GONE);*/
                            } else {
                                listener.showToast(context.getResources().getString(R.string.this_phone_has_registerd));

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Mario", "onCancelled", databaseError.toException());
                        }
                    });
                    //////////////////////////


                }
            }
        });

        customDialog.setOnCancelClickLisner(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
//endregion


    }
    //endregion

    //For FirebaseAddUseres
    //region FireBase Add Useres , Workers
    private void writeNewWorker(
            String name, String password, String phone, String location,
            String personalImage, String profession, String description,
            ArrayList<String> workImages, Review review, float rate, int ViewedBy
            , FrameLayout frame_loading, CustomDialog customDialog) {

        final Worker worker = new Worker(name, password, phone, location, personalImage,
                profession, description
                , workImages, review, rate, ViewedBy);
        frame_loading.setVisibility(View.VISIBLE);
        //region Save Image
        imageRef = storage.getReference().child(WORKERES_DATABASE_PHOTO_LOCATION
                + phone + WORKERES_DATABASE_PHOTO_NAME);
        Uri file = Uri.parse(personalImage);
        //    Uri file = Uri.parse(personalImage);
        //here the image will uploaded
        UploadTask uploadTask = imageRef.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    worker.setPersonalImage(task.getResult().toString());
                    //UPload USer
                    mDatabase.child(WORKER_DATABASE_NAME).child(phone).setValue(worker);

                    customDialog.dismiss();
                } else {
                    mDatabase.child(WORKER_DATABASE_NAME).child(phone).setValue(worker);

                    customDialog.dismiss();

                }
            }
        });


        //endregion

        //Getting the loaction and save it we will save loaction in Path  path/to/geofire so that we
        //Query it we will use the Path
        //region Save Location
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DatabaseUtils.WORKERS_DATABASE_LOCATION_WORKER_PATH);
        final GeoFire geoFire = new GeoFire(ref);
        if (ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(listener.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            geoFire.setLocation(phone, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                    } else {
                                    }
                                }
                            });
                        } else {
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        //endregion

    }

    private void writeNewUser(String name, final String phone, String password, String personalImage, ArrayList<Worker> workersRated,
                              ArrayList<Worker> workersReviewed, ArrayList<Worker>
                                      lastFiveWorkersVisitedByThisUser, String location,
                              FrameLayout frame_loading, CustomDialog customDialog) {


        final User user = new User(name, phone, password, personalImage, workersRated, workersReviewed
                , lastFiveWorkersVisitedByThisUser, location);


        frame_loading.setVisibility(View.VISIBLE);
        //region Save Image
        imageRef = storage.getReference().child(USERES_DATABASE_PHOTO_LOCATION + phone + USERES_DATABASE_PHOTO_NAME);
        Uri file = Uri.parse(personalImage);
        //    Uri file = Uri.parse(personalImage);
        //here the image will uploaded
        UploadTask uploadTask = imageRef.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    user.setPersonalImage(task.getResult().toString());
                    //UPload USer
                    mDatabase.child(USERES_DATABASE_NAME).child(phone).setValue(user);

                    customDialog.dismiss();
                } else {
                    mDatabase.child(USERES_DATABASE_NAME).child(phone).setValue(user);

                    customDialog.dismiss();

                }
            }
        });


        //endregion


        //Getting the loaction and save it we will save loaction in Path  path/to/geofire so that we
        //Query it we will use the Path
        //region Save Location
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USERES_DATABASE_LOCATION_USER_PATH);
        final GeoFire geoFire = new GeoFire(ref);
        if (ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(listener.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            geoFire.setLocation(phone, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {
                                    if (error != null) {
                                    } else {
                                    }
                                }
                            });
                        } else {
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        //endregion



    }
    //endregion



    // ----- View Model Interface

    public interface Listener {

        // User --> True, Worker --> False
        void loginToStartActivity(boolean isUserWorker);

        Activity getActivity();

        void showToast(String s);

    }

}
