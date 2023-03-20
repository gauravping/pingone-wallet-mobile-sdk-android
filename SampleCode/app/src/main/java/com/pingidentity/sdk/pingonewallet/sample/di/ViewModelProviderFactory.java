package com.pingidentity.sdk.pingonewallet.sample.di;

import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Singleton;

@Singleton
public class ViewModelProviderFactory<T extends ViewModel> extends ViewModelProvider.NewInstanceFactory implements ViewModelProvider.Factory {

    private final Class<T> viewModelClass;
    private final Supplier<T> viewModelSupplier;

    public ViewModelProviderFactory(Class<T> viewModelClass, Supplier<T> viewModelSupplier) {
        this.viewModelClass = viewModelClass;
        this.viewModelSupplier = viewModelSupplier;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <S extends ViewModel> S create(@NonNull Class<S> modelClass) {
        if (modelClass.isAssignableFrom(viewModelClass)) {
            return (S) viewModelSupplier.get();
        } else {
            throw new IllegalArgumentException("Unknown Class name " + viewModelClass.getName());
        }
    }
}