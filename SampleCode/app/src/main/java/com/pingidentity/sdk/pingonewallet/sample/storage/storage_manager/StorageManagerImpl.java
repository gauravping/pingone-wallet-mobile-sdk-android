package com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pingidentity.did.sdk.client.service.model.ApplicationInstance;
import com.pingidentity.did.sdk.json.JsonUtil;
import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.encrypted_storage.EncryptedStorageProvider;
import com.pingidentity.sdk.pingonewallet.encrypted_storage.EncryptedStorageProviderImpl;
import com.pingidentity.sdk.pingonewallet.contracts.StorageManagerContract;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
public class StorageManagerImpl implements StorageManagerContract {

    public static final String TAG = StorageManagerImpl.class.getCanonicalName();

    private static final String PREF_FILE_ENC_NAME = "p1verify_test_enc";

    private static final String CLAIM_PREFIX_KEY = "claim_";
    private static final String APPLICATION_INSTANCE_KEY = "app_instance_key";

    public static final String CLAIM_TYPE_SELF = "self-claim";

    private static StorageManagerContract sharedInstance;

    private final SharedPreferences encryptedPreferences;

    private StorageManagerImpl(@NonNull final SharedPreferences encryptedPreferences) {
        Objects.requireNonNull(encryptedPreferences);
        this.encryptedPreferences = encryptedPreferences;
    }

    public static StorageManagerContract getInstance() {
        return sharedInstance;
    }

    public static void initialize(@NonNull final WeakReference<FragmentActivity> fragmentActivityWeakReference, @NonNull final Runnable resultHandler, @NonNull final Consumer<Throwable> errorHandler) {
        EncryptedStorageProvider storageProvider = new EncryptedStorageProviderImpl();
        storageProvider.getAuthenticatedPreferences(fragmentActivityWeakReference.get(), PREF_FILE_ENC_NAME,
                encryptedPreferences -> {
                    sharedInstance = new StorageManagerImpl(encryptedPreferences);
                    resultHandler.run();
                }, errorHandler);
    }

    ///////////////////////////////////////
    ///////////// Claims work /////////////
    ///////////////////////////////////////

    @Override
    public void saveClaim(@NonNull Claim claim) {
        String key = claim.getData().get("CardType") != null ? claim.getId().toString() : CLAIM_TYPE_SELF;
        encryptedPreferences.edit().putString(CLAIM_PREFIX_KEY + key, claim.toJson()).apply();
    }

    @Override
    public Claim getClaim(@NonNull String claimId) {
        try {
            String claimJson = encryptedPreferences.getString(CLAIM_PREFIX_KEY + claimId, null);
            return Claim.fromJson(claimJson);
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Claim> getClaims() {
        String rawValue = getString("card_ids_preferences_key");
        Set<String> idList = new HashSet<>();
        if (rawValue == null) {
            return Collections.emptyList();
        }

        try {
            idList = (Set<String>) JsonUtil.simple().adapter(new TypeToken<Set<String>>() {
            }.getType()).fromJson(rawValue);
        } catch (IOException e) {
            Log.e(TAG, "Error decoding claims list json", e);
            return Collections.emptyList();
        }

        if (idList == null) {
            return Collections.emptyList();
        }
        
        return idList.stream()
                .map(this::getClaim)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void deleteClaim(String id) {
        final SharedPreferences.Editor editor = encryptedPreferences.edit();
        editor.remove(CLAIM_PREFIX_KEY + id).apply();
    }

    ///////////////////////////////////////
    ///////////// ApplicationInstance /////
    ///////////////////////////////////////

    @Override
    public void saveApplicationInstance(@NonNull ApplicationInstance applicationInstance) {
        encryptedPreferences.edit().putString(APPLICATION_INSTANCE_KEY, applicationInstance.toJson(true)).apply();
    }

    @Override
    public ApplicationInstance getApplicationInstance() {
        String json = encryptedPreferences.getString(APPLICATION_INSTANCE_KEY, null);
        if (json != null) {
            try {
                return ApplicationInstance.fromJson(json);
            } catch (Exception e) {
                Log.e(TAG, String.format("Cannot parse Application Instance Json: %s", json), e);
            }
        }
        return null;
    }

    ///////////////////////////////////////
    ///////////// Different Data //////////
    ///////////////////////////////////////

    @Override
    public void saveString(@NonNull String str, @NonNull String key) {
        encryptedPreferences.edit().putString(key, str).apply();
    }

    @Override
    public String getString(@NonNull String key) {
        return encryptedPreferences.getString(key, null);
    }

    public void removeString(@NonNull String key) {
        encryptedPreferences.edit().remove(key).apply();
    }

}
