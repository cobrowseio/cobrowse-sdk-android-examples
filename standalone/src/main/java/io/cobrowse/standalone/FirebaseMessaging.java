package io.cobrowse.standalone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.cobrowse.CobrowseIO;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        CobrowseIO.instance().onPushNotification(remoteMessage.getData());

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification == null) {
            return;
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!manager.areNotificationsEnabled()) {
                Log.d(TAG, "Cannot show a notification because they are disabled");
            }
        }

        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                Build.VERSION.SDK_INT >= 23
                        ? PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
                        : PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        CobrowseIO.instance().setDeviceToken(token);
    }

}