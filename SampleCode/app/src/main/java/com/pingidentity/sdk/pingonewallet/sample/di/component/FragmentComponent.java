package com.pingidentity.sdk.pingonewallet.sample.di.component;

import dagger.Component;

import com.pingidentity.sdk.pingonewallet.sample.di.scope.FragmentScope;
import com.pingidentity.sdk.pingonewallet.sample.di.module.FragmentModule;
import com.pingidentity.sdk.pingonewallet.sample.ui.credential_details.CredentialDetailsFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.home.HomeFragment;
import com.pingidentity.sdk.pingonewallet.sample.ui.picker.PickerFragment;

@FragmentScope
@Component(modules = FragmentModule.class, dependencies = AppComponent.class)
public interface FragmentComponent {

    void inject(HomeFragment fragment);

    void inject(CredentialDetailsFragment fragment);

    void inject(PickerFragment fragment);

}
