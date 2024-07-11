package com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.Map;

public interface NotificationServiceHelper {

    void updatePushToken(@NonNull final String pushToken);
    LiveData<String> getPushToken();
    void updateNotificationData(Map<String, String> notificationData);
    LiveData<Map<String, String>> getNotificationData();
    void clearNotificationData();
}
