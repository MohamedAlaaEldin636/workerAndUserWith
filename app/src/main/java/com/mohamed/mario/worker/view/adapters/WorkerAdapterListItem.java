package com.mohamed.mario.worker.view.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.User;
import com.mohamed.mario.worker.model.Worker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.mohamed.mario.worker.utils.DatabaseUtils.USERES_DATABASE_QUERY_PHONE;
import static com.mohamed.mario.worker.utils.DatabaseUtils.WORKER_DATABASE_NAME;

public class WorkerAdapterListItem extends RecyclerView.Adapter<WorkerAdapterListItem.ViewHolder> {

    List<String> keysPhone;
    private ListItemClick listItemClick;
    Context context;

    public WorkerAdapterListItem(List<String> keysPhone, ListItemClick listItemClick, Context context) {
        this.keysPhone = keysPhone;
        this.listItemClick = listItemClick;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_main_worker, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(context==null)return;
        //Todo Setup Date Repeat
        GetDataFromQuery(keysPhone.get(position),holder);
    }

    @Override
    public int getItemCount() {
        return keysPhone == null ? 0 : keysPhone.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
             ImageView worker_image_list;
              TextView worker_name_list,worker_pro_list;
              RatingBar worker_id_ratingbar;
        public ViewHolder(View itemView) {
            super(itemView);
            worker_image_list=itemView.findViewById(R.id.worker_image_list);
            worker_name_list=itemView.findViewById(R.id.worker_name_list);
            worker_pro_list=itemView.findViewById(R.id.worker_pro_list);
            worker_id_ratingbar=itemView.findViewById(R.id.worker_id_ratingbar);

        }

    }


    public void GetDataFromQuery(String Phone, ViewHolder holder){
        //////////////////////////
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(WORKER_DATABASE_NAME);
        ////////////////////////////Check If Same user in database
        Query phoneQuery = ref.orderByChild(USERES_DATABASE_QUERY_PHONE).equalTo(Phone);
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If user added no need to add more we will print message
                List<Worker> users_list = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    users_list.add(singleSnapshot.getValue(Worker.class));
                }
                Worker worker=users_list.get(0);
                Picasso.get().load(worker.getPersonalImage()).into(holder.worker_image_list);
                holder.worker_name_list.setText(worker.getName());
                String profession=context.getResources().getStringArray(R.array.professions_list)
                        [Integer.valueOf(worker.getProfession())];
                holder.worker_pro_list.setText(profession);
                holder.worker_id_ratingbar.setRating(worker.getRate());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Mario", "onCancelled", databaseError.toException());
            }
        });
        //////////////////////////
    }



    public void Swapadapter(List<String> keysPhone) {
        if (keysPhone != null) {

            this.keysPhone = keysPhone;
            notifyDataSetChanged();

        }
    }
//setToEmptyList

    public void setToEmptyList() {

            this.keysPhone = null;
            notifyDataSetChanged();


    }


    public interface ListItemClick {
        void OnItemClick(int Postion);
    }


}
