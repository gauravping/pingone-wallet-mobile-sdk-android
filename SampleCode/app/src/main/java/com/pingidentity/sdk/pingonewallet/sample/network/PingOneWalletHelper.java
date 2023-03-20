package com.pingidentity.sdk.pingonewallet.sample.network;

import static com.pingidentity.did.sdk.w3c.verifiableCredential.PresentationActionType.OPEN_URI;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.google.firebase.messaging.FirebaseMessaging;
import com.pingidentity.did.sdk.client.service.NotFoundException;
import com.pingidentity.did.sdk.client.service.model.Challenge;
import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.did.sdk.types.ClaimReference;
import com.pingidentity.did.sdk.types.Share;
import com.pingidentity.did.sdk.w3c.verifiableCredential.OpenUriAction;
import com.pingidentity.did.sdk.w3c.verifiableCredential.PresentationAction;
import com.pingidentity.sdk.pingonewallet.client.PingOneWalletClient;
import com.pingidentity.sdk.pingonewallet.contracts.WalletCallbackHandler;
import com.pingidentity.sdk.pingonewallet.errors.WalletException;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.ClaimPickerListener;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.notifications.PingOneNotificationService;
import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager.StorageManagerImpl;
import com.pingidentity.sdk.pingonewallet.sample.utils.NotificationUtil;
import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;
import com.pingidentity.sdk.pingonewallet.types.RequestedKey;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PingOneWalletHelper implements WalletCallbackHandler {

    public static final String TAG = PingOneWalletHelper.class.getCanonicalName();

    private static PingOneWalletHelper shared;
    private static Runnable resultHandler;

    private PingOneWalletClient mPingOneWalletClient;

    private final DataRepository mDataRepository;
    private final NotificationUtil mNotificationUtil;


    private PingOneWalletHelper(DataRepository repository, NotificationUtil notificationUtil) {
        mDataRepository = repository;
        mNotificationUtil = notificationUtil;

        Completable.fromRunnable(() -> {
                    mPingOneWalletClient = new PingOneWalletClient.Builder()
                            .setApplicationInstance(StorageManagerImpl.getInstance().getApplicationInstance())
                            .setWalletCallbackHandler(PingOneWalletHelper.this)
                            .build();
                    /* TODO: Uncomment to use push notifications
                     *  BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> getPushToken(pushToken -> {
                     *      if (pushToken != null) {
                     *          StorageManagerImpl.getInstance().saveApplicationInstance(mPingOneWalletClient.updatePushToken(pushToken));
                     *      }
                     *  }));
                     *
                     */

                    StorageManagerImpl.getInstance().saveApplicationInstance(mPingOneWalletClient.getApplicationInstance());
                    resultHandler.run();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public static void initialize(DataRepository repository, NotificationUtil notificationUtil, Runnable result) {
        resultHandler = result;
        shared = new PingOneWalletHelper(repository, notificationUtil);
    }

    public static PingOneWalletHelper getInstance() {
        return shared;
    }

    public void processQrContent(@NonNull final String qrContent) {
        mPingOneWalletClient.processQrContent(qrContent);
    }

    public void reportCredentialDeletion(@NonNull final Claim claim) {
        mPingOneWalletClient.reportCredentialDeletion(claim);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// WalletCallbackHandler Implementation ////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean handleCredentialIssuance(String issuer, String message, Challenge challenge, Claim claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialIssuance");
        mDataRepository.saveClaim(claim);
        BackgroundThreadHandler.postOnMainThread(() -> mNotificationUtil.showToast("Received a new credential"));
        return true;
    }

    @Override
    public boolean handleCredentialRevocation(String issuer, String message, Challenge challenge, ClaimReference claimReference, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialRevocation");
        mDataRepository.saveRevokedClaimReference(claimReference);
        BackgroundThreadHandler.postOnMainThread(() -> mNotificationUtil.showToast("Credential Revoked"));
        return true;
    }

    @Override
    public void handleCredentialPresentation(String sender, String message, Challenge challenge, List<Share> claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialPresentation");
        BackgroundThreadHandler.postOnMainThread(() -> mNotificationUtil.showToast("Coming soon..."));
    }

    @Override
    public void handleCredentialRequest(PresentationRequest presentationRequest) {
        if (handlePairingRequest(presentationRequest)) {
            return;
        }

        Log.i(TAG, "handlePresentationRequest");
        final List<RequestedKey> requestedKeys = presentationRequest.getKeys();
        if (requestedKeys == null || requestedKeys.isEmpty()) {
            mNotificationUtil.showPairingRequest(() -> shareSelfClaim(presentationRequest));
            return;
        }

        BackgroundThreadHandler.postOnMainThread(() -> mNotificationUtil.showToast("Processing presentation request..."));
        final List<Claim> allClaims = mDataRepository.getAllClaims();
        final List<CredentialMatcherResult> credentialMatcherResults = mPingOneWalletClient.findMatchingCredentialsForRequest(presentationRequest, allClaims).getResult();

        boolean matchingCredentials = false;

        if (credentialMatcherResults != null) {
            for (CredentialMatcherResult credentialMatcherResult : credentialMatcherResults) {
                if (!credentialMatcherResult.getClaims().isEmpty()) {
                    matchingCredentials = true;
                    break;
                }
            }
        }

        if (!matchingCredentials) {
            mNotificationUtil.showAlert("No matching credentials", "Cannot find any credentials in your wallet matching the criteria in the request.");
            return;
        }

        selectCredentialToPresent(credentialMatcherResults, 0, new ClaimPickerListener() {

            final CredentialsPresentation presentation = new CredentialsPresentation(presentationRequest);

            @Override
            public void onClaimPicked(@NonNull Claim claim, @NonNull List<String> keys) {
                presentation.addClaimForKeys(keys, claim);
            }

            @Override
            public void onPickerComplete() {
                shareCredentialPresentation(presentation);
            }

            @Override
            public void onPickerCanceled() {
                Log.i(TAG, "Presentation action canceled by user.");
                mNotificationUtil.showToast("Presentation canceled");
            }
        });

    }

    @Override
    public void handleError(WalletException error) {
        Log.i(TAG, "handleError");
        Log.e(TAG, "Exception in message processing", error);
        if (error.getCause() instanceof NotFoundException){
            mNotificationUtil.showToast("Failed to process request");
        }
    }

    private boolean handlePairingRequest(@NonNull final PresentationRequest presentationRequest) {
        if (!presentationRequest.isPairingRequest()) {
            return false;
        }

        /*  TODO: Uncomment to use push notifications
         *   BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> getPushToken(pushToken -> {
         *          mNotificationUtil.showPairingRequest(() ->
         *              StorageManagerImpl.getInstance().saveApplicationInstance(mPingOneWalletClient.pairWallet(presentationRequest, mNotificationUtil.getContextWeakReference().get(), pushToken)));
         *      }));
         */

        // Comment/delete the following block if using push notifications
        mNotificationUtil.showPairingRequest(() ->
                StorageManagerImpl.getInstance().saveApplicationInstance(mPingOneWalletClient.pairWallet(presentationRequest, mNotificationUtil.getContextWeakReference().get(), null)));
        return true;
    }

    private void selectCredentialToPresent(@NonNull final List<CredentialMatcherResult> credentialMatcherResults, int index, @NonNull final ClaimPickerListener listener) {
        if (index >= credentialMatcherResults.size()) {
            listener.onPickerComplete();
            return;
        }

        final CredentialMatcherResult credentialMatcherResult = credentialMatcherResults.get(index);
        if (credentialMatcherResult.getClaims().isEmpty()) {
            selectCredentialToPresent(credentialMatcherResults, index + 1, listener);
            return;
        }

        if (credentialMatcherResult.getClaims().size() == 1) {
            listener.onClaimPicked(credentialMatcherResult.getClaims().get(0), credentialMatcherResult.getRequestedKeys());
            selectCredentialToPresent(credentialMatcherResults, index + 1, listener);
        } else {
            mNotificationUtil.showClaimsPicker(credentialMatcherResult.getClaims(), claim -> {
                if (claim == null) {
                    listener.onPickerCanceled();
                } else {
                    listener.onClaimPicked(claim, credentialMatcherResult.getRequestedKeys());
                    selectCredentialToPresent(credentialMatcherResults, index + 1, listener);
                }
            });
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void shareSelfClaim(@NonNull final PresentationRequest presentationRequest) {
        final CredentialsPresentation presentation = new CredentialsPresentation(presentationRequest);
        Claim selfClaim = mDataRepository.getSelfClaim();
        if (selfClaim == null) {

            final Profile profile = mDataRepository.getProfile();
            Map<String, String> claimMap = profile == null ? new HashMap<>() : profile.toMap();
            mPingOneWalletClient.createSelfClaim(claimMap)
                    .subscribe(
                            claim -> {
                                shareCredentialPresentation(presentation.addClaimForKeys(presentationRequest.getStringKeys(), claim));
                                mDataRepository.saveSelfClaim(claim);
                            },
                            throwable -> Log.e(TAG, "Failed to create self-claim...share data:", throwable));
        } else {
            shareCredentialPresentation(presentation.addClaimForKeys(presentationRequest.getStringKeys(), selfClaim));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void shareCredentialPresentation(@NonNull final CredentialsPresentation credentialsPresentation) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                mPingOneWalletClient.presentCredentials(credentialsPresentation)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(presentationResult -> {
                            switch (presentationResult.getPresentationStatus().getStatus()) {
                                case SUCCESS:
                                    mNotificationUtil.showToast("Information sent successfully");
                                    break;
                                case FAILURE:
                                    mNotificationUtil.showToast("Failed to present credential");
                                    if (presentationResult.getError() != null) {
                                        Log.e(TAG, "\"Error sharing information: ", presentationResult.getError());
                                    }
                                    Log.e(TAG, String.format("Presentation failed. %s", presentationResult.getDetails()));
                                    break;
                                case REQUIRES_ACTION:
                                    handlePresentationAction(presentationResult.getPresentationStatus().getAction());
                            }
                        }));
    }

    private void handlePresentationAction(final PresentationAction action) {
        if (action == null) {
            return;
        }
        switch (action.getActionType()) {
            case OPEN_URI:
                final OpenUriAction openUriAction = (OpenUriAction)action;
                final String appOpenUri = openUriAction.getRedirectUri();
                mNotificationUtil.openUri(appOpenUri);
        }
    }

    @SuppressWarnings("unused")
    private static void getPushToken(@Nullable Consumer<String> resultConsumer) {
        final String pushToken = PingOneNotificationService.getPushToken().getValue();
        if (pushToken == null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                String token = null;

                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to fetch current token", task.getException());
                } else {
                    token = task.getResult();
                    if (token != null) {
                        PingOneNotificationService.updatePushToken(token);
                    }
                    Log.d(TAG, "Push Token retrieved: " + token);
                }

                if (resultConsumer != null) {
                    resultConsumer.accept(token);
                }
            });
        } else {
            if (resultConsumer != null) {
                resultConsumer.accept(pushToken);
            }
        }
    }

}