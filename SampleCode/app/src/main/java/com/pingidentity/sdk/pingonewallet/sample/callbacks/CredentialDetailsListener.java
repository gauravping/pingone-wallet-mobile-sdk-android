package com.pingidentity.sdk.pingonewallet.sample.callbacks;

import com.pingidentity.did.sdk.types.Claim;

public interface CredentialDetailsListener {

    void onActionClick(Claim claim);

    void onCancel();

}
