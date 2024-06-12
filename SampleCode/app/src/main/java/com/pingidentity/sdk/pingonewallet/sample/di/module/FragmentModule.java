package com.pingidentity.sdk.pingonewallet.sample.di.module;

import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import dagger.Module;
import dagger.Provides;

import com.pingidentity.sdk.pingonewallet.sample.ui.home.HomeViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.PickerViewModel;
import com.pingidentity.sdk.pingonewallet.sample.wallet.PingOneWalletHelper;
import com.pingidentity.sdk.pingonewallet.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsViewModel;
import com.pingidentity.sdk.pingonewallet.sample.utils.ViewModelProviderFactory;

@Module
public class FragmentModule {

    private final BaseFragment<?, ?> mFragment;

    public FragmentModule(BaseFragment<?, ?> fragment) {
        this.mFragment = fragment;
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager() {
        return new LinearLayoutManager(mFragment.requireActivity());
    }

    @Provides
    CredentialDetailsViewModel provideCredentialDetailsViewModel(@Nullable PingOneWalletHelper pingOneWalletHelper) {
        Supplier<CredentialDetailsViewModel> supplier = () -> new CredentialDetailsViewModel(pingOneWalletHelper);
        ViewModelProviderFactory<CredentialDetailsViewModel> factory = new ViewModelProviderFactory<>(CredentialDetailsViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(CredentialDetailsViewModel.class);
    }

    @Provides
    HomeViewModel provideCredentialsListViewModel(@Nullable PingOneWalletHelper pingOneWalletHelper) {
        Supplier<HomeViewModel> supplier = () -> new HomeViewModel(pingOneWalletHelper);
        ViewModelProviderFactory<HomeViewModel> factory = new ViewModelProviderFactory<>(HomeViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(HomeViewModel.class);
    }

    @Provides
    PickerViewModel provideItemPickerViewModel(@Nullable PingOneWalletHelper pingOneWalletHelper) {
        Supplier<PickerViewModel> supplier = () -> new PickerViewModel(pingOneWalletHelper);
        ViewModelProviderFactory<PickerViewModel> factory = new ViewModelProviderFactory<>(PickerViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(PickerViewModel.class);
    }

}
