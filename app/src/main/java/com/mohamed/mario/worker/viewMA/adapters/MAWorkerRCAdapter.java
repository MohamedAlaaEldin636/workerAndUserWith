package com.mohamed.mario.worker.viewMA.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.utils.StringUtils;
import com.mohamed.mario.worker.utils.UnitsConversionsUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by Mohamed on 8/29/2018.
 *
 */
public class MAWorkerRCAdapter extends RecyclerView.Adapter<MAWorkerRCAdapter.CustomViewHolder> {

    private static final int DEFAULT_ITEM_HEIGHT_IN_DP = 280;

    private Context context;
    private Listener listener;
    private HashMap<String, Worker> workersListAsMap;

    private int itemHeight = 0;

    public MAWorkerRCAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        workersListAsMap = new HashMap<>();
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(
                R.layout.list_main_worker, parent, false);

        // Set item view height
        if (itemHeight == 0){
            itemHeight = UnitsConversionsUtils.dpToPx(DEFAULT_ITEM_HEIGHT_IN_DP, context);
        }
        view.getLayoutParams().height = itemHeight;

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Set<String> keysSet = workersListAsMap.keySet();
        List<String> keysList = new ArrayList<>();
        keysList.addAll(keysSet);

        final Worker worker = workersListAsMap.get(keysList.get(position));

        String personalImage = StringUtils.isNullOrEmpty(worker.getPersonalImage()) ?
                "def" : worker.getPersonalImage();
        Picasso.get().load(personalImage)
                .placeholder(R.drawable.workerbanner)
                .into(holder.workerImageView);

        holder.workerName.setText(worker.getName());

        String profession = context.getResources().getStringArray(
                R.array.professions_list)[Integer.valueOf(worker.getProfession())];
        holder.workerProfession.setText(profession);

        holder.workerRatingBar.setRating(worker.getRate());

        holder.itemView.setOnClickListener((v)
                -> listener.onItemClick(worker));
    }

    @Override
    public int getItemCount() {
        int itemCount = workersListAsMap.size();

        listener.changeEmptyViewVisibility(itemCount == 0);

        return itemCount;
    }

    // ---- Public Methods

    public void setItemHeightFrom(int recyclerViewHeight){
        if (recyclerViewHeight == 0){
            Timber.v("How come RC height is 0 !!!");

            return;
        }

        // -- Below approach to show isa only 2.25 items in one screen.
        int numberOfRows = 2;
        this.itemHeight =
                (int) ((recyclerViewHeight / numberOfRows) - (recyclerViewHeight * 0.25 * 0.25));

        notifyDataSetChanged();
    }

    public void swapToEmptyList(){
        // because we will re-fetch all of the data from internet isa.
        workersListAsMap = new HashMap<>();

        notifyDataSetChanged();
    }

    public void deleteWithKeysListWithoutNotifyChanges(@Nullable List<String> keysList){
        if (keysList == null || keysList.size() == 0){
            return;
        }

        for (String key : keysList){
            workersListAsMap.remove(key);
        }
    }

    public void addWorkersListAsMap(@Nullable HashMap<String , Worker> toBeAddedWorkersListAsMap,
                                    boolean notifyChanges){
        /* Note even if a mapped object exists it is ok, as it will be replaced by this
                    updated one isa. */
        workersListAsMap.putAll(toBeAddedWorkersListAsMap);

        if (notifyChanges){
            notifyDataSetChanged();
        }
    }

    // ----- View Holder

    class CustomViewHolder extends RecyclerView.ViewHolder {

        final ImageView workerImageView;
        final TextView workerName;
        final TextView workerProfession;
        final RatingBar workerRatingBar;

        CustomViewHolder(View itemView) {
            super(itemView);

            workerImageView = itemView.findViewById(R.id.worker_image_list);
            workerName = itemView.findViewById(R.id.worker_name_list);
            workerProfession = itemView.findViewById(R.id.worker_pro_list);
            workerRatingBar = itemView.findViewById(R.id.worker_id_ratingbar);
        }
    }

    // ----- Listener Interface

    public interface Listener {

        void onItemClick(Worker worker);

        void changeEmptyViewVisibility(boolean showEmptyView);

    }

}
