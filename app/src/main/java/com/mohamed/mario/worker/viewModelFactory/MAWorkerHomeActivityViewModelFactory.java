package com.mohamed.mario.worker.viewModelFactory;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.mohamed.mario.worker.viewModelMa.MAWorkerHomeActivityViewModel;

/**
 * Created by Mohamed on 8/29/2018.
 *
 */
public class MAWorkerHomeActivityViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private Application application;
    private MAWorkerHomeActivityViewModel.Listener listener;

    public MAWorkerHomeActivityViewModelFactory(@NonNull Application application,
                                                MAWorkerHomeActivityViewModel.Listener listener) {
        super(application);

        this.application = application;
        this.listener = listener;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MAWorkerHomeActivityViewModel viewModel = new MAWorkerHomeActivityViewModel(
                application, listener);

        return modelClass.cast(viewModel);
    }
}
