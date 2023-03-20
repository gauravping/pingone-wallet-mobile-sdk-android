package com.pingidentity.sdk.pingonewallet.sample.rv_adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders.ItemPickerViewHolder;
import com.pingidentity.sdk.pingonewallet.sample.ui.item_picker.ItemPickerFragment;

import java.util.List;

public class ItemPickerAdapter extends RecyclerView.Adapter<ItemPickerViewHolder> {

    private final List<Claim> mClaimList;
    private final ItemPickerFragment.ItemPickerListener mItemPickerListener;

    public ItemPickerAdapter(List<Claim> data, ItemPickerFragment.ItemPickerListener itemPickerListener) {
        this.mClaimList = data;
        this.mItemPickerListener = itemPickerListener;
    }

    @NonNull
    @Override
    public ItemPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPickerBinding binding = ItemPickerBinding.inflate(inflater, parent, false);
        return new ItemPickerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemPickerViewHolder holder, int position) {
        holder.bind(mClaimList.get(position), mItemPickerListener);
    }

    @Override
    public int getItemCount() {
        return mClaimList.size();
    }

}
