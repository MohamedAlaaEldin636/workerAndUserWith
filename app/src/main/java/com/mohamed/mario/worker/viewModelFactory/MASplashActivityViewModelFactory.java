package com.mohamed.mario.worker.viewModelFactory;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.mohamed.mario.worker.viewModelMa.MASplashActivityViewModel;

/**
 * Created by Mohamed on 8/28/2018.
 *
 */
public class MASplashActivityViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private Application application;
    private MASplashActivityViewModel.Listener listener;

    public MASplashActivityViewModelFactory(@NonNull Application application,
                                            MASplashActivityViewModel.Listener listener) {
        super(application);

        this.application = application;
        this.listener = listener;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        MASplashActivityViewModel viewModel = new MASplashActivityViewModel(
                application, listener);

        return modelClass.cast(viewModel);
    }
}
