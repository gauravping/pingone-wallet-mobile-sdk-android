package com.pingidentity.sdk.pingonewallet.sample.ui.picker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.ItemPickerAdapter;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;

import java.util.Arrays;
import java.util.List;

public class PickerFragment extends BaseFragment<FragmentPickerBinding, PickerViewModel> {

    public static final String TAG = PickerFragment.class.getName();

    private List<Credential> credentials;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        credentials = Arrays.asList(PickerFragmentArgs.fromBundle(getArguments()).getCredentialsList());
        setRecyclerView();
    }

    @Override
    public FragmentPickerBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentPickerBinding.inflate(inflater, container, false);
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void setRecyclerView() {
        ItemPickerAdapter adapter = getAdapter();
        getViewBinding().recyclerChooser.setLayoutManager(new LinearLayoutManager(requireContext()));
        getViewBinding().recyclerChooser.setAdapter(adapter);
    }

    private ItemPickerAdapter getAdapter() {
            return new ItemPickerAdapter(credentials, claim -> {
                mViewModel.navigateBack();
                new ViewModelProvider(requireActivity()).get(PickerSharedViewModel.class).setCredential(claim);
            });

    }

}
