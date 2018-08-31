package com.mohamed.mario.worker.viewMA;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mohamed.mario.worker.BaseApplication;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ActivityMaWorkerHomeActivityBinding;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.view.dialogs.CustomDialog;
import com.mohamed.mario.worker.viewMA.adapters.MAWorkerRCAdapter;
import com.mohamed.mario.worker.viewModelFactory.MAWorkerHomeActivityViewModelFactory;
import com.mohamed.mario.worker.viewModelMa.MAWorkerHomeActivityViewModel;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Mohamed on 8/29/2018.
 *
 */
public class MAWorkerHomeActivity extends AppCompatActivity
        implements MAWorkerHomeActivityViewModel.Listener ,
        MAWorkerRCAdapter.Listener {

    // --- Constants

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 8217;

    // --- Private Variables

    private ActivityMaWorkerHomeActivityBinding binding;

    private MAWorkerHomeActivityViewModel viewModel;

    private MAWorkerRCAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_ma_worker_home_activity);

        // -- init view model
        MAWorkerHomeActivityViewModelFactory factory = new MAWorkerHomeActivityViewModelFactory(
                getApplication(), this);
        viewModel = ViewModelProviders.of(this, factory)
                .get(MAWorkerHomeActivityViewModel.class);

        // -- Setup recyclerView
        setupUIElements();

        // -- check permission and after granting it isa, re-load data.
        checkLocationPermissionAndRequestItIfNotGrantedElseMakeAction();
    }

    // ---- Inside onCreate();

    private void setupUIElements() {
        // -- Swipe Refresh Layout
        binding.swipeRefreshLayout.setOnRefreshListener(
                this::fetchDataFromFirebase);

        // -- Recycler View
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2,
                LinearLayoutManager.VERTICAL, false);
        adapter = new MAWorkerRCAdapter(this, this);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.post(() -> {
            int recyclerViewHeight = binding.recyclerView.getHeight();
            adapter.setItemHeightFrom(recyclerViewHeight);
        });
        binding.recyclerView.setAdapter(adapter);

        // -- Profile Image Click
        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(MAWorkerHomeActivity.this, MAWorkerProfileActivity.class);
            intent.putExtra(MAWorkerProfileActivity.INTENT_KEY_WORKER_OBJECT, viewModel.getOwnWorker());

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void checkLocationPermissionAndRequestItIfNotGrantedElseMakeAction() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            /* Permission is not granted, show an explanation dialog even if it is not asked before
                    since audience might not understand english dialog. */

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
                ActivityCompat.requestPermissions(MAWorkerHomeActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

                customDialog.dismiss();
            });
        }else {
            // -- Get list of workers/users nearby

            /* Note setting setRefreshing to tru from code won't trigger the listener,
                so we call it manually as well */
            binding.swipeRefreshLayout.setRefreshing(true);
            fetchDataFromFirebase();
        }
    }

    // ---- Private Methods

    private void fetchDataFromFirebase() {
        // -- Re-check granting permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            checkLocationPermissionAndRequestItIfNotGrantedElseMakeAction();

            return;
        }

        // -- To ensure empty rc while loading
        adapter.swapToEmptyList();

        /* Flow
        1- get your own location.

        2- get based on your location by geoFire, list of keys of workers/users in specified radius.

        3- for loop in every key to retuen hashMap of <String key, Worker worker>

        4- give that to adapter and close the loading thing

        5- if a change occurred to the nearby phones, iterate through that change
                and if it is in the map update it , add it or remove it according to
                the response isa. see geoFire github isa.

        6- listen to children changes and update it isa, but delete listener
                in case a child is deleted by geofire isa, or listen once is better isa.

        */
        viewModel.makeFullGeoFireQuery();
    }

    // ---- Overriding onRequestPermissionsResult() -> location permission

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        BaseApplication baseApplication = ((BaseApplication) getApplicationContext());

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    baseApplication.showToast(
                            getString(R.string.succesful_loaction));

                    // -- Get list of workers/users nearby

                    /* Note setting setRefreshing to tru from code won't trigger the listener,
                        so we call it manually as well */
                    binding.swipeRefreshLayout.setRefreshing(true);
                    fetchDataFromFirebase();
                }else {
                    baseApplication.showToast(
                            getString(R.string.no_loaction_user));
                }
            }
        }
    }

    // ---- View Model Listener Implementation

    @Override
    public void swapRCAdapterWithoutClearingRefresh(@Nullable List<String> toBeDeletedKeysList) {
        adapter.deleteWithKeysListWithoutNotifyChanges(toBeDeletedKeysList);
    }

    @Override
    public void clearRefresh() {
        binding.swipeRefreshLayout.setRefreshing(false);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void swapRCAdapterAndMaybeClearRefresh(@Nullable HashMap<String, Worker> toBeAddedMap,
                                                  boolean clearRefresh) {
        // -- Swipe refresh layout refresh
        binding.swipeRefreshLayout.setRefreshing(! clearRefresh);

        // -- swap data to adapter
        adapter.addWorkersListAsMap(toBeAddedMap, clearRefresh);
    }

    @Override
    public void errorOccurred() {
        ((BaseApplication) getApplication())
                .showToast(getString(R.string.try_again_and_check_internet_connection));

        binding.swipeRefreshLayout.setRefreshing(false);
    }

    // ---- RC Adapter Listener Implementation

    @Override
    public void onItemClick(Worker worker) {
        // todo go to detail of another worker isa.
    }

    @Override
    public void changeEmptyViewVisibility(boolean showEmptyView) {
        if (showEmptyView && ! binding.swipeRefreshLayout.isRefreshing()){
            binding.emptyViewTextView.setVisibility(View.VISIBLE);
        }else if (! showEmptyView){
            binding.emptyViewTextView.setVisibility(View.GONE);
        }
    }

}