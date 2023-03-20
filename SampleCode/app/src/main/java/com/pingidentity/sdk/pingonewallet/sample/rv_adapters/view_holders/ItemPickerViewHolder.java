package com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders;

import android.graphics.Bitmap;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.ui.item_picker.ItemPickerFragment;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

public class ItemPickerViewHolder extends RecyclerView.ViewHolder {

    private final ItemPickerBinding mBinding;

    public ItemPickerViewHolder(ItemPickerBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public void bind(Claim claim, ItemPickerFragment.ItemPickerListener itemPickerListener) {
        String type = String.valueOf(claim.getData().get("CardType"));
        Bitmap image = BitmapUtil.getBitmapFromClaim(claim);
        mBinding.txtCardType.setText(type);
        mBinding.imgCard.setImageBitmap(image);
        mBinding.layout.setOnClickListener(view -> itemPickerListener.onItemPicked(claim));
    }
}
