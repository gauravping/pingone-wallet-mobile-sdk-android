package com.pingidentity.sdk.pingonewallet.sample.di.component;

import dagger.Component;

import com.pingidentity.sdk.pingonewallet.sample.MainApplication;
import com.pingidentity.sdk.pingonewallet.sample.di.module.AppModule;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;

import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MainApplication application);

    DataRepository getDataRepository();

}
