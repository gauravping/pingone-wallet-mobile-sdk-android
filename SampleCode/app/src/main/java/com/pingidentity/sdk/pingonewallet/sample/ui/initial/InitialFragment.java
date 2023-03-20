package com.pingidentity.sdk.pingonewallet.sample.ui.initial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentInitBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.create_profile.CreateProfileFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list.CredentialsListFragment;

public class InitialFragment extends BaseFragment<FragmentInitBinding, InitialViewModel> {

    public static final String TAG = InitialFragment.class.getCanonicalName();

    private final ActivityResultLauncher<String> requestPermissionNotification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (Boolean.FALSE.equals(isGranted)){
                    showAlert("Warning","Push notifications permission has been denied, app may exhibit unexpected behavior");
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getViewBinding().btnCreateProfile.setOnClickListener(v -> replaceFragment(new CreateProfileFragment()));
    }

    @Override
    public FragmentInitBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentInitBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mViewModel.getProfile() != null) {
            replaceFragment(CredentialsListFragment.newInstance());
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            requestPermissionNotification.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

}