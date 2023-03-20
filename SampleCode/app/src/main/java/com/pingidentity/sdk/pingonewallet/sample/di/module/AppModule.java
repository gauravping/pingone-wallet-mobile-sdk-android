package com.pingidentity.sdk.pingonewallet.sample.di.module;

import dagger.Module;
import dagger.Provides;

import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepositoryImpl;
import com.pingidentity.sdk.pingonewallet.encrypted_storage.EncryptedStorageProvider;
import com.pingidentity.sdk.pingonewallet.encrypted_storage.EncryptedStorageProviderImpl;
import com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager.StorageManagerImpl;
import com.pingidentity.sdk.pingonewallet.sample.utils.JsonUtil;
import com.pingidentity.sdk.pingonewallet.contracts.StorageManagerContract;

import javax.inject.Singleton;

@Module
public class AppModule {

    @Provides
    @Singleton
    JsonUtil provideJsonUtil() {
        return new JsonUtil();
    }

    @Provides
    @Singleton
    EncryptedStorageProvider provideEncryptedStorageProvider() {
        return new EncryptedStorageProviderImpl();
    }

    @Provides
    @Singleton
    StorageManagerContract provideStorageManager() {
        return StorageManagerImpl.getInstance();
    }

    @Provides
    @Singleton
    DataRepository provideDataRepository(StorageManagerContract storageManager, JsonUtil jsonUtil) {
        return new DataRepositoryImpl(storageManager, jsonUtil);
    }

}
