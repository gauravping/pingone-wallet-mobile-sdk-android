package com.pingidentity.sdk.pingonewallet.sample.ui.home;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.util.List;

public class HomeViewModel extends BaseViewModel {

    public HomeViewModel(PingOneWalletHelper pingOneWalletHelper) {
        super(pingOneWalletHelper);
        pingOneWalletHelper.pollForMessages();
    }

    public MutableLiveData<List<Claim>> getClaims() {
        return getDataManager().subscribeCredentialsChange();
    }

    public void pollForMessages(){
        getPingOneWalletHelper().pollForMessages();
    }

    public void stopPolling(){
        getPingOneWalletHelper().stopPolling();
    }

    public boolean isClaimRevoked(@NonNull final String claimId) {
        return getDataManager().isCredentialRevoked(claimId);
    }

    public void processUrl(String url) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                getPingOneWalletHelper().processPingOneRequest(url));
    }

    public void goToQrScannerClick() {
        navigate(HomeFragmentDirections.actionHomeFragmentToQrScannerFragment());
    }

    public void goToQrDetails(Credential credential) {
        navigate(HomeFragmentDirections.actionHomeFragmentToCredentialDetailsFragment(credential));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getPingOneWalletHelper().stopPolling();
    }
}