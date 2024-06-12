package com.pingidentity.sdk.pingonewallet.sample.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.MainApplication;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.databinding.ActivityMainBinding;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.ui.home.HomeFragmentDirections;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.PickerSharedViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.picker_default.DefaultCredentialPicker;
import com.pingidentity.sdk.pingonewallet.sample.wallet.ApplicationUiHandler;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.types.ClaimKeys;
import com.pingidentity.sdk.pingonewallet.utils.BackgroundThreadHandler;

import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity implements ApplicationUiHandler {

    public static final String TAG = MainActivity.class.getCanonicalName();

    private ActivityMainBinding binding;
    private NavController navController;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initWallet();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display a message explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission.
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initWallet() {
        PingOneWalletHelper.initializeWallet(this, helper -> {
            setupDependencyInjection(helper);
            setupNavigation();
            askNotificationPermission();
        }, throwable -> showPasswordDialog());
    }

    private void setupDependencyInjection(PingOneWalletHelper pingOneWalletHelper) {
        ((MainApplication) getApplication()).initDagger(pingOneWalletHelper);
//        ApplicationUiHandler uiHandler = ApplicationUiHandlerImpl.getInstance(this);
        pingOneWalletHelper.setApplicationUiHandler(this);
        pingOneWalletHelper.setCredentialPicker(new DefaultCredentialPicker(this));
    }

    private void setupNavigation() {
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        navController = navHost.getNavController();
        NavGraph graph = navController.getNavInflater().inflate(R.navigation.nav_graph);
        graph.setStartDestination(R.id.homeFragment);
        navController.setGraph(graph);
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

    //////////////////// Notifications ////////////////////

    public void showAlert(int title, int message) {
        showAlert(getString(title), getString(message));
    }

    public void showAlert(String title, String message) {
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(R.string.dialog_confirm, (dialog, which) -> dialog.dismiss())
                        .show());
    }

    public void showToast(String text) {
        BackgroundThreadHandler.postOnMainThread(() -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show());
    }

    public void showConfirmationAlert(int title, int message, @NonNull final Consumer<Boolean> consumer) {
        BackgroundThreadHandler.postOnMainThread(() ->
                new AlertDialog.Builder(this)
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
        Intent redirectUriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(redirectUriIntent);
    }

    public void selectCredentialForPresentation(List<Claim> credentials, DefaultCredentialPicker.OnCredentialPicked itemPickerListener) {

        Credential[] claims = credentials.stream()
                .filter(claim -> claim.getData().get(ClaimKeys.cardType) != null)
                .map(claim -> new Credential(claim, false))
                .toArray(Credential[]::new);

        openPicker(claims, itemPickerListener);
    }

    private void openPicker(Credential[] claims, DefaultCredentialPicker.OnCredentialPicked itemPickerListener) {
        navController.navigate(HomeFragmentDirections.actionHomeFragmentToItemPickerFragment(claims));

        BackgroundThreadHandler.postOnMainThread(() -> {
            PickerSharedViewModel pickerSharedViewModel = new ViewModelProvider(this).get(PickerSharedViewModel.class);
            pickerSharedViewModel.getPickedCredential().observe(this, claim -> {
                if (claim != null) {
                    pickerSharedViewModel.clearCredential();
                    itemPickerListener.onPicked(claim);
                    pickerSharedViewModel.getPickedCredential().removeObservers(this);
                }
            });
        });
    }

}