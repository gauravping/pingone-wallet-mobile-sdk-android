package com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces;

import com.pingidentity.did.sdk.types.Claim;

import java.util.List;

public interface CredentialPickerListener {
    void onCredentialPicked(Claim claim, List<String> keys);

    void onPickerComplete();

    void onPickerCanceled();

}
