package com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders;

import android.graphics.Bitmap;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;
import com.pingidentity.sdk.pingonewallet.types.ClaimKeys;

import java.util.function.Consumer;

public class ItemPickerViewHolder extends RecyclerView.ViewHolder {

    private final ItemPickerBinding mBinding;

    public ItemPickerViewHolder(ItemPickerBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public void bind(Credential credential, Consumer<Claim> onResult) {
        String type = credential.getClaim().getData().get(ClaimKeys.cardType);
        Bitmap image = BitmapUtil.getBitmapFromClaim(credential.getClaim());
        mBinding.txtCardType.setText(type);
        mBinding.imgCard.setImageBitmap(image);
        mBinding.layout.setOnClickListener(view -> onResult.accept(credential.getClaim()));
    }
}
