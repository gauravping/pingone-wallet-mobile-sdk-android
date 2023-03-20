package com.pingidentity.sdk.pingonewallet.sample.rv_adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingonewallet.sample.databinding.ItemCredentialCardBinding;
import com.pingidentity.sdk.pingonewallet.sample.models.Credential;
import com.pingidentity.sdk.pingonewallet.sample.rv_adapters.view_holders.CardViewHolder;
import com.pingidentity.sdk.pingonewallet.sample.callbacks.DocumentClickListener;

import java.util.List;

public class CardsListAdapter extends RecyclerView.Adapter<CardViewHolder> {

    public static final String TAG = CardsListAdapter.class.getCanonicalName();

    private final List<Credential> mCardsList;
    private final DocumentClickListener mCallback;

    public CardsListAdapter(final List<Credential> cardsList, DocumentClickListener callback) {
        this.mCardsList = cardsList;
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCredentialCardBinding binding = ItemCredentialCardBinding.inflate(inflater, parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(mCardsList.get(position), mCallback);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mCardsList.size();
    }

}
