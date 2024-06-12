package com.pingidentity.sdk.pingonewallet.sample.ui.qr_scanner;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QrScannerSharedViewModel extends ViewModel {

    private final MutableLiveData<String> qrDataLiveData = new MutableLiveData<>();
    private final MutableLiveData<Exception> exceptionLiveData = new MutableLiveData<>();

    public MutableLiveData<String> getQrData() {
        return qrDataLiveData;
    }

    public void setQrData(String qrData) {
        this.qrDataLiveData.postValue(qrData);
    }

    public MutableLiveData<Exception> getException() {
        return exceptionLiveData;
    }

    public void setException(Exception exception) {
        this.exceptionLiveData.postValue(exception);
    }
}
