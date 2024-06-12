package com.pingidentity.sdk.pingonewallet.sample.wallet;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.FragmentActivity;

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
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.notifications.PingOneNotificationService;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.picker_abstract.CredentialPicker;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.types.CredentialMatcherResult;
import com.pingidentity.sdk.pingonewallet.types.CredentialsPresentation;
import com.pingidentity.sdk.pingonewallet.types.PresentationRequest;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletCredentialEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletError;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletEvents.WalletPairingEvent;
import com.pingidentity.sdk.pingonewallet.types.WalletMessage.credential.CredentialAction;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PingOneWalletHelper implements WalletCallbackHandler {

    public static final String TAG = PingOneWalletHelper.class.getCanonicalName();

    private PingOneWalletClient pingOneWalletClient;

    private ApplicationUiHandler applicationUiHandler;
    private CredentialPicker credentialPicker;

    public static void initializeWallet(FragmentActivity context, Consumer<PingOneWalletHelper> onResult, Consumer<Throwable> onError) {
        Completable.fromRunnable(() -> new PingOneWalletClient.Builder()
                        .build(context, pingOneWalletClient -> {
                            final PingOneWalletHelper helper = new PingOneWalletHelper(pingOneWalletClient);
                            onResult.accept(helper);
                        }, onError))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private PingOneWalletHelper(PingOneWalletClient client) {
        pingOneWalletClient = client;
        client.registerCallbackHandler(this);

        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() -> getPushToken(pushToken -> {
            if (pushToken != null) {
                pingOneWalletClient.updatePushTokens(pushToken);
            }
        }));

        checkForMessages();
    }

    /**
     * Set optional ApplicationUiHandler to handle UI notifications/Alerts etc.
     * @param applicationUiHandler: Implementation of interface ApplicationUiHandler
     * @see ApplicationUiHandler
     */
    public void setApplicationUiHandler(ApplicationUiHandler applicationUiHandler) {
        this.applicationUiHandler = applicationUiHandler;
    }

    /**
     * Set optional CredentialPicker implementation to handle credential selection when multiple credentials of same type are present in the wallet matching the criteria in the presentation request.
     * @param credentialPicker: Implementation of interface CredentialPicker
     * @see CredentialPicker
     */
    public void setCredentialPicker(CredentialPicker credentialPicker) {
        this.credentialPicker = credentialPicker;
    }

    /**
     * This method returns the data repository used by the wallet for storing ApplicationInstances and Credentials. See DataRepository for more details.
     * @return DataRepository used by Wallet Instance
     * @see DataRepository
     */
    public DataRepository getDataRepository() {
        return pingOneWalletClient.getDataRepository();
    }

    /**
     * Call this method to process PingOne Credentials QR codes and Universal links.
     * @param qrContent: Content of the scanned QR code or Universal link used to open the app
     */
    public void processPingOneRequest(@NonNull final String qrContent) {
        pingOneWalletClient.processPingOneRequest(qrContent);
    }

    /**
     *  Call this method when a credential is deleted from the Wallet. Reporting this action will help admins view accurate stats on their dashboards in future.
     * @param claim: Deleted credential
     */
    public void reportCredentialDeletion(@NonNull final Claim claim) {
        pingOneWalletClient.reportCredentialDeletion(claim);
    }

    /** Call this method to check if wallet has received any new messages in the mailbox. This method can be used to check for messages on user action or if push notifications are not available. */
    public void checkForMessages() {
        pingOneWalletClient.checkForMessages();
    }

    /** Call this method to start polling for new messages sent to the wallet. Use this method only if you are not using push notifications. */
    public void pollForMessages() {
        pingOneWalletClient.pollForMessages();
    }

    /** Call this method to stop polling for messages sent to the wallet. */
    public void stopPolling() {
        pingOneWalletClient.stopPolling();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// WalletCallbackHandler Implementation /////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handle the newly issued credential.
     * @param  issuer: ApplicationInstanceID of the credential issuer
     * @param  message: Optional string message
     * @param  challenge: Optional challenge
     * @param  claim: Issued credential
     * @param  errors: List of any errors while processing/verifying the credential
     * @return boolean: True if the user has accepted the credential, False if the user has rejected the credential
    */
     @Override
    public boolean handleCredentialIssuance(String issuer, String message, Challenge challenge, Claim claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialIssuance");
        Log.i(TAG, "Credential received: Issuer: " + issuer + " message: " + message);
        pingOneWalletClient.getDataRepository().saveCredential(claim);
        BackgroundThreadHandler.postOnMainThread(() -> applicationUiHandler.showToast("Received a new credential"));
        return true;
    }

    /**
     * Handle the revocation of a credential.
     * @param  issuer: ApplicationInstanceID of the credential issuer
     * @param  message: Optional string message
     * @param  challenge: Optional challenge
     * @param  claimReference: ClaimReference for the revoked credential
     * @param  errors: List of any errors while revoking the credential
     * @return True if the user has accepted the credential revocation, False if the user has rejected the credential revocation
     */
    @Override
    public boolean handleCredentialRevocation(String issuer, String message, Challenge challenge, ClaimReference claimReference, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialRevocation");
        Log.i(TAG, "Credential revoked: Issuer: " + issuer + " message: " + message);
        pingOneWalletClient.getDataRepository().saveCredentialReference(claimReference);
        BackgroundThreadHandler.postOnMainThread(() -> applicationUiHandler.showToast("Credential Revoked"));
        return true;
    }
    /**
     * This callback is triggered when another wallet shares a credential with the current application instance.
     * @param  sender: ApplicationInstanceID of the sender
     * @param  message: Optional string message
     * @param  challenge: Optional challenge
     * @param  claim: Shared credential
     * @param  errors: List of any errors while verifying the shared credential
     */
    @Override
    public void handleCredentialPresentation(String sender, String message, Challenge challenge, List<Share> claim, List<WalletException> errors) {
        Log.i(TAG, "handleCredentialPresentation");
        BackgroundThreadHandler.postOnMainThread(() -> applicationUiHandler.showToast("Coming soon..."));
    }

    /**
     * This callback is triggered when a credential is requested from the current wallet using supported protocols.
     * @param presentationRequest PresentationRequest for presenting Credentials from wallet
     */
    @Override
    public void handleCredentialRequest(PresentationRequest presentationRequest) {
        if (presentationRequest.isPairingRequest()) {
            handlePairingRequest(presentationRequest);
            return;
        }

        BackgroundThreadHandler.postOnMainThread(() -> applicationUiHandler.showToast("Processing presentation request..."));

        final List<Claim> allClaims = pingOneWalletClient.getDataRepository().getAllCredentials();
        final List<CredentialMatcherResult> credentialMatcherResults = pingOneWalletClient.findMatchingCredentialsForRequest(presentationRequest, allClaims).getResult();
        List<CredentialMatcherResult> matchingCredentials = Collections.emptyList();
        if (credentialMatcherResults != null){
            matchingCredentials = credentialMatcherResults.stream().filter(result -> !result.getClaims().isEmpty()).collect(Collectors.toList());
        }
        if (matchingCredentials.isEmpty()) {
            applicationUiHandler.showAlert(R.string.dialog_no_matching_cred_title, R.string.dialog_no_matching_cred_message);
            return;
        }
        if (credentialPicker == null) {
            return;
        }

        int message = matchingCredentials.size() == credentialMatcherResults.size() ? R.string.dialog_presentation_message : R.string.dialog_presentation_message_missing_credential;
        int title = matchingCredentials.size() == credentialMatcherResults.size() ? R.string.dialog_presentation_title : R.string.dialog_presentation_title_missing_credential;
        applicationUiHandler.showConfirmationAlert(title, message, isPositiveAction -> {
            if (isPositiveAction) {
                selectCredential(presentationRequest, credentialMatcherResults);
            } else {
                Log.i(TAG, "Presentation rejected by user.");
                applicationUiHandler.showToast("Presentation canceled");
            }
        });
    }
    private void selectCredential(PresentationRequest presentationRequest, List<CredentialMatcherResult> credentialMatcherResults ){
        credentialPicker.selectCredentialFor(presentationRequest, credentialMatcherResults, result -> {
            if (result == null || result.isEmpty()) {
                applicationUiHandler.showToast("Presentation canceled");
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
            applicationUiHandler.showToast("Failed to process request");
        }
    }

    /**
     * Callback returns different events when using Wallet, including errors
     * Backward compatibility - Call handleEvent() if you're still using `handleError` callback to manage exceptions
     * @param event: WalletEvent
     */
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
                getPushToken(pushToken -> applicationUiHandler.showConfirmationAlert(R.string.dialog_pairing_title, R.string.dialog_pairing_message, isPositiveAction -> {
                    if (isPositiveAction) {
                        try {
                            pingOneWalletClient.pairWallet(presentationRequest, pushToken);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to pair wallet", e);
                            applicationUiHandler.showToast("Wallet pairing failed");
                        }
                    } else {
                        applicationUiHandler.showToast("Pairing canceled");
                        Log.i(TAG, "User rejected pairing request");
                    }
                })));
    }

    private void shareCredentialPresentation(@NonNull final CredentialsPresentation credentialsPresentation) {
        presentCredential(credentialsPresentation);
    }

    private void presentCredential(@NonNull final CredentialsPresentation credentialsPresentation) {
        BackgroundThreadHandler.singleBackgroundThreadHandler().post(() ->
                pingOneWalletClient.presentCredentials(credentialsPresentation)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(presentationResult -> {
                            switch (presentationResult.getPresentationStatus().getStatus()) {
                                case SUCCESS:
                                    applicationUiHandler.showToast("Information sent successfully");
                                    break;
                                case FAILURE:
                                    applicationUiHandler.showToast("Failed to present credential");
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
                final OpenUriAction openUriAction = (OpenUriAction) action;
                final String appOpenUri = openUriAction.getRedirectUri();
                applicationUiHandler.openUri(appOpenUri);
        }
    }

    private void handlePairingEvent(WalletPairingEvent event) {
        Log.i(TAG, "Wallet paired: " + event.isSuccess());
        if (event.isSuccess()) {
            this.applicationUiHandler.showToast("Wallet paired successfully");
        } else {
            this.applicationUiHandler.showToast("Wallet pairing failed");
            if (event.getError() != null) {
                Log.e(TAG, "Wallet Pairing Error", event.getError());
            }
        }
    }

    private void handleErrorEvent(WalletError errorEvent) {
        Log.e(TAG, "Error in wallet callback handler", errorEvent.getError());
    }

    private void handleCredentialEvent(WalletCredentialEvent event) {
        switch (event.getCredentialEvent()) {
            case CREDENTIAL_UPDATED:
                handleCredentialUpdate(event.getAction(), event.getReferenceCredentialId());
        }
    }

    private void handleCredentialUpdate(CredentialAction action, String referenceCredentialId) {
        switch (action) {
            case DELETE:
                pingOneWalletClient.getDataRepository().deleteCredential(referenceCredentialId);
        }
    }

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