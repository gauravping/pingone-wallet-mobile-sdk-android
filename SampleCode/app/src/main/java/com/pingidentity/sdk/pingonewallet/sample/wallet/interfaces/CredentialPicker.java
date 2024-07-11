package com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces;

import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;

import java.util.List;
import java.util.function.Consumer;

public interface CredentialPicker {

    void selectCredentialFor(PresentationRequest presentationRequest, List<CredentialMatcherResult> credentialMatcherResults, Consumer<CredentialsPresentation> onResult);

}
