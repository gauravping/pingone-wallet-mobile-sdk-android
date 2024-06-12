package com.pingidentity.sdk.pingonewallet.sample.di;

import com.pingidentity.sdk.pingonewallet.sample.di.component.AppComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.component.DaggerAppComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.component.DaggerAppComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.module.PingOneWalletHelperModule;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;

public class Injector {

    private static AppComponent mAppComponent;

    private Injector() {

    }

    public static void initializeAppComponent(PingOneWalletHelper helper) {
        mAppComponent = DaggerAppComponent.builder()
                .pingOneWalletHelperModule(new PingOneWalletHelperModule(helper))
                .build();
    }

    public static AppComponent getAppComponent() {
        return mAppComponent;
    }

}
