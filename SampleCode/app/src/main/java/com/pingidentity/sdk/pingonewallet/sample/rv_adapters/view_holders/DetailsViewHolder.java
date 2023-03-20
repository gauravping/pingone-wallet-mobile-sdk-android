package com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemCredentialDetailsBinding;

public class DetailsViewHolder extends RecyclerView.ViewHolder {

    private final ItemCredentialDetailsBinding mBinding;

    public DetailsViewHolder(ItemCredentialDetailsBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public void bind(String key, String value) {
        mBinding.txtKey.setText(key);
        mBinding.txtValue.setText(value);
    }

}
