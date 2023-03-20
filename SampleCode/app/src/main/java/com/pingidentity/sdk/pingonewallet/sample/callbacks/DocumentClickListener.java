package com.pingidentity.sdk.pingonewallet.sample.callbacks;

import com.pingidentity.sdk.pingonewallet.sample.models.Credential;

public interface DocumentClickListener {

    void onActionClick(Credential credential);

}
