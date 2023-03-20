package com.pingidentity.sdk.pingonewallet.sample.di.component;

import dagger.Component;

import com.pingidentity.sdk.pingonewallet.sample.di.scope.FragmentScope;
import com.pingidentity.sdk.pingonewallet.sample.di.module.FragmentModule;
import com.pingidentity.sdk.pingonewallet.sample.ui.create_profile.CreateProfileFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.credentials_list.CredentialsListFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.initial.InitialFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.item_picker.ItemPickerFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.splash.SplashFragment;

@FragmentScope
@Component(modules = FragmentModule.class, dependencies = AppComponent.class)
public interface FragmentComponent {

    void inject(CreateProfileFragment fragment);

    void inject(CredentialDetailsFragment fragment);

    void inject(CredentialsListFragment fragment);

    void inject(InitialFragment fragment);

    void inject(SplashFragment fragment);

    void inject(ItemPickerFragment fragment);

}
