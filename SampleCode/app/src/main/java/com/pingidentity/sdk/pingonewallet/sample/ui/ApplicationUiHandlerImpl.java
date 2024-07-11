package com.pingidentity.sdk.pingonewallet.sample.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.notifications.PingOneNotificationService;
import com.pingidentity.sdk.pingonewallet.sample.ui.home.HomeFragmentDirections;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.PickerSharedViewModel;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.ApplicationUiHandler;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.NotificationServiceHelper;
import com.pingidentity.sdk.pingonewallet.types.ClaimKeys;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

public class ApplicationUiHandlerImpl implements ApplicationUiHandler {

    private static final String TAG = ApplicationUiHandler.class.getCanonicalName();

    private final WeakReference<FragmentActivity> mContextWeakReference;

    private static ApplicationUiHandler instance;

    private ApplicationUiHandlerImpl(FragmentActivity context) {
        this.mContextWeakReference = new WeakReference<>(context);
    }

    public static ApplicationUiHandler getInstance(FragmentActivity context) {
        if (instance == null) {
            instance = new ApplicationUiHandlerImpl(context);
        }
        return instance;
    }

    public void showAlert(int title, int message) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }

        showAlert(mContextWeakReference.get().getString(title), mContextWeakReference.get().getString(message));
    }

    public void showAlert(String title, String message) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(mContextWeakReference.get())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> dialog.dismiss())
                        .show());
    }

    public void showToast(String text) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }

        BackgroundThreadHandler.postOnMainThread(() -> Toast.makeText(mContextWeakReference.get(), text, Toast.LENGTH_SHORT).show());
    }

    public void showConfirmationAlert(int title, int message, @NonNull final Consumer<Boolean> consumer) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(mContextWeakReference.get())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(R.string.button_confirm, (dialog, which) -> consumer.accept(true))
                        .setNegativeButton(R.string.dialog_share_cancel, (dialog, which) -> {
                            dialog.dismiss();
                            consumer.accept(false);
                        })
                        .show());
    }

    public void openUri(@NonNull final String uri) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot open uri");
            return;
        }
        Intent redirectUriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mContextWeakReference.get().startActivity(redirectUriIntent);
    }

    public void selectCredentialForPresentation(List<Claim> credentials, Consumer<Claim> consumer) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "FragmentManager reference nonexistent, cannot display UI element");
            return;
        }

        Credential[] claims = credentials.stream()
                .filter(claim -> claim.getData().get(ClaimKeys.cardType) != null)
                .map(claim -> new Credential(claim, false))
                .toArray(Credential[]::new);


        openPicker(claims, consumer, mContextWeakReference.get());
    }

    private void openPicker(Credential[] claims, Consumer<Claim> consumer, FragmentActivity context) {
        final NavController navController;
        try {
             navController = Navigation.findNavController(context, R.id.fragment_container);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to get NavController", e);
            return;
        }

        navController
                .navigate(HomeFragmentDirections.actionHomeFragmentToItemPickerFragment(claims));

        BackgroundThreadHandler.postOnMainThread(() -> {
            PickerSharedViewModel pickerSharedViewModel = new ViewModelProvider(mContextWeakReference.get()).get(PickerSharedViewModel.class);
            pickerSharedViewModel.getPickedCredential().observe(mContextWeakReference.get(), claim -> {
                if (claim != null) {
                    pickerSharedViewModel.clearCredential();
                    consumer.accept(claim);
                    pickerSharedViewModel.getPickedCredential().removeObservers(mContextWeakReference.get());
                }
            });
        });
    }

    @Override
    public NotificationServiceHelper getNotificationServiceHelper() {
        return PingOneNotificationService.getNotificationServiceHelper();
    }

}
