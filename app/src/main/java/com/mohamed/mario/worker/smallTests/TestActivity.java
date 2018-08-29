package com.mohamed.mario.worker.smallTests;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.Worker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import timber.log.Timber;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

public class TestActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // init views
        textView = findViewById(R.id.textView);

        // start get data from firebase
        loadDataFromFirebaseIsa();
    }

    private void loadDataFromFirebaseIsa() {
        final String phone1 = "01119884433";
        final String phone2 = "01111223344";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WORKER_DATABASE_NAME);
        Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(phone1);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If user added no need to add more we will print message
                List<Worker> workersList = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    workersList.add(singleSnapshot.getValue(Worker.class));
                }

                String valueMsg = "List size -> " + workersList.size();

                textView.setText(valueMsg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.v("onCancelled -> " + databaseError.toException().getMessage());

                textView.setText("Error occurred -> check logCat");
            }
        });
    }


    private void anyThing(){
        HashMap<String , Worker> map = new HashMap<>();
        List<String> keysList = new ArrayList<>();
        Set<String> keys = map.keySet();
        keysList.addAll(keys);
        for (int i=0; i<keys.size(); i++){

        }
    }
}
