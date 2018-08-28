package com.mohamed.mario.worker.viewModelFactory;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.mohamed.mario.worker.viewModelMa.MALoginActivityViewModel;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MALoginActivityViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private Application application;
    private MALoginActivityViewModel.Listener listener;

    public MALoginActivityViewModelFactory(@NonNull Application application,
                                           MALoginActivityViewModel.Listener listener) {
        super(application);

        this.application = application;
        this.listener = listener;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MALoginActivityViewModel viewModel = new MALoginActivityViewModel(
                application, listener);

        return modelClass.cast(viewModel);
    }
}
