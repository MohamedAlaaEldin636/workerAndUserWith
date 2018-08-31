package com.mohamed.mario.worker.utils;

import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.mohamed.mario.worker.R;
import com.mohamed.mario.worker.viewMA.adapters.MAWorkerPrevWorkRCAdapter;
import com.mohamed.mario.worker.viewModelMa.MAWorkerProfileActivityViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed on 8/30/2018.
 *
 */
public class BindingAdapterUtils {

    // ---- General Usage

    @BindingAdapter("setImageByUrl")
    public static void setImageByUrl(ImageView imageView, String url){
        if (StringUtils.isNullOrEmpty(url)){
            url = "def";
        }

        Picasso.get()
                .load(url)
                .placeholder(R.drawable.workerbanner)
                .into(imageView);
    }

    @BindingAdapter("setTextChangedListenerToSetObservable")
    public static void setTextChangedListenerToSetObservable(TextView textView,
                                                             ObservableField<String> stringObservableField){
        textView.addTextChangedListener(
                new CustomTextWatcherListener(stringObservableField));
    }

    // ---- Specific Usage ( to this app )

    @BindingAdapter("setToolbarNavIconClickWithViewModelListenerCall")
    public static void setToolbarNavIconClickWithViewModelListenerCall(Toolbar toolbar,
                                                                       final MAWorkerProfileActivityViewModel viewModel){
        toolbar.setNavigationOnClickListener(v -> {
            MAWorkerProfileActivityViewModel.Listener listener = viewModel == null
                    ? null : viewModel.getListener();

            if (listener != null){
                listener.callOnBackPressed();
            }
        });
    }

    @BindingAdapter("setWorkImagesListInRCToCorrespondingAdapter")
    public static void setWorkImagesListInRCToCorrespondingAdapter(RecyclerView recyclerView,
                                                                   ArrayList<String> imagesList){
        RecyclerView.Adapter adapter = recyclerView.getAdapter();

        if (adapter != null && adapter instanceof MAWorkerPrevWorkRCAdapter){
            MAWorkerPrevWorkRCAdapter prevWorkAdapter = (MAWorkerPrevWorkRCAdapter) adapter;

            prevWorkAdapter.swapWholeImageUrlList(imagesList);
        }
    }

    // ----- Private Helper Classes ( for listeners )

    private static class CustomTextWatcherListener implements TextWatcher {

        private ObservableField<String> stringObservableField;

        CustomTextWatcherListener(ObservableField<String> stringObservableField) {
            this.stringObservableField = stringObservableField;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String textAfterChange = s.toString();

            stringObservableField.set(textAfterChange);
        }
    }

}
