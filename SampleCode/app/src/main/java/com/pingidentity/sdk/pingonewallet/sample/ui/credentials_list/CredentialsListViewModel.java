package com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.network.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.util.List;

public class CredentialsListViewModel extends BaseViewModel {

    public static final String URL_HTTP = "http";
    public static final String URL_OPENID_VC = "openid-vc";

    public CredentialsListViewModel(DataRepository dataManager) {
        super(dataManager);
    }

    public LiveData<List<Claim>> getClaims() {
        return getDataManager().subscribeClaimsChange();
    }

    public void resetClaimsLiveData() {
        getDataManager().subscribeClaimsChange().postValue(null);
    }

    public boolean isClaimRevoked(@NonNull final String claimId) {
        return getDataManager().isClaimRevoked(claimId);
    }

    public Profile getProfile(){
        return getDataManager().getProfile();
    }

    public void processUrl(String url) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> {
            PingOneWalletHelper.getInstance().processQrContent(url);
        });
    }

    public void deleteClaim(Claim claim){
        getDataManager().deleteClaim(claim);
        PingOneWalletHelper.getInstance().reportCredentialDeletion(claim);
    }

}