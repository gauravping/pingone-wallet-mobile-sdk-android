package com.pingidentity.sdk.pingonewallet.sample.ui.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewbinding.ViewBinding;

import com.pingidentity.sdk.pingonewallet.sample.di.Injector;
import com.pingidentity.sdk.pingonewallet.sample.di.component.DaggerFragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.module.FragmentModule;
import com.pingidentity.sdk.pingonewallet.sample.models.navigation.NavigationCommand;

import javax.inject.Inject;

public abstract class BaseFragment<T extends ViewBinding, V extends BaseViewModel> extends Fragment {

    @Inject
    protected V mViewModel;

    private T mViewDataBinding;

    public static final String TAG = BaseFragment.class.getCanonicalName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        performDependencyInjection(getBuildComponent());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mViewDataBinding = this.performBinding(inflater, container);
        observeNavigation();
        return mViewDataBinding.getRoot();
    }

    public abstract T performBinding(@NonNull LayoutInflater inflater, ViewGroup container);

    public abstract void performDependencyInjection(FragmentComponent buildComponent);


    public T getViewBinding() {
        return mViewDataBinding;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            try {
                Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
                int returnVal = p1.waitFor();
                return (returnVal == 0);
            } catch (Exception e) {
                Log.e(TAG, "Unable to connect to Internet", e);
            }
        }
        return false;
    }

    private void observeNavigation() {
        mViewModel.getNavigation().observe(getViewLifecycleOwner(), navigationCommandEvent -> {
            NavigationCommand navigationCommand = navigationCommandEvent.getContentIfNotHandled();
            if (navigationCommand != null) {
                handleNavigation(navigationCommand);
            }
        });
    }

    private void handleNavigation(NavigationCommand navCommand) {
        if(navCommand instanceof NavigationCommand.ToDirection){
            Navigation.findNavController(mViewDataBinding.getRoot()).navigate(((NavigationCommand.ToDirection) navCommand).directions);
        } else if(navCommand instanceof NavigationCommand.Back){
            Navigation.findNavController(mViewDataBinding.getRoot()).navigateUp();
        }
    }

    private FragmentComponent getBuildComponent() {
        return DaggerFragmentComponent.builder()
                .appComponent(Injector.getAppComponent())
                .fragmentModule(new FragmentModule(this))
                .build();
    }

}