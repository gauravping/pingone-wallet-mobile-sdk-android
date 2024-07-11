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

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.pingidentity.sdk.pingonewallet.sample.R;
import com.pingidentity.sdk.pingonewallet.sample.ui.MainActivity;
import com.pingidentity.sdk.pingonewallet.sample.wallet.interfaces.NotificationServiceHelper;

public class PingOneNotificationService extends FirebaseMessagingService {

    private static final String TAG = PingOneNotificationService.class.getCanonicalName();
    private static final String CHANNEL_ID = "pingone_wallet_channel";

    private static final NotificationServiceHelper notificationServiceHelper = new NotificationServiceHelperImpl();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New Token: " + token);
        notificationServiceHelper.updatePushToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "notificationData: " + (remoteMessage.getData()));
        notificationServiceHelper.updateNotificationData(remoteMessage.getData());
        NotificationMessage notificationMessage = new Gson().fromJson(remoteMessage.getData().get("aps"), NotificationMessage.class);
        sendNotification(notificationMessage);
        super.onMessageReceived(remoteMessage);
    }

    public static NotificationServiceHelper getNotificationServiceHelper() {
        return notificationServiceHelper;
    }
    public static void fetchNewToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token;

            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to fetch current token", task.getException());
            } else {
                token = task.getResult();
                if (token != null) {
                    notificationServiceHelper.updatePushToken(token);
                }
                Log.d(TAG, "Push Token retrieved: " + token);
            }
        });
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
