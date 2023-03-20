package com.pingidentity.sdk.pingonewallet.sample.ui.create_profile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentProfileBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.models.Profile;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

public class CreateProfileFragment extends BaseFragment<FragmentProfileBinding, CreateProfileViewModel> {

    public static final String TAG = CreateProfileFragment.class.getSimpleName();
    public static final String CAMERA_INTENT = "android.intent.extras.CAMERA_FACING";

    private Bitmap mSelfie;

    private final ActivityResultLauncher<String> permissionResult =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (Boolean.TRUE.equals(result)) {
                    dispatchTakePictureIntent();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("data")) {
                        mSelfie = (Bitmap) data.getExtras().get("data");
                        getViewBinding().selfiePreview.setImageBitmap(mSelfie);
                    }
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
        setClickListeners();
    }

    @Override
    public FragmentProfileBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void observeViewModel() {
        mViewModel.subscribeProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                setExistingProfile(profile);
            }
        });
        mViewModel.subscribeShowDialog().observe(getViewLifecycleOwner(), message ->
                showAlert(getString(R.string.error_missing_info), message));
        mViewModel.subscribeFlowCompletion().observe(getViewLifecycleOwner(), this::navigateBack);
    }

    private void navigateBack(boolean success) {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void setClickListeners() {
        getViewBinding().btnSelfieCapture.setOnClickListener(v -> checkPermission());
        getViewBinding().btnCreateProfile.setOnClickListener(v -> createProfile());
    }

    private void setExistingProfile(Profile profile) {
        getViewBinding().btnCreateProfile.setText(R.string.profile_update);
        getViewBinding().editFirstName.setText(profile.getFirstName());
        getViewBinding().editLastName.setText(profile.getLastName());
        getViewBinding().editEmail.setText(profile.getEmail());
        mSelfie = profile.getSelfie();
        getViewBinding().selfiePreview.setImageBitmap(mSelfie);
    }

    private void createProfile() {
        mViewModel.createProfile(
                mSelfie,
                getViewBinding().editFirstName.getText().toString(),
                getViewBinding().editLastName.getText().toString(),
                getViewBinding().editEmail.getText().toString());
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(CAMERA_INTENT, 1);
        cameraLauncher.launch(intent);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            permissionResult.launch(Manifest.permission.CAMERA);
        }
    }

}