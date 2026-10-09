package io.cobrowse.sample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.cobrowse.CobrowseIO
import io.cobrowse.sample.data.getAndroidLogTag
import io.cobrowse.sample.ui.login.LoginActivity

/**
 * Firebase messaging service.
 */
class MainFirebaseMessagingService : FirebaseMessagingService() {

    @Suppress("PrivatePropertyName")
    private val Any.TAG: String
        get() = javaClass.getAndroidLogTag()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (CobrowseIO.isCobrowseNotification(remoteMessage.data)) {
            CobrowseIO.instance().onPushNotification(remoteMessage.data)
            return
        }

        // Check if message contains a notification payload
        val notification = remoteMessage.notification ?: return

        // Check if notifications are enabled
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!manager.areNotificationsEnabled()) {
                Log.d(TAG, "Cannot show a notification because they are disabled")
            }
        }

        // Since Android Oreo notification channel is needed.
        val channelId = getString(io.cobrowse.sample.core.R.string.default_notification_channel_id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(io.cobrowse.sample.core.R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        // Create and show a simple notification containing the received FCM message
        val intent = Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        manager.notify(0 /* ID of notification */, builder.build())
    }

    override fun onNewToken(token: String) {
        CobrowseIO.instance().setDeviceToken(token)
    }
}