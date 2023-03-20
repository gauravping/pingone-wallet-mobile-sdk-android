package com.pingidentity.sdk.pingonewallet.sample.callbacks;

public interface QrScannerCallback {

    void onQrScanned(String rawQrData);

    void onError(Exception e);

    void onCanceled();

}
