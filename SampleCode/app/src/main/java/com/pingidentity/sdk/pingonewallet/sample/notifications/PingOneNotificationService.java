package com.pingidentity.sdk.pingonewallet.sample.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.ui.MainActivity;

import java.util.Map;

public class PingOneNotificationService extends FirebaseMessagingService {

    private static final String TAG = PingOneNotificationService.class.getCanonicalName();
    private static final String CHANNEL_ID = "pingone_wallet_channel";


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
        Log.e(TAG, "notificationData: " + (remoteMessage.getData()));
        notificationData.postValue(remoteMessage.getData());
        NotificationMessage notificationMessage = new Gson().fromJson(remoteMessage.getData().get("aps"), NotificationMessage.class);
        sendNotification(notificationMessage);
        super.onMessageReceived(remoteMessage);
    }

    private void sendNotification(NotificationMessage notificationMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ResourcesCompat.getColor(getResources(), R.color.app_color, getTheme()))
                        .setContentTitle(notificationMessage.getAlert().getTitle())
                        .setContentText(notificationMessage.getAlert().getBody())
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "PingOne Wallet",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, notificationBuilder.build());
    }

}
