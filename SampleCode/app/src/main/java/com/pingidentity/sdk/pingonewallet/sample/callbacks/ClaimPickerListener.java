package com.pingidentity.sdk.pingonewallet.sample.callbacks;

import androidx.annotation.NonNull;

import com.pingidentity.did.sdk.types.Claim;

import java.util.List;

public interface ClaimPickerListener {

    void onClaimPicked(@NonNull final Claim claim, @NonNull final List<String> keys);

    void onPickerComplete();

    void onPickerCanceled();
}
