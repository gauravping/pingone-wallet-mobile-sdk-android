package com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.MainApplication;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.CredentialDetailsListener;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.QrScannerCallback;
import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentCredentialsListBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.CardsListAdapter;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.qr_scanner.QrScannerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CredentialsListFragment extends BaseFragment<FragmentCredentialsListBinding, CredentialsListViewModel> implements QrScannerCallback {

    private final List<Credential> mDocuments = new ArrayList<>();

    public static CredentialsListFragment newInstance() {
        return new CredentialsListFragment();
    }

    final Observer<String> appOpenUrlObserver = url -> {
        if (url != null) {
            mViewModel.processUrl(url);
            ((MainApplication) requireActivity().getApplication()).setUrl(null);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNotificationUtil.updateFragmentManager(getChildFragmentManager());
        setCardsListAdapter();
        setupProfileView();
        processAppOpenUrl();
        retrieveData();
        setupClickListeners();
    }

    @Override
    public FragmentCredentialsListBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentCredentialsListBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void setupClickListeners() {
        getViewBinding().btnScanQr.setOnClickListener(view -> replaceFragment(QrScannerFragment.newInstance(this)));
    }

    private void processAppOpenUrl() {
        ((MainApplication) requireActivity().getApplication()).getUrl().observe(getViewLifecycleOwner(), appOpenUrlObserver);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void retrieveData() {
        mViewModel.getClaims().observe(getViewLifecycleOwner(), claims -> {
            if (claims == null) {
                return;
            }
            mDocuments.clear();
            mDocuments.addAll(claims.stream().map(claim ->
                            new Credential(claim, mViewModel.isClaimRevoked(claim.getId().toString())))
                    .collect(Collectors.toList()));
            mViewModel.resetClaimsLiveData();
            Objects.requireNonNull(getViewBinding().rvCredentials.getAdapter()).notifyDataSetChanged();
        });
    }

    private void setupProfileView() {
        Profile profile = mViewModel.getProfile();
        getViewBinding().txtProfileName.setText(profile.getFullName());
        getViewBinding().imgProfilePhoto.setImageBitmap(profile.getSelfie());
    }

    private void setCardsListAdapter() {
        CardsListAdapter adapter = new CardsListAdapter(mDocuments, credential ->
                replaceFragment(CredentialDetailsFragment.newInstance(
                        credential, getString(R.string.delete_claim), new CredentialDetailsListener() {
                            @Override
                            public void onActionClick(Claim claim) {
                                mViewModel.deleteClaim(claim);
                            }

                            @Override
                            public void onCancel() {
                                //Action not required
                            }
                        })
                )
        );
        getViewBinding().rvCredentials.setLayoutManager(new LinearLayoutManager(requireContext()));
        getViewBinding().rvCredentials.setAdapter(adapter);
    }

    @Override
    public void onQrScanned(String rawQrData) {
        mViewModel.processUrl(rawQrData);
    }

    @Override
    public void onError(Exception e) {
        // process qr error
    }

    @Override
    public void onCanceled() {
        // process qr cancel
    }

}