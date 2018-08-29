package com.mohamed.mario.worker.viewModelMa;

import android.Manifest;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.RADIOUS_KILLOSMETERS;
import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

/**
 * Created by Mohamed on 8/29/2018.
 *
 */
public class MAWorkerHomeActivityViewModel extends AndroidViewModel {

    private Listener listener;

    /** This shouldn't be used except in the following methods only 1- */
    private int counterForEndRefresh;

    private GeoQuery geoQuery;

    public MAWorkerHomeActivityViewModel(@NonNull Application application, Listener listener) {
        super(application);

        this.listener = listener;
    }

    // ---- Public Methods

    /**
     * ==> Flow
     *
     * 1- get your own location.
     *
     * 2- get based on your location by geoFire, list of keys of workers/users in specified radius.
     *
     * 3- for loop in every key to return hashMap of <String key, Worker worker>
     *
     * 4- give that to adapter and close the loading thing
     *
     * 5- if a change occurred to the nearby phones, iterate through that change
     *      and in the adapter update it, add it or remove it according to th situation isa.
     */
    public void makeFullGeoFireQuery(){
        if (geoQuery != null){
            /* -- in case we made a refresh by swiping, so we will need only one listener
                    to stick around isa, in case of any gro change then the listener is notified
                    once no matter how many refreshes has been done isa. */
            geoQuery.removeAllListeners();
        }

        Context appContext = getApplication().getApplicationContext();

        // 1- Get current location
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(appContext);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(DatabaseUtils.WORKERS_DATABASE_LOCATION_WORKER_PATH);
        final GeoFire geoFire = new GeoFire(ref);
        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Task<Location> locationTask = fusedLocationClient.getLastLocation();

            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    geoQuery = geoFire.queryAtLocation(
                            new GeoLocation(location.getLatitude(), location.getLongitude()),
                            RADIOUS_KILLOSMETERS);

                    // 2- get nearby people
                    geoQuery.addGeoQueryEventListener(new CustomGeoQueryEventListener());
                }else {
                    Timber.v("Null current location !!");

                    listener.errorOccurred();
                }
            });

            locationTask.addOnFailureListener(e -> {
                Timber.v("Error while retrieving current location -> " + e.getMessage());

                listener.errorOccurred();
            });
        }
    }

    // ---- Private Methods

    private void loopThroughKeysAndGetCorrespondingWorkersList(@NonNull List<String> toBeAddedKeysList,
                                                               List<String> toBeDeletedKeysList){
        // 3- looping through keys, to get objects

        // -- see if there are deleted key in added one, and if so delete it from both isa.
        List<String> deleteFromBothListsList = new ArrayList<>();
        for (int i=0; i<toBeDeletedKeysList.size(); i++){
            String temp = toBeDeletedKeysList.get(i);

            if (toBeAddedKeysList.contains(temp)){
                deleteFromBothListsList.remove(temp);
            }
        }
        for (String temp : deleteFromBothListsList){
            toBeDeletedKeysList.remove(temp);
            toBeAddedKeysList.remove(temp);
        }

        // -- Note no need to get data for toBeDeleted Keys
        if (toBeDeletedKeysList.size() > 0){
            listener.swapRCAdapterWithoutClearingRefresh(toBeDeletedKeysList);
        }
        if (toBeAddedKeysList.size() == 0){
            listener.clearRefresh();

            Timber.v("There was no added geoFire locations nearby this device.");

            return;
        }

        // -- add counter value to ensure it is working correctly isa.
        counterForEndRefresh = toBeAddedKeysList.size();

        // -- Loop through the toBeAddedKeysList and continue with the rest of the flow isa.
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WORKER_DATABASE_NAME);
        for (String phone : toBeAddedKeysList){
            Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone);
            phoneQuery.addListenerForSingleValueEvent(workersValueEventListener);
        }
    }

    // ----- Helper Classes ( Implementing Listeners )

    private class CustomGeoQueryEventListener implements GeoQueryEventListener {

        private List<String> toBeAddedKeysList;
        private List<String> toBeDeletedKeysList;

        CustomGeoQueryEventListener() {
            toBeAddedKeysList = new ArrayList<>();

            toBeDeletedKeysList = new ArrayList<>();
        }

        @Override
        public void onKeyEntered(String key, GeoLocation location) {
            toBeAddedKeysList.add(key);

            Timber.v("ON GEO QUERY OF WORKERS LIST ==>>>> YESSSS I'M IN");
        }

        @Override
        public void onKeyExited(String key) {
            toBeDeletedKeysList.add(key);

            loopThroughKeysAndGetCorrespondingWorkersList(new ArrayList<>(), toBeDeletedKeysList);

            Timber.v("ON GEO QUERY OF WORKERS LIST ==>>>> EXITED");
        }

        @Override
        public void onKeyMoved(String key, GeoLocation location) {
            // We don't need this feature.


            Timber.v("ON GEO QUERY OF WORKERS LIST ==>>>> KINDA MOVED A LITTLE");
        }

        @Override
        public void onGeoQueryReady() {
            loopThroughKeysAndGetCorrespondingWorkersList(toBeAddedKeysList, toBeDeletedKeysList);

            /* -- clear both lists, so when a new member(s) enters/exits we will just append to
                    the adapter */
            toBeAddedKeysList = new ArrayList<>();
            toBeDeletedKeysList = new ArrayList<>();

            Timber.v("ON GEO QUERY OF WORKERS LIST READY");
        }

        @Override
        public void onGeoQueryError(DatabaseError error) {
            Timber.v("Error occurred while querying -> " + error.getMessage());

            listener.errorOccurred();
        }
    }

    // --- Private Listener Variables

    private ValueEventListener workersValueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            List<Worker> workersList = new ArrayList<>();
            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                workersList.add(singleSnapshot.getValue(Worker.class));
            }

            Worker worker = workersList.get(0);
            String key = worker.getPhone();

            HashMap<String , Worker> map = new HashMap<>();
            map.put(key, worker);

            counterForEndRefresh--;

            listener.swapRCAdapterAndMaybeClearRefresh(map, counterForEndRefresh == 0);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            /* Note do note make listener.errorOccurred()
                    since the case is only 1 worker error occurred. */

            Timber.v("Error occurred while getting one of the workers from firebase, " +
                    "to add to the adapter");

            counterForEndRefresh--;

            listener.swapRCAdapterAndMaybeClearRefresh(null, counterForEndRefresh == 0);
        }
    };

    // ----- Listener Interface

    public interface Listener {

        /**
         * @param toBeDeletedKeysList Whenever geoFire exists the area.
         */
        void swapRCAdapterWithoutClearingRefresh(@Nullable List<String> toBeDeletedKeysList);

        void clearRefresh();

        /**
         * @param toBeAddedMap Initial geofire results or whenever geoFire exists the area.
         */
        void swapRCAdapterAndMaybeClearRefresh(@Nullable HashMap<String , Worker> toBeAddedMap,
                                               boolean clearRefresh);

        /**
         * ==> Usage
         * 1- current location returned null
         * 2- error while querying geoFire nearby keys
         *
         * ==> Flow
         * 1- Note to reach here we already made the adapter is empty, so keep it empty.
         * 2- tell user via showToast to check connection and refresh again (by swiping from above).
         */
        void errorOccurred();

    }

}
