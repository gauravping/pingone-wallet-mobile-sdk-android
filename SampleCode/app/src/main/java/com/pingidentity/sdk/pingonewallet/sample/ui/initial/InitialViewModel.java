package com.pingidentity.sdk.pingonewallet.sample.ui.initial;

import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;

public class InitialViewModel extends BaseViewModel {

    public InitialViewModel(DataRepository dataManager) {
        super(dataManager);
    }

    public Profile getProfile() {
        return getDataManager().getProfile();
    }
}
