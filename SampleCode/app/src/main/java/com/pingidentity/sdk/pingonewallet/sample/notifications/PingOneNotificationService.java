package com.pingidentity.sdk.pingonewallet.sample.notifications;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class PingOneNotificationService extends FirebaseMessagingService {

    private static final String TAG = PingOneNotificationService.class.getCanonicalName();

    private static final MutableLiveData<String> pushToken = new MutableLiveData<>();
    private static final MutableLiveData<Map<String, String>> notificationData = new MutableLiveData<>();

    public static void updatePushToken(@NonNull final String pushToken) {
        PingOneNotificationService.pushToken.postValue(pushToken);
    }

    public static LiveData<String> getPushToken() {
        return pushToken;
    }

    public static LiveData<Map<String, String>> getNotificationData() {
        Log.e(TAG, "notificationData: " + (notificationData.hasActiveObservers()));
        return notificationData;
    }

    public static void clearNotificationData() {
        notificationData.postValue(null);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, "notificationData: " + (remoteMessage));
        notificationData.postValue(remoteMessage.getData());
    }

}
