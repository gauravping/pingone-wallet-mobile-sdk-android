package com.pingidentity.sdk.pingonewallet.sample.rv_adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemCredentialDetailsBinding;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders.DetailsViewHolder;
import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CredentialDetailsAdapter extends RecyclerView.Adapter<DetailsViewHolder> {

    final Map<String, String> mClaimData;
    final List<String> orderedKeys;

    public CredentialDetailsAdapter(Map<String, String> data) {
        mClaimData = data;
        orderedKeys = data.keySet().stream()
                .filter(s -> !BitmapUtil.getImageKeys().contains(s.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCredentialDetailsBinding binding = ItemCredentialDetailsBinding.inflate(inflater, parent, false);
        return new DetailsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder holder, int position) {
        String key = orderedKeys.get(position);
        String value = mClaimData.getOrDefault(key, "");
        holder.bind(key, value);
    }

    @Override
    public int getItemCount() {
        return orderedKeys.size();
    }

}
