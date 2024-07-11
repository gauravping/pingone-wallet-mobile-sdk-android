package com.pingidentity.sdk.pingonewallet.sample.notifications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.NotificationServiceHelper;

import java.util.Map;

public class NotificationServiceHelperImpl implements NotificationServiceHelper {

    private static final String TAG = NotificationServiceHelper.class.getCanonicalName();
    private final MutableLiveData<String> pushToken = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> notificationData = new MutableLiveData<>();

    public void updatePushToken(@NonNull final String pushToken) {
        this.pushToken.postValue(pushToken);
    }

    public LiveData<String> getPushToken() {
        if (pushToken.getValue() == null) {
            PingOneNotificationService.fetchNewToken();
        }
        return pushToken;
    }

    public void updateNotificationData(Map<String, String> notificationData) {
        this.notificationData.postValue(notificationData);
    }

    public LiveData<Map<String, String>> getNotificationData() {
        Log.e(TAG, "notificationData: " + (notificationData.hasActiveObservers()));
        return notificationData;
    }

    public void clearNotificationData() {
        notificationData.postValue(null);
    }

}
