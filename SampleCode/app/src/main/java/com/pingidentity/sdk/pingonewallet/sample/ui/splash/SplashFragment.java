package com.pingidentity.sdk.pingonewallet.sample.ui.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentSplashBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.storage.storage_manager.StorageManagerImpl;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.initial.InitialFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list.CredentialsListFragment;

public class SplashFragment extends BaseFragment<FragmentSplashBinding, SplashViewModel> {

    public static final String TAG = SplashFragment.class.getCanonicalName();

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean hasNetworkConnection = isNetworkConnected();
        if (hasNetworkConnection || StorageManagerImpl.getInstance().getApplicationInstance() != null) {
            mViewModel.init(mNotificationUtil);
        }
        if (!hasNetworkConnection) {
            showAlert("No Network Connection", "Network not available. App may behave oddly.");
        }
        observeUI();
    }

    @Override
    public FragmentSplashBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentSplashBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void observeUI() {
        mViewModel.subscribeSplashState().observe(getViewLifecycleOwner(), splashState -> {
            if (splashState == SplashState.OPEN_MAIN) {
                replaceFragment(new CredentialsListFragment());
            } else {
                replaceFragment(new InitialFragment());
            }
        });
    }

}