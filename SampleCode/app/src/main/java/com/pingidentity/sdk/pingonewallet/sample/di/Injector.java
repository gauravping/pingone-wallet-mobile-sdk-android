package com.pingidentity.sdk.pingonewallet.sample.di;

import com.pingidentity.sdk.pingonewallet.sample.di.component.AppComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.component.DaggerAppComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.module.AppModule;

public class Injector {

    private static AppComponent mAppComponent;

    private Injector() {

    }

    public static void initializeAppComponent() {
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule())
                .build();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

}
