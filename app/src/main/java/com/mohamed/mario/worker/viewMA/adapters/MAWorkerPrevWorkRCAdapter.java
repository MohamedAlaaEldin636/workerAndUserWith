package com.mohamed.mario.worker.viewMA.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.databinding.ItemMaWorkerProfilePreviousWorkImagesBinding;
import com.mohamed.mario.worker.utils.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mohamed on 8/31/2018.
 *
 */
public class MAWorkerPrevWorkRCAdapter
        extends RecyclerView.Adapter<MAWorkerPrevWorkRCAdapter.CustomViewHolder> {

    private static final int FIXED_ITEM_COUNT = 5;

    private Listener listener;
    private ArrayList<String> previousWorkImageList;

    public MAWorkerPrevWorkRCAdapter(Listener listener,
                                     ArrayList<String> previousWorkImageList) {
        this.listener = listener;

        // -- size must be isa == FIXED_ITEM_COUNT.
        if (previousWorkImageList != null){
            this.previousWorkImageList = previousWorkImageList;

            if (this.previousWorkImageList.size() < FIXED_ITEM_COUNT){
                for (int i=this.previousWorkImageList.size(); i<FIXED_ITEM_COUNT; i++){
                    this.previousWorkImageList.add("def");
                }
            }
        }else {
            this.previousWorkImageList = new ArrayList<>();

            for (int i=0; i<FIXED_ITEM_COUNT; i++){
                this.previousWorkImageList.add("def");
            }
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        ItemMaWorkerProfilePreviousWorkImagesBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_ma_worker_profile_previous_work_images, parent, false);

        return new CustomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        final int index = position;

        if (StringUtils.isNullOrEmpty(previousWorkImageList.get(position))
                || previousWorkImageList.get(position).equals("def")){
            // -- Show no image
            holder.binding.noImageLinearLayout.setVisibility(View.VISIBLE);

            // -- Both fab clicks -> camera and gallery
            holder.binding.cameraFab.setOnClickListener(v
                    -> listener.onCameraFabClick(index));
            holder.binding.galleryFab.setOnClickListener(v
                    -> listener.onGalleryFabClick(index));
        }else {
            // -- Hide no image
            holder.binding.noImageLinearLayout.setVisibility(View.GONE);

            // -- Get image url, then set it into imageVIew
            final String imageUrl = previousWorkImageList.get(position);

            Picasso.get()
                    .load(StringUtils.isNullOrEmpty(imageUrl) ? "def" : imageUrl)
                    .placeholder(R.drawable.workerbanner)
                    .into(holder.binding.imageView);

            // -- Set click to the imageView
            holder.binding.imageView.setOnClickListener(v
                    -> listener.onImageViewClick(imageUrl, index));
        }
    }

    @Override
    public int getItemCount() {
        return FIXED_ITEM_COUNT;
    }

    // ---- Public Methods

    public void swapWholeImageUrlList(ArrayList<String> previousWorkImageList){
        // -- size must be isa == FIXED_ITEM_COUNT.
        if (previousWorkImageList != null){
            this.previousWorkImageList = previousWorkImageList;

            if (this.previousWorkImageList.size() < FIXED_ITEM_COUNT){
                for (int i=this.previousWorkImageList.size(); i<FIXED_ITEM_COUNT; i++){
                    this.previousWorkImageList.add("def");
                }
            }
        }else {
            this.previousWorkImageList = new ArrayList<>();

            for (int i=0; i<FIXED_ITEM_COUNT; i++){
                this.previousWorkImageList.add("def");
            }
        }

        notifyDataSetChanged();
    }

    public void swapSingleImageUrl(String imageUrl, int position){
        previousWorkImageList.remove(position);

        previousWorkImageList.add(position, imageUrl);

        notifyDataSetChanged();
    }

    public ArrayList<String> getPreviousWorkImageList() {
        return previousWorkImageList;
    }

    // ----- View Holder

    class CustomViewHolder extends RecyclerView.ViewHolder {

        final ItemMaWorkerProfilePreviousWorkImagesBinding binding;

        CustomViewHolder(ItemMaWorkerProfilePreviousWorkImagesBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }

    // ----- Listener Interface

    public interface Listener {

        void onImageViewClick(String imageUrl, int position);

        void onCameraFabClick(int position);

        void onGalleryFabClick(int position);

    }

}
