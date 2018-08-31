package com.mohamed.mario.worker.viewModelFactory;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mohamed.mario.worker.model.Worker;
import com.mohamed.mario.worker.viewModelMa.MAWorkerHomeActivityViewModel;
import com.mohamed.mario.worker.viewModelMa.MAWorkerProfileActivityViewModel;

/**
 * Created by Mohamed on 8/30/2018.
 *
 */
public class MAWorkerProfileActivityViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private Application application;
    private MAWorkerProfileActivityViewModel.Listener listener;
    @Nullable private Worker worker;

    public MAWorkerProfileActivityViewModelFactory(@NonNull Application application,
                                                   MAWorkerProfileActivityViewModel.Listener listener,
                                                   @Nullable Worker worker) {
        super(application);

        this.application = application;
        this.listener = listener;
        this.worker = worker;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MAWorkerProfileActivityViewModel viewModel = new MAWorkerProfileActivityViewModel(
                application, listener, worker);

        return modelClass.cast(viewModel);
    }

}
