package com.pingidentity.sdk.pingonewallet.sample.ui.picker.picker_abstract;

import com.pingidentity.did.sdk.types.Claim;

import java.util.List;

public interface CredentialPickerListener {
    void onCredentialPicked(Claim claim, List<String> keys);

    void onPickerComplete();

    void onPickerCanceled();

}
