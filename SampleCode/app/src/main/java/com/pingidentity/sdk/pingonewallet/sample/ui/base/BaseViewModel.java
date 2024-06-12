package com.pingidentity.sdk.pingonewallet.sample.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavDirections;

import com.pingidentity.sdk.pingonewallet.sample.models.navigation.Event;
import com.pingidentity.sdk.pingonewallet.sample.models.navigation.NavigationCommand;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;

import java.util.Optional;

public abstract class BaseViewModel extends ViewModel {

    private final PingOneWalletHelper pingOneWalletHelper;
    private final MutableLiveData<Event<NavigationCommand>> navigation = new MutableLiveData<>();

    protected BaseViewModel(PingOneWalletHelper helper) {
        this.pingOneWalletHelper = helper;
    }

    public Optional<PingOneWalletHelper> getPingOneWalletHelperOptional() {
        return Optional.of(pingOneWalletHelper);
    }

    public PingOneWalletHelper getPingOneWalletHelper() {
        return pingOneWalletHelper;
    }

    public DataRepository getDataManager() {
        return pingOneWalletHelper.getDataRepository();
    }

    public LiveData<Event<NavigationCommand>> getNavigation(){
        return navigation;
    }

    public void navigate(NavDirections navDirections) {
        navigation.postValue(new Event<>(new NavigationCommand.ToDirection(navDirections)));
    }

    public void navigateBack() {
        navigation.postValue(new Event<>(new NavigationCommand.Back()));
    }
}
