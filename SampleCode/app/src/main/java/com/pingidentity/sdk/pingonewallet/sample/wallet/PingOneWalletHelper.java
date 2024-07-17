package com.pingidentity.sdk.pingonewallet.sample.wallet;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;

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
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.ApplicationUiHandler;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.CredentialPicker;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletCredentialEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletError;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletPairingEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletMessage.credential.CredentialAction;
import com.pingidentity.sdk.pingonewallet.types.regions.PingOneRegion;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PingOneWalletHelper implements WalletCallbackHandler {

    public static final String TAG = PingOneWalletHelper.class.getCanonicalName();

    private final PingOneWalletClient pingOneWalletClient;
    private final WeakReference<FragmentActivity> contextWeakReference;
    private ApplicationUiHandler applicationUiHandler;
    private CredentialPicker credentialPicker;

    public static void initializeWallet(FragmentActivity context, Consumer<PingOneWalletHelper> onResult, Consumer<Throwable> onError) {
        Completable.fromRunnable(() -> new PingOneWalletClient.Builder(true, PingOneRegion.NA)
                        .build(context, pingOneWalletClient -> {
                            final PingOneWalletHelper helper = new PingOneWalletHelper(pingOneWalletClient, context);
                            onResult.accept(helper);
                        }, onError))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private PingOneWalletHelper(PingOneWalletClient client, FragmentActivity context) {
        this.pingOneWalletClient = client;
        this.contextWeakReference = new WeakReference<>(context);
        client.registerCallbackHandler(this);

        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> getPushToken(pushToken -> {
            if (pushToken != null) {
                pingOneWalletClient.updatePushTokens(pushToken);
            }
        }));



        checkForMessages();
    }

    public void setApplicationUiHandler(ApplicationUiHandler applicationUiHandler) {
        this.applicationUiHandler = applicationUiHandler;
    }

    public void setCredentialPicker(CredentialPicker credentialPicker) {
        this.credentialPicker = credentialPicker;
    }

    public DataRepository getDataRepository() {
        return pingOneWalletClient.getDataRepository();
    }

    public void processPingOneRequest(@NonNull final String qrContent) {
        pingOneWalletClient.processPingOneRequest(qrContent);
    }

    public void reportCredentialDeletion(@NonNull final Claim claim) {
        pingOneWalletClient.reportCredentialDeletion(claim);
    }

    public void checkForMessages() {
        pingOneWalletClient.checkForMessages();
    }

    public void pollForMessages() {
        pingOneWalletClient.pollForMessages();
    }

    public void stopPolling() {
        pingOneWalletClient.stopPolling();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// WalletCallbackHandler Implementation /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean handleCredentialIssuance(String issuer, String message, Challenge challenge, Claim claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialIssuance");
        Log.i(TAG, "Credential received: Issuer: " + issuer + " message: " + message);
        pingOneWalletClient.getDataRepository().saveCredential(claim);
        BackgroundThreadHandler.postOnMainThread(() -> notifyUser("Received a new credential"));
        return true;
    }

    @Override
    public boolean handleCredentialRevocation(String issuer, String message, Challenge challenge, ClaimReference claimReference, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialRevocation");
        Log.i(TAG, "Credential revoked: Issuer: " + issuer + " message: " + message);
        pingOneWalletClient.getDataRepository().saveCredentialReference(claimReference);
        BackgroundThreadHandler.postOnMainThread(() -> notifyUser("Credential Revoked"));
        return true;
    }

    @Override
    public void handleCredentialPresentation(String sender, String message, Challenge challenge, List<Share> claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialPresentation");
        BackgroundThreadHandler.postOnMainThread(() -> notifyUser("Coming soon..."));
    }

    @Override
    public void handleCredentialRequest(PresentationRequest presentationRequest) {
        if (presentationRequest.isPairingRequest()) {
            handlePairingRequest(presentationRequest);
            return;
        }

        BackgroundThreadHandler.postOnMainThread(() -> notifyUser("Processing presentation request..."));

        final List<Claim> allClaims = pingOneWalletClient.getDataRepository().getAllCredentials();
        final List<CredentialMatcherResult> credentialMatcherResults = pingOneWalletClient.findMatchingCredentialsForRequest(presentationRequest, allClaims).getResult();
        List<CredentialMatcherResult> matchingCredentials = Collections.emptyList();
        if (credentialMatcherResults != null){
            matchingCredentials = credentialMatcherResults.stream().filter(result -> !result.getClaims().isEmpty()).collect(Collectors.toList());
        }
        if (matchingCredentials.isEmpty()) {
            showError(R.string.dialog_no_matching_cred_title, R.string.dialog_no_matching_cred_message);
            return;
        }
        if (credentialPicker == null) {
            return;
        }

        int message = matchingCredentials.size() == credentialMatcherResults.size() ? R.string.dialog_presentation_message : R.string.dialog_presentation_message_missing_credential;
        int title = matchingCredentials.size() == credentialMatcherResults.size() ? R.string.dialog_presentation_title : R.string.dialog_presentation_title_missing_credential;
        askUserPermission(title, message, isPositiveAction -> {
            if (isPositiveAction) {
                selectCredential(presentationRequest, credentialMatcherResults);
            } else {
                Log.i(TAG, "Presentation rejected by user.");
                notifyUser("Presentation canceled");
            }
        });
    }
    private void selectCredential(PresentationRequest presentationRequest, List<CredentialMatcherResult> credentialMatcherResults ){
        credentialPicker.selectCredentialFor(presentationRequest, credentialMatcherResults, result -> {
            if (result == null || result.isEmpty()) {
                notifyUser("Presentation canceled");
                return;
            }
            shareCredentialPresentation(result);
        });
    }

    @Override
    public void handleError(WalletException error) {
        Log.i(TAG, "handleError");
        Log.e(TAG, "Exception in message processing", error);
        if (error.getCause() instanceof NotFoundException) {
            notifyUser("Failed to process request");
        }
    }

    @Override
    public void handleEvent(WalletEvent event) {
        switch (event.getType()) {
            case PAIRING:
                handlePairingEvent((WalletPairingEvent) event);
                break;
            case CREDENTIAL_UPDATE:
                handleCredentialEvent((WalletCredentialEvent) event);
                break;
            case ERROR:
                handleErrorEvent((WalletError) event);
                break;
            default:
                Log.e(TAG, "Received unknown event: " + event.getType());
        }

    }

    private void handlePairingRequest(@NonNull final PresentationRequest presentationRequest) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                getPushToken(pushToken -> askUserPermission(R.string.dialog_pairing_title, R.string.dialog_pairing_message, isPositiveAction -> {
                    if (isPositiveAction) {
                        try {
                            pingOneWalletClient.pairWallet(presentationRequest, pushToken);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to pair wallet", e);
                            notifyUser("Wallet pairing failed");
                        }
                    } else {
                        notifyUser("Pairing canceled");
                        Log.i(TAG, "User rejected pairing request");
                    }
                })));
    }

    private void shareCredentialPresentation(@NonNull final CredentialsPresentation credentialsPresentation) {
        presentCredential(credentialsPresentation);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private void presentCredential(@NonNull final CredentialsPresentation credentialsPresentation) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                pingOneWalletClient.presentCredentials(credentialsPresentation)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(presentationResult -> {
                            switch (presentationResult.getPresentationStatus().getStatus()) {
                                case SUCCESS:
                                    notifyUser("Information sent successfully");
                                    break;
                                case FAILURE:
                                    notifyUser("Failed to present credential");
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

    /** @noinspection SwitchStatementWithTooFewBranches*/
    private void handlePresentationAction(final PresentationAction action) {
        if (action == null) {
            return;
        }
        switch (action.getActionType()) {
            case OPEN_URI:
                final OpenUriAction openUriAction = (OpenUriAction) action;
                final String appOpenUri = openUriAction.getRedirectUri();
                if (applicationUiHandler != null) {
                    applicationUiHandler.openUri(appOpenUri);
                } else {
                    Log.e(TAG, "Must implement ApplicationUiHandler for this");
                }
        }
    }

    private void handlePairingEvent(WalletPairingEvent event) {
        Log.i(TAG, "Wallet paired: " + event.isSuccess());
        if (event.isSuccess()) {
            this.notifyUser("Wallet paired successfully");
        } else {
            this.notifyUser("Wallet pairing failed");
            if (event.getError() != null) {
                Log.e(TAG, "Wallet Pairing Error", event.getError());
            }
        }
    }

    private void handleErrorEvent(WalletError errorEvent) {
        Log.e(TAG, "Error in wallet callback handler", errorEvent.getError());
    }

    /** @noinspection SwitchStatementWithTooFewBranches*/
    private void handleCredentialEvent(WalletCredentialEvent event) {
        switch (event.getCredentialEvent()) {
            case CREDENTIAL_UPDATED:
                handleCredentialUpdate(event.getAction(), event.getReferenceCredentialId());
        }
    }

    /** @noinspection SwitchStatementWithTooFewBranches*/
    private void handleCredentialUpdate(CredentialAction action, String referenceCredentialId) {
        switch (action) {
            case DELETE:
                pingOneWalletClient.getDataRepository().deleteCredential(referenceCredentialId);
        }
    }

    private void getPushToken(@NonNull Consumer<String> resultConsumer) {
        if (applicationUiHandler == null || applicationUiHandler.getNotificationServiceHelper() == null) {
            resultConsumer.accept(null);
            return;
        }

        final String pushToken = applicationUiHandler.getNotificationServiceHelper().getPushToken().getValue();

        if (pushToken == null && contextWeakReference.get() != null) {
            BackgroundThreadHandler.postOnMainThread(() -> applicationUiHandler.getNotificationServiceHelper().getPushToken().observe(contextWeakReference.get(),
                    token -> BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> resultConsumer.accept(token))));

        } else {
            resultConsumer.accept(pushToken);
        }
    }


    // ApplicationUIHandler Calls
    private void notifyUser(@NonNull final String message) {
        if (applicationUiHandler == null) {
            return;
        }
        applicationUiHandler.showToast(message);
    }

    private void askUserPermission(int title, int message, @NonNull final java.util.function.Consumer<Boolean> consumer) {
        if (applicationUiHandler == null) {
            consumer.accept(true);
            return;
        }

        applicationUiHandler.showConfirmationAlert(title, message, consumer);
    }

    /** @noinspection SameParameterValue*/
    private void showError(int title, int message) {
        if (applicationUiHandler == null) {
            return;
        }
        applicationUiHandler.showAlert(title, message);
    }

}