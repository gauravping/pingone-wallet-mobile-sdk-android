package com.pingidentity.sdk.pingonewallet.sample.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.pingidentity.sdk.pingonewallet.sample.MainApplication;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager.StorageManagerImpl;
import com.pingidentity.sdk.pingonewallet.sample.ui.splash.SplashFragment;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeStorage();
    }

    private void initializeStorage() {
        StorageManagerImpl.initialize(new WeakReference<>(MainActivity.this), () -> {
                    ((MainApplication) getApplication()).initDagger();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SplashFragment(), SplashFragment.TAG)
                            .addToBackStack(null)
                            .commit();

                    handleInterceptedIntent(getIntent());
                },
                err -> {
                    Log.e(TAG, "Failed to initialize StorageManager", err);
                    if (err.getMessage() != null && err.getMessage().contains("BIOMETRIC_ERROR_NONE_ENROLLED")) {
                        showPasswordDialog();
                    } else {
                        Toast.makeText(this,
                                getString(R.string.error_init_problem) + err.getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPasswordDialog() {
        new AlertDialog.Builder(this)
                .setMessage("For security purposes, the application stores data in encrypted storage and requires established biometric data")
                .setPositiveButton("Setup biometric data", (dialog, which) -> {
                    openPasswordSettings();
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .show();
    }

    private void openPasswordSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(new Intent(Settings.ACTION_BIOMETRIC_ENROLL));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                startActivity(new Intent(Settings.ACTION_FINGERPRINT_ENROLL));
            } else {
                startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleInterceptedIntent(intent);
    }

    private void handleInterceptedIntent(@NonNull Intent intent) {
        final MainApplication application = (MainApplication) getApplication();
        if (intent.getData() != null) {
            Log.i(TAG, String.format("Intent intercepted: %s", intent.getData().toString()));
            application.setUrl(intent.getData().toString());
        }
    }
}