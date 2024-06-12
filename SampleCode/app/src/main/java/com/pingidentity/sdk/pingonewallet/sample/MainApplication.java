package com.pingidentity.sdk.pingonewallet.sample;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.sdk.pingonewallet.sample.di.Injector;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;

public class MainApplication extends Application {

    private final MutableLiveData<String> receivedUrl = new MutableLiveData<>();

    public void initDagger(PingOneWalletHelper helper) {
        Injector.initializeAppComponent(helper);
        Injector.getAppComponent().inject(this);
    }

    public void setUrl(String template) {
        receivedUrl.postValue(template);
    }

    public LiveData<String> getUrl() {
        return receivedUrl;
    }

}
