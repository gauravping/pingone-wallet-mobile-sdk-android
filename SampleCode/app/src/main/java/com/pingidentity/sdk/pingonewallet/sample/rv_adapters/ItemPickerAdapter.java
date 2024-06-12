package com.pingidentity.sdk.pingonewallet.sample.rv_adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.did.sdk.types.Claim;
import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemPickerBinding;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders.ItemPickerViewHolder;

import java.util.List;
import java.util.function.Consumer;

public class ItemPickerAdapter extends RecyclerView.Adapter<ItemPickerViewHolder> {

    private final List<Credential> credentials;
    private final Consumer<Claim> onResult;

    public ItemPickerAdapter(List<Credential> data, Consumer<Claim> onResult) {
        this.credentials = data;
        this.onResult = onResult;
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
        holder.bind(credentials.get(position), onResult);
    }

    @Override
    public int getItemCount() {
        return credentials.size();
    }

}
