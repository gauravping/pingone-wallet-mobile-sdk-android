package com.pingidentity.sdk.pingonewallet.sample.storage.data_repository;

import static com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager.StorageManagerImpl.CLAIM_TYPE_SELF;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.ClaimReference;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.utils.JsonUtil;
import com.pingidentity.sdk.pingonewallet.contracts.StorageManagerContract;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

@Singleton
public class DataRepositoryImpl implements DataRepository {

    public static final String TAG = DataRepositoryImpl.class.getCanonicalName();

    private static final String KEY_CARD_ID = "card_ids_preferences_key";
    private static final String KEY_REVOKED_CARD_ID = "revoked_card_ids_preferences_key";
    private static final String KEY_PROFILE = "profile";
    private static final String PROFILE_SELF_CLAIM_STORAGE_KEY = "profile_self_claim_storage_key";

    private final StorageManagerContract mStorage;
    private final JsonUtil mJsonUtil;

    private final Set<String> mClaimIdList;
    private final Set<String> mRevokedClaimIds;

    private final List<Claim> mClaimsList = new ArrayList<>();
    private final MutableLiveData<List<Claim>> mClaimsLiveData = new MutableLiveData<>();

    public DataRepositoryImpl(StorageManagerContract storageManager, JsonUtil jsonUtil){
        this.mStorage = storageManager;
        this.mJsonUtil = jsonUtil;
        this.mClaimIdList = getClaimIdList();
        this.mRevokedClaimIds = getRevokedClaimIdList();
        loadClaims();
    }

    ///////////////////////////////////////
    ///////////// Profile work ////////////
    ///////////////////////////////////////

    @Override
    public void saveProfile(@NonNull final Profile profile) {
        final String profileString =
                mJsonUtil.toJson(profile.toMap(), Types.newParameterizedType(Map.class, String.class, String.class));
        mStorage.saveString(profileString, KEY_PROFILE);
        mStorage.deleteClaim(CLAIM_TYPE_SELF);
    }

    @Override
    public Profile getProfile() {
        final String profileString = mStorage.getString(KEY_PROFILE);
        if (profileString == null) {
            return null;
        }
        Map<String, String> profileData = mJsonUtil.fromJson(profileString,
                Types.newParameterizedType(Map.class, String.class, String.class));
        if (profileData != null) {
            return new Profile(profileData);
        }
        return null;
    }

    ///////////////////////////////////////
    ///////////// Card ID's work //////////
    ///////////////////////////////////////

    @Override
    public List<Claim> getAllClaims() {
        return mClaimsList;
    }

    @Override
    public MutableLiveData<List<Claim>> subscribeClaimsChange() {
        return mClaimsLiveData;
    }

    public void saveRevokedClaimReference(@NonNull final ClaimReference claimReference) {
        mStorage.saveString(claimReference.toJson(), "REVOKED_" + claimReference.getId().toString());
        saveRevokedClaimId(claimReference.getId().toString());
        mClaimsLiveData.postValue(mClaimsList);
    }

    @Nullable
    public ClaimReference getRevokedClaimReference(@NonNull final String claimId) {
        if (mRevokedClaimIds.contains(claimId)) {
            final String claimReferenceJson = mStorage.getString("REVOKED" + claimId);
            if (claimReferenceJson != null) {
                try {
                    return ClaimReference.fromJson(claimReferenceJson);
                } catch (IOException e) {
                    Log.e(TAG, String.format("Failed to read claim reference for id %s", claimId), e);
                }
            }
        }
        return null;
    }

    @Override
    public void saveClaim(Claim claim) {
        mStorage.saveClaim(claim);
        mClaimsList.add(claim);
        updateCredentialViewModel();
        saveClaimId(claim.getId().toString());
    }

    public void saveSelfClaim(Claim claim) {
        mStorage.saveClaim(claim);
        mStorage.saveString(claim.getId().toString(), PROFILE_SELF_CLAIM_STORAGE_KEY);
    }

    @Nullable
    public Claim getSelfClaim() {
        final String selfClaimId = mStorage.getString(PROFILE_SELF_CLAIM_STORAGE_KEY);
        if(selfClaimId == null) {
            return null;
        }
        return mStorage.getClaim(selfClaimId);
    }

    @Override
    public Claim getClaim(String id) {
        return mStorage.getClaim(id);
    }

    @Override
    public boolean isClaimRevoked(@NonNull final String claimId) {
        return mRevokedClaimIds.contains(claimId);
    }

    @Override
    public void deleteClaim(Claim claim) {
        mStorage.deleteClaim(claim.getId().toString());
        mClaimsList.remove(claim);
        updateCredentialViewModel();
        removeClaimId(claim.getId().toString());
    }

    ///////////////////////////////////////
    ///////////// Card ID's work //////////
    ///////////////////////////////////////

    private void updateCredentialViewModel() {
        mClaimsLiveData.postValue(mClaimsList);
    }

    private void loadClaims() {
        mClaimsList.addAll(mStorage.getClaims());
        updateCredentialViewModel();
    }

    private Set<String> getClaimIdList() {
        String rawValue = mStorage.getString(KEY_CARD_ID);
        if (rawValue != null) {
            Set<String> idList = mJsonUtil.fromJson(rawValue, Types.newParameterizedType(Set.class, String.class));
            if (idList != null) {
                return idList;
            }
        }
        return new HashSet<>();
    }

    private Set<String> getRevokedClaimIdList() {
        String rawValue = mStorage.getString(KEY_REVOKED_CARD_ID);
        if (rawValue != null) {
            Set<String> idList = mJsonUtil.fromJson(rawValue, Types.newParameterizedType(Set.class, String.class));
            if (idList != null) {
                return idList;
            }
        }
        return new HashSet<>();
    }

    private void saveRevokedClaimId(String id) {
        mRevokedClaimIds.add(id);
        mStorage.saveString(mJsonUtil.toJson(mRevokedClaimIds, Types.newParameterizedType(Set.class, String.class)), KEY_REVOKED_CARD_ID);
    }

    private void saveClaimId(String id) {
        mClaimIdList.add(id);
        mStorage.saveString(mJsonUtil.toJson(mClaimIdList, Types.newParameterizedType(Set.class, String.class)), KEY_CARD_ID);
    }

    private void removeClaimId(@NonNull final String cardId) {
        mClaimIdList.remove(cardId);
        mStorage.saveString(mJsonUtil.toJson(mClaimIdList, Types.newParameterizedType(Set.class, String.class)), KEY_CARD_ID);
    }

}
