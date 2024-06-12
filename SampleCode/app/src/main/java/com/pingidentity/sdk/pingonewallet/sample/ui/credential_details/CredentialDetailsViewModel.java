package com.pingidentity.sdk.pingonewallet.sample.ui.credential_details;

import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseViewModel;

public class CredentialDetailsViewModel extends BaseViewModel {

    private Credential credential;
    private CredentialDetailsFragment.CredentialDetailsAction action;

    public CredentialDetailsViewModel(PingOneWalletHelper pingOneWalletHelper) {
        super(pingOneWalletHelper);
    }

    public void setup(Credential credential, CredentialDetailsFragment.CredentialDetailsAction action) {
        this.credential = credential;
        this.action = action;
    }

    private void goBack() {
        navigateBack();
    }

    public void performAction() {
        switch (action) {
            case ACCEPT:
                getDataManager().saveCredential(credential.getClaim());
                break;
            case DELETE:
                getDataManager().deleteCredential(credential.getClaim().getId().toString());
                getPingOneWalletHelper().reportCredentialDeletion(credential.getClaim());
                break;
        }
        goBack();
    }

}
