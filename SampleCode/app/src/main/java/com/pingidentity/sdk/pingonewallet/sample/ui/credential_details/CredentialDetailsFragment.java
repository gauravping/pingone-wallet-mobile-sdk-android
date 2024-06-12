package com.pingidentity.sdk.pingonewallet.sample.ui.credential_details;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentCredentialDetailsBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.CredentialDetailsAdapter;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

import java.util.Map;

public class CredentialDetailsFragment extends BaseFragment<FragmentCredentialDetailsBinding, CredentialDetailsViewModel> {

    public static final String TAG = CredentialDetailsFragment.class.getCanonicalName();

    public enum CredentialDetailsAction {ACCEPT, DELETE}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Credential credential = CredentialDetailsFragmentArgs.fromBundle(getArguments()).getCredential();
        mViewModel.setup(credential, CredentialDetailsAction.DELETE);
        setClaimData(credential);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mViewModel.navigateBack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

    }

    @Override
    public FragmentCredentialDetailsBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentCredentialDetailsBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void setClaimData(Credential credential) {
        getViewBinding().btnAction.setText(R.string.delete_claim);

        Bitmap image = BitmapUtil.getBitmapFromClaim(credential.getClaim());
        if (image != null) {
            getViewBinding().credentialImage.setImageBitmap(image);
        }
        getViewBinding().viewExpired.setVisibility(credential.isRevoked() ? View.VISIBLE : View.GONE);

        getViewBinding().btnAction.setOnClickListener(v -> mViewModel.performAction());

        setRecyclerView(credential.getClaim().getData());
    }

    private void setRecyclerView(Map<String, String> data) {
        CredentialDetailsAdapter adapter = new CredentialDetailsAdapter(data);
        getViewBinding().claimsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getViewBinding().claimsList.setAdapter(adapter);
    }

}