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
import androidx.viewbinding.ViewBinding;

import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.di.Injector;
import com.pingidentity.sdk.pingonewallet.sample.di.component.DaggerFragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.component.FragmentComponent;
import com.pingidentity.sdk.pingonewallet.sample.di.module.FragmentModule;
import com.pingidentity.sdk.pingonewallet.sample.utils.NotificationUtil;

import javax.inject.Inject;

public abstract class BaseFragment<T extends ViewBinding, V extends BaseViewModel> extends Fragment {

    @Inject
    protected V mViewModel;
    @Inject
    protected NotificationUtil mNotificationUtil;

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
        return mViewDataBinding.getRoot();
    }

    public abstract T performBinding(@NonNull LayoutInflater inflater, ViewGroup container);

    public T getViewBinding() {
        return mViewDataBinding;
    }

    public void showAlert(int title, int message) {
        showAlert(getString(title), getString(message));
    }

    public void showAlert(String title, String message) {
        mNotificationUtil.showAlert(title, message);
    }

    public abstract void performDependencyInjection(FragmentComponent buildComponent);

    private FragmentComponent getBuildComponent() {
        return DaggerFragmentComponent.builder()
                .appComponent(Injector.getAppComponent())
                .fragmentModule(new FragmentModule(this))
                .build();
    }

    public void replaceFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, fragment.getClass().getCanonicalName())
                .addToBackStack(null)
                .commit();
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

}