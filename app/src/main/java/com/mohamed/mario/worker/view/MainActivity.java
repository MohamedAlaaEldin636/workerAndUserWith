package com.mohamed.mario.worker.view;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mohamed.mario.worker.databinding.ActivityMainBinding;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.CommonIntentsUtils;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewmodel.MainActivityViewModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityViewModel.Listener {
    private static final int LOCATION_PERMISSON_INT = 100;
    //For Photo From Camera and Gallery
    //Uri viewModel.fullPhotoUri = null;
    //Toast
    Toast msToast;
    //Firebase
    //private DatabaseReference mDatabase;
    //Loaction
    //private FusedLocationProviderClient mFusedLocationClient;
    //Firebase Storage
    //private FirebaseStorage storage = FirebaseStorage.getInstance();
    //private StorageReference userImageRef;

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


    @Override
    public void launchActivity(boolean isUser) {
        if (isUser) {
            // todo
        } else {

        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }
/*

    public void userClick(View view) {
        final CustomDialog customDialog = new CustomDialog(this, R.layout.user_register,
                this.getResources().getString(R.string.registeruser), this.getResources().getString(R.string.data_enter)
                , this.getResources().getString(R.string.submit), this.getResources().getString(R.string.cancel));

        customDialog.show();


        View register_layout = customDialog.getRootView();
        final ImageView user_photo = register_layout.findViewById(R.id.imageView);
        final TextView txt_label = register_layout.findViewById(R.id.txt_label);
        final EditText ebt_username = register_layout.findViewById(R.id.ebt_username);
        final EditText ebt_phone = register_layout.findViewById(R.id.ebt_phone);
        final EditText ebt_password = register_layout.findViewById(R.id.ebt_password);
        // For Handel On Activity Result
        CommonIntentsUtils.layout = register_layout;
        CommonIntentsUtils.IMAGE_ID = user_photo.getId();
        CommonIntentsUtils.Txt_ID = txt_label.getId();

        //region user_photo click
        user_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ContextWrapper For Them Background To Style Of Popupmenu
                ContextThemeWrapper ctw = new ContextThemeWrapper(MainActivity.this, R.style.PopupMenu);
                PopupMenu popupMenu = new PopupMenu(ctw, user_photo);
                popupMenu.inflate(R.menu.popup_menu_camera_or_gallery);
                popupMenu.show();         //We need to Show it

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_gallery) {
                            viewModel.fullPhotoUri = null;
                            CommonIntentsUtils.getImageFromGallery(MainActivity.this,
                                    MainActivity.this.getResources().getString(R.string.Get_From_phone)
                                    , CommonIntentsUtils.REQUSER_CODE_Gallery);

                        } else if (item.getItemId() == R.id.action_camera) {
                            viewModel.fullPhotoUri = null;
                            File imageFile = null;
                            try {
                                imageFile = CommonIntentsUtils.createImageFile(MainActivity.this);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // to get file path isa. -> imageFile.getAbsolutePath();
                            viewModel.fullPhotoUri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    "com.mohamed.mario.worker.fileprovider",
                                    imageFile);
                            Log.d("MARIO", viewModel.fullPhotoUri.toString());
                            CommonIntentsUtils.getImageFromCamera(MainActivity.this, MainActivity.this.getResources().getString(R.string.Get_From_Camera)
                                    , viewModel.fullPhotoUri, CommonIntentsUtils.REQUSER_CODE_CAMERA);
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
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                //////////////////
                if (viewModel.fullPhotoUri == null) {
                    //We Will Put Default Photo TODO
                }
                if (TextUtils.isEmpty(ebt_username.getText().toString()) || ebt_username.getText().length() < 5) {
                    showToast(MainActivity.this.getResources().getString(R.string.You_Must_youname));
                } else if (TextUtils.isEmpty(ebt_phone.getText().toString()) || ebt_phone.getText().length() != 11) {
                    showToast(MainActivity.this.getResources().getString(R.string.You_Must_yourphone));
                } else if (TextUtils.isEmpty(ebt_password.getText().toString()) || ebt_password.getText().length() < 8) {
                    showToast(MainActivity.this.getResources().getString(R.string.You_Must_yourpassword));
                } else {
                    ////////////////////////////Check If Same user in database
                    Query phoneQuery = ref.orderByChild("phone").equalTo(ebt_phone.getText().toString());
                    phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<User> users_list = new ArrayList<>();
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                users_list.add(singleSnapshot.getValue(User.class));
                            }
                            if (users_list.size() == 0) {
                                writeNewUser(ebt_username.getText().toString(),
                                        ebt_phone.getText().toString(), ebt_password.getText().toString()
                                        , viewModel.fullPhotoUri.toString(), null, null, null, "");
                                showToast(MainActivity.this.getResources().getString(R.string.successMessage));
                                customDialog.dismiss();
                            } else {
                                showToast(MainActivity.this.getResources().getString(R.string.this_phone_has_registerd));

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
*/


    public void workerClick(View view) {


    }

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

    ///For Preparing Toast
    @Override
    public void showToast(String text) {
        if (msToast != null) {
            msToast.cancel();
        }
        msToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        msToast.show();

    }

//
//    //For FirebaseAddUseres
//    private void writeNewUser(String name, final String phone, String password, String personalImage, ArrayList<Worker> workersRated,
//                              ArrayList<Worker> workersReviewed, ArrayList<Worker> lastFiveWorkersVisitedByThisUser, String location) {
//        final User user = new User(name, phone, password, personalImage, workersRated, workersReviewed
//                , lastFiveWorkersVisitedByThisUser, location);
//
//        final String[] ImageUrl = new String[1];
//
//        //region Save Image
//        userImageRef = storage.getReference().child("users/Images/" + phone + "/image");
//        Uri file = Uri.parse(personalImage);
//        UploadTask uploadTask = userImageRef.putFile(file);
//        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if (!task.isSuccessful()) {
//                    throw task.getException();
//                }
//                return userImageRef.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                if (task.isSuccessful()) {
//                    Log.e("Imageurl", task.getResult().toString());
//                    ImageUrl[0] = task.getResult().toString();
//                    user.setPersonalImage(ImageUrl[0]);
//                    //UPload USer
//                    mDatabase.child("users").child(phone).setValue(user);
//                } else {
//                    // Handle failures
//                    // ...
//                }
//            }
//        });
//
//
//        //endregion
//
//
//        //Getting the loaction and save it we will save loaction in Path  path/to/geofire so that we
//        //Query it we will use the Path
//
//        //region Save Location
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Location/User/geofire");
//        final GeoFire geoFire = new GeoFire(ref);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//
//                            geoFire.setLocation(phone, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
//                                @Override
//                                public void onComplete(String key, DatabaseError error) {
//                                    if (error != null) {
//                                    } else {
//                                    }
//                                }
//                            });
//                        } else {
//
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//            }
//        });
//
//        //endregion
//
//
//    }


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
