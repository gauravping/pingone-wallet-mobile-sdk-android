package com.pingidentity.sdk.pingonewallet.sample.models;

import com.pingidentity.did.sdk.types.Claim;

@SuppressWarnings("unused")
public class Credential {

    final Claim claim;
    boolean isRevoked = false;

    public Credential(Claim claim) {
        this.claim = claim;
    }

    public Credential(Claim claim, boolean isRevoked) {
        this.claim = claim;
        this.isRevoked = isRevoked;
    }

    public Claim getClaim() {
        return claim;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}
