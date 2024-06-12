package com.pingidentity.sdk.pingonewallet.sample.ui.picker;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pingidentity.did.sdk.types.Claim;

public class PickerSharedViewModel extends ViewModel {

    private final MutableLiveData<Claim> claimLiveData = new MutableLiveData<>();

    public MutableLiveData<Claim> getPickedCredential(){
        return claimLiveData;
    }

    public void setCredential(Claim claim){
        this.claimLiveData.postValue(claim);
    }

    public void clearCredential(){
        this.claimLiveData.setValue(null);
    }

}
