package com.pingidentity.sdk.pingonewallet.sample.storage.data_repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.ClaimReference;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;

import java.util.List;

public interface DataRepository {

    void saveProfile(Profile profile);

    Profile getProfile();

    MutableLiveData<List<Claim>> subscribeClaimsChange();

    void saveSelfClaim(Claim claim);

    Claim getSelfClaim();

    void saveClaim(Claim claim);

    Claim getClaim(String id);

    void saveRevokedClaimReference(@NonNull final ClaimReference claimReference);

    ClaimReference getRevokedClaimReference(@NonNull final String claimId);

    boolean isClaimRevoked(@NonNull final String claimId);

    void deleteClaim(Claim claim);

    List<Claim> getAllClaims();

}
