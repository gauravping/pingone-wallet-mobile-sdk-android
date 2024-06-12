package com.pingidentity.sdk.pingonewallet.sample.di.module;

import androidx.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;

import javax.inject.Singleton;

@Module
public class PingOneWalletHelperModule {

    private final PingOneWalletHelper pingOneWalletHelper;


    public PingOneWalletHelperModule(PingOneWalletHelper helper) {
        this.pingOneWalletHelper = helper;
    }

    @Nullable
    @Provides
    @Singleton
    PingOneWalletHelper provideWalletHelper() {
        return pingOneWalletHelper;
    }

}
