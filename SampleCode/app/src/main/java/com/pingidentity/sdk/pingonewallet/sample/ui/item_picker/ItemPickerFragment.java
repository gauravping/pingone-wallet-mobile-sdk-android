package com.pingidentity.sdk.pingonewallet.sample.ui.item_picker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.databinding.FragmentItemPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.ItemPickerAdapter;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;

import java.util.List;

public class ItemPickerFragment extends BaseFragment<FragmentItemPickerBinding, ItemPickerViewModel> {

    public static final String TAG = ItemPickerFragment.class.getName();
    private ItemPickerListener mItemPickerListener;
    private List<Claim> mClaimList;

    public static ItemPickerFragment newInstance(List<Claim> claims, ItemPickerListener itemPickerListener) {
        ItemPickerFragment dialog = new ItemPickerFragment();
        dialog.mClaimList = claims;
        dialog.mItemPickerListener = itemPickerListener;
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRecyclerView();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                this.remove();
                requireActivity().onBackPressed();
                mItemPickerListener.onItemPicked(null);
            }
        });
    }

    @Override
    public FragmentItemPickerBinding performBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentItemPickerBinding.inflate(inflater, container, false);
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
            return new ItemPickerAdapter(mClaimList, claim -> {
                requireActivity().getSupportFragmentManager().popBackStack();
                mItemPickerListener.onItemPicked(claim);
            });

    }

    public interface ItemPickerListener {

        void onItemPicked(@Nullable final Claim claim);

    }
}
