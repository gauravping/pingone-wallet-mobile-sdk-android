package com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces;

import androidx.annotation.NonNull;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.default_impl.DefaultCredentialPicker;

import java.util.List;
import java.util.function.Consumer;

public interface ApplicationUiHandler {

    void showAlert(int title, int message);

    void showAlert(String title, String message);

    void showToast(String text);

    void openUri(@NonNull final String uri);

    void showConfirmationAlert(int title, int message, @NonNull final Consumer<Boolean> consumer);

    void selectCredentialForPresentation(List<Claim> credentials, Consumer<Claim> consumer);

    NotificationServiceHelper getNotificationServiceHelper();

}
