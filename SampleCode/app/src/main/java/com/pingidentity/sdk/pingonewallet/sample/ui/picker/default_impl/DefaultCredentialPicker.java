package com.pingidentity.sdk.pingonewallet.sample.ui.picker.default_impl;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.ApplicationUiHandler;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.CredentialPicker;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.CredentialPickerListener;
import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;

import java.util.List;
import java.util.function.Consumer;

public class DefaultCredentialPicker implements CredentialPicker {

    private final ApplicationUiHandler applicationUiCallbackHandler;

    private int nextCredentialIndex;
    private List<CredentialMatcherResult> credentialMatcherResults;

    public interface OnCredentialPicked {
        void onPicked(Claim claim);
    }

    public DefaultCredentialPicker(ApplicationUiHandler uiCallbackHandler) {
        this.applicationUiCallbackHandler = uiCallbackHandler;
    }

    @Override
    public void selectCredentialFor(PresentationRequest presentationRequest, List<CredentialMatcherResult> credentialMatcherResults, Consumer<CredentialsPresentation> onResult) {
        DefaultCredentialPickerListener listener = new DefaultCredentialPickerListener(presentationRequest, onResult);
        this.credentialMatcherResults = credentialMatcherResults;
        selectCredentialToPresent(0, listener);
    }

    private void selectCredentialToPresent(int index, CredentialPickerListener listener) {
        nextCredentialIndex = index + 1;

        if (index >= credentialMatcherResults.size()) {
            listener.onPickerComplete();
            return;
        }

        CredentialMatcherResult credentialMatcherResult = credentialMatcherResults.get(index);
        if (credentialMatcherResult.getClaims().isEmpty()) {
            selectCredentialToPresent(nextCredentialIndex, listener);
            return;
        }

        if (credentialMatcherResult.getClaims().size() == 1) {
            listener.onCredentialPicked(credentialMatcherResult.getClaims().get(0), credentialMatcherResult.getRequestedKeys());
            selectCredentialToPresent(nextCredentialIndex, listener);
        } else {
            selectCredentialForPresentation(credentialMatcherResult, listener);
        }
    }

    private void selectCredentialForPresentation(CredentialMatcherResult credentialMatcherResult, CredentialPickerListener listener) {
        applicationUiCallbackHandler.selectCredentialForPresentation(credentialMatcherResult.getClaims(), claim -> {
            if (claim == null) {
                listener.onPickerCanceled();
                return;
            }
            listener.onCredentialPicked(claim, credentialMatcherResult.getRequestedKeys());
            selectCredentialToPresent(nextCredentialIndex, listener);
        });
    }

}
