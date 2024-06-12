package com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders;

import android.graphics.Bitmap;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemCredentialCardBinding;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

import java.util.function.Consumer;

public class CardViewHolder extends RecyclerView.ViewHolder {

    private final ItemCredentialCardBinding mBinding;

    public CardViewHolder(ItemCredentialCardBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public void bind(Credential credential, Consumer<Credential> callback) {
        String cardType = credential.getClaim().getData().getOrDefault("CardType", "");
        mBinding.txtCardTitle.setText(cardType);
        Bitmap image = BitmapUtil.getBitmapFromClaim(credential.getClaim());
        if (image != null) {
            mBinding.cardFrontImage.setVisibility(View.VISIBLE);
            mBinding.cardFrontImage.setImageBitmap(image);
        } else {
            mBinding.cardFrontImage.setVisibility(View.GONE);
        }
        mBinding.viewExpired.setVisibility(credential.isRevoked() ? View.VISIBLE : View.GONE);
        mBinding.layoutDocument.setOnClickListener(v -> callback.accept(credential));
    }

}