package com.pingidentity.sdk.pingonewallet.sample.ui.base;

import androidx.lifecycle.ViewModel;

import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;

public abstract class BaseViewModel extends ViewModel {

    private final DataRepository mDataManager;

    protected BaseViewModel(DataRepository dataManager) {
        this.mDataManager = dataManager;
    }

    public DataRepository getDataManager() {
        return mDataManager;
    }

}
