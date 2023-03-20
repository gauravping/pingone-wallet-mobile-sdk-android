package com.pingidentity.sdk.pingonewallet.sample.utils;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.CredentialDetailsListener;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.ui.item_picker.ItemPickerFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsFragment;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class NotificationUtil {

    private static final String TAG = NotificationUtil.class.getCanonicalName();

    private final WeakReference<FragmentActivity> mContextWeakReference;
    private WeakReference<FragmentManager> mFragmentManagerWeakReference;

    private static NotificationUtil instance;

    private NotificationUtil(FragmentActivity context) {
        this.mContextWeakReference = new WeakReference<>(context);
    }

    public WeakReference<FragmentActivity> getContextWeakReference() {
        return mContextWeakReference;
    }

    public static NotificationUtil getInstance(FragmentActivity context) {
        if (instance == null) {
            instance = new NotificationUtil(context);
        }
        return instance;
    }

    public void updateFragmentManager(FragmentManager fragmentManager) {
        this.mFragmentManagerWeakReference = new WeakReference<>(fragmentManager);
    }

    public void openUri(@NonNull final String uri) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot open uri");
            return;
        }
        Intent redirectUriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mContextWeakReference.get().startActivity(redirectUriIntent);
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

    public void showPairingRequest(Runnable positiveCallback) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(mContextWeakReference.get())
                        .setMessage(mContextWeakReference.get().getString(R.string.dialog_pairing_message))
                        .setPositiveButton(R.string.button_confirm, (dialog, which) -> positiveCallback.run())
                        .setNegativeButton(R.string.dialog_share_cancel, (dialog, which) -> dialog.dismiss())
                        .show());
    }

    @SuppressWarnings("unused")
    public void showShareRequest(int cardType, Runnable positiveCallback) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }

        showShareRequest(mContextWeakReference.get().getString(cardType), positiveCallback);
    }

    public void showShareRequest(String cardType, Runnable positiveCallback) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(mContextWeakReference.get())
                        .setMessage(mContextWeakReference.get().getString(R.string.dialog_share_message, cardType))
                        .setPositiveButton(R.string.dialog_share_confirm, (dialog, which) -> positiveCallback.run())
                        .setNegativeButton(R.string.dialog_share_cancel, (dialog, which) -> dialog.dismiss())
                        .show());
    }

    public void showToast(String text) {
        if (mContextWeakReference.get() == null) {
            Log.e(TAG, "Context reference nonexistent, cannot display UI element");
            return;
        }

        BackgroundThreadHandler.postOnMainThread(() -> Toast.makeText(mContextWeakReference.get(), text, Toast.LENGTH_SHORT).show());
    }

    public void showClaimsPicker(List<Claim> claims, ItemPickerFragment.ItemPickerListener listener) {
        if (mFragmentManagerWeakReference.get() == null) {
            Log.e(TAG, "FragmentManager reference nonexistent, cannot display UI element");
            return;
        }

        replaceFragment(ItemPickerFragment.newInstance(claims, claim -> {
            if (claim == null) {
                listener.onItemPicked(null);
                return;
            }
            replaceFragment(CredentialDetailsFragment.newInstance(
                    new Credential(claim), mContextWeakReference.get().getString(R.string.button_confirm), new CredentialDetailsListener() {
                        @Override
                        public void onActionClick(Claim claim) {
                            listener.onItemPicked(claim);
                        }

                        @Override
                        public void onCancel() {
                            showClaimsPicker(claims, listener);
                        }
                    }), CredentialDetailsFragment.class.getCanonicalName());
        }), ItemPickerFragment.class.getCanonicalName());
    }

    private void replaceFragment(Fragment fragment, String tag) {
        mContextWeakReference.get().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

}