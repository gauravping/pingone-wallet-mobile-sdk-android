package com.pingidentity.sdk.pingonewallet.sample.di.module;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import dagger.Module;
import dagger.Provides;

import com.pingidentity.sdk.pingonewallet.sample.storage.data_repository.DataRepository;
import com.pingidentity.sdk.pingonewallet.sample.ui.base.BaseFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.create_profile.CreateProfileViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list.CredentialsListViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.initial.InitialViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.item_picker.ItemPickerViewModel;
import com.pingidentity.sdk.pingonewallet.sample.ui.splash.SplashViewModel;
import com.pingidentity.sdk.pingonewallet.sample.di.ViewModelProviderFactory;
import com.pingidentity.sdk.pingonewallet.sample.utils.NotificationUtil;

@Module
public class FragmentModule {

    private final BaseFragment<?, ?> mFragment;
    private final NotificationUtil mNotificationUtil;

    public FragmentModule(BaseFragment<?, ?> fragment) {
        this.mFragment = fragment;
        this.mNotificationUtil = NotificationUtil.getInstance(mFragment.requireActivity());
    }

    @Provides
    NotificationUtil provideNotificationUtil() {
        return mNotificationUtil;
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager() {
        return new LinearLayoutManager(mFragment.requireActivity());
    }

    @Provides
    CreateProfileViewModel provideCreateProfileViewModel(DataRepository dataRepository) {
        Supplier<CreateProfileViewModel> supplier = () -> new CreateProfileViewModel(dataRepository);
        ViewModelProviderFactory<CreateProfileViewModel> factory = new ViewModelProviderFactory<>(CreateProfileViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(CreateProfileViewModel.class);
    }

    @Provides
    CredentialDetailsViewModel provideCredentialDetailsViewModel(DataRepository dataRepository) {
        Supplier<CredentialDetailsViewModel> supplier = () -> new CredentialDetailsViewModel(dataRepository);
        ViewModelProviderFactory<CredentialDetailsViewModel> factory = new ViewModelProviderFactory<>(CredentialDetailsViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(CredentialDetailsViewModel.class);
    }

    @Provides
    InitialViewModel provideInitialViewModel(DataRepository dataRepository) {
        Supplier<InitialViewModel> supplier = () -> new InitialViewModel(dataRepository);
        ViewModelProviderFactory<InitialViewModel> factory = new ViewModelProviderFactory<>(InitialViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(InitialViewModel.class);
    }

    @Provides
    CredentialsListViewModel provideCredentialsListViewModel(DataRepository dataRepository) {
        Supplier<CredentialsListViewModel> supplier = () -> new CredentialsListViewModel(dataRepository);
        ViewModelProviderFactory<CredentialsListViewModel> factory = new ViewModelProviderFactory<>(CredentialsListViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(CredentialsListViewModel.class);
    }

    @Provides
    ItemPickerViewModel provideItemPickerViewModel(DataRepository dataRepository) {
        Supplier<ItemPickerViewModel> supplier = () -> new ItemPickerViewModel(dataRepository);
        ViewModelProviderFactory<ItemPickerViewModel> factory = new ViewModelProviderFactory<>(ItemPickerViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(ItemPickerViewModel.class);
    }

    @Provides
    SplashViewModel provideSplashViewModel(DataRepository dataRepository) {
        Supplier<SplashViewModel> supplier = () -> new SplashViewModel(dataRepository);
        ViewModelProviderFactory<SplashViewModel> factory = new ViewModelProviderFactory<>(SplashViewModel.class, supplier);
        return new ViewModelProvider(mFragment, factory).get(SplashViewModel.class);
    }

}
