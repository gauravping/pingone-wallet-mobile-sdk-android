package com.pingidentity.sdk.pingonewallet.sample.di.component;

import dagger.Component;
import io.reactivex.rxjava3.annotations.Nullable;

import com.pingidentity.sdk.pingonewallet.sample.MainApplication;
import com.pingidentity.sdk.pingonewallet.sample.di.module.PingOneWalletHelperModule;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PingOneWalletHelperModule.class})
public interface AppComponent {

    void inject(MainApplication application);

    @Nullable PingOneWalletHelper getPingOneWalletHelper();

    void setPingOneWalletHelper(PingOneWalletHelper helper);

}
