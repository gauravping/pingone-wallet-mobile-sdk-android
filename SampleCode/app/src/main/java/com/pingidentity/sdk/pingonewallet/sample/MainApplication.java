package com.pingidentity.sdk.pingonewallet.sample;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.sdk.pingonewallet.sample.di.Injector;

public class MainApplication extends Application {

    private final MutableLiveData<String> receivedUrl = new MutableLiveData<>();

    public void initDagger() {
        Injector.initializeAppComponent();
        Injector.getAppComponent().inject(this);
    }

    public void setUrl(String template) {
        receivedUrl.postValue(template);
    }

    public LiveData<String> getUrl() {
        return receivedUrl;
    }

}
