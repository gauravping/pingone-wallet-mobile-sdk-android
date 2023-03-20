package com.pingidentity.sdk.pingonewallet.sample.ui.splash;

import androidx.lifecycle.MutableLiveData;

import com.pingidentity.sdk.pingonewallet.sample.network.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;
import com.pingidentity.sdk.pingonewallet.sample.utils.NotificationUtil;

public class SplashViewModel extends BaseViewModel {

    private final MutableLiveData<SplashState> mStateLiveData = new MutableLiveData<>();

    public SplashViewModel(DataRepository dataManager) {
        super(dataManager);
    }

    public void init(NotificationUtil notificationUtil) {
        PingOneWalletHelper.initialize(getDataManager(), notificationUtil, () -> {
            if (getDataManager().getProfile() != null) {
                mStateLiveData.postValue(SplashState.OPEN_MAIN);
            } else {
                mStateLiveData.postValue(SplashState.OPEN_INIT);
            }
        });
    }

    public MutableLiveData<SplashState> subscribeSplashState() {
        return mStateLiveData;
    }
}
