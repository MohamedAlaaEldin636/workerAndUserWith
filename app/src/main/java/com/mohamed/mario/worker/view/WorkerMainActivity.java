package com.mohamed.mario.worker.view;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityWorkermainBinding;
import com.mohamed.mario.worker.databinding.ActivityWorkermainV2Binding;
import com.mohamed.mario.worker.utils.DatabaseUtils;
import com.mohamed.mario.worker.view.adapters.WorkerAdapterListItem;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewmodel.WorkerMainActivityViewModel;

import java.util.ArrayList;
import java.util.List;

import static com.mohamed.mario.worker.utils.DatabaseUtils.RADIOUS_KILLOSMETERS;

public class WorkerMainActivity extends AppCompatActivity implements WorkerAdapterListItem.ListItemClick {

    private ActivityWorkermainV2Binding binding;
    private WorkerMainActivityViewModel viewModel;

    private List<String> keysList;

    //Loaction
    private FusedLocationProviderClient mFusedLocationClient;

    //For Loaction Permission
    private static final int LOCATION_PERMISSON_INT = 100;
    //For Toast
    Toast msToast;
    //Adapter
    WorkerAdapterListItem adapterListItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_workermain_v2);




        // --- Setup recyclerView
        setupUIElements();
        setupRecyclerView();
        // -- Fetch Data from firebase to RC
        fetchDataFromFirebase();
    }

    private void setupUIElements() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchDataFromFirebase);
    }

    private void setupRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2,
                LinearLayoutManager.VERTICAL, false);
        adapterListItem=new WorkerAdapterListItem(null,this,this);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapterListItem);


    }

    private void fetchDataFromFirebase() {
        keysList = new ArrayList<>();

        binding.swipeRefreshLayout.setRefreshing(true);

        adapterListItem.setToEmptyList();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
        //region GetLoactopn
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DatabaseUtils.WORKERS_DATABASE_LOCATION_WORKER_PATH);
        final GeoFire geoFire = new GeoFire(ref);
        if (ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplication().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.e("1","1");

                            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(),
                                    location.getLongitude()), RADIOUS_KILLOSMETERS);

                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    Log.e("1","2");
                                    keysList.add(key);
                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {
                                    binding.swipeRefreshLayout.setRefreshing(true);
                                    adapterListItem.setToEmptyList();
                                }

                                @Override
                                public void onGeoQueryReady() {
                                    Log.e("1","3");
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                    adapterListItem.Swapadapter(keysList);
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {

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


    public void showToast(String text) {
        if (msToast != null) {
            msToast.cancel();
        }
        msToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        msToast.show();

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
                    ActivityCompat.requestPermissions(WorkerMainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
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
                    showToast(WorkerMainActivity.this.getResources().getString(R.string.succesful_loaction));
                } else {
                    showToast(WorkerMainActivity.this.getResources().getString(R.string.no_loaction_user));
                }
                return;
            }

        }
    }

    @Override
    public void OnItemClick(int Postion) {

    }
//endregion
}
