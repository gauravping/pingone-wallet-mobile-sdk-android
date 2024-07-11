package com.pingidentity.sdk.pingonewallet.sample.ui.picker.default_impl;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.CredentialPickerListener;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;

import java.util.List;
import java.util.function.Consumer;

class DefaultCredentialPickerListener implements CredentialPickerListener {

    CredentialsPresentation result;
    Consumer<CredentialsPresentation> onEvent;

    public DefaultCredentialPickerListener(PresentationRequest presentationRequest, Consumer<CredentialsPresentation> onEvent) {
        this.result = new CredentialsPresentation(presentationRequest);
        this.onEvent = onEvent;
    }

    public void onCredentialPicked(Claim claim, List<String> keys) {
        result.addClaimForKeys(keys, claim);
    }

    public void onPickerComplete() {
        onEvent.accept(result);
    }

    public void onPickerCanceled() {
        onEvent.accept(null);
    }

}
