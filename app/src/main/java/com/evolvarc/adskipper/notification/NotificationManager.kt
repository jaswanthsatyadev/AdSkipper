package com.evolvarc.adskipper.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.evolvarc.adskipper.MainActivity
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.receivers.ServiceControlReceiver

object NotificationManager {

    private const val CHANNEL_ID = "AdSkipperChannel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AdSkipper Service Channel"
            val descriptionText = "Channel for AdSkipper foreground service notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getNotification(context: Context, adsSkipped: Int): Notification {
        val title = "AdSkipper Active"
        val text = "Watching for ads... Skipped: $adsSkipped today"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val pauseServiceIntent = Intent(context, ServiceControlReceiver::class.java).apply {
            action = ServiceControlReceiver.ACTION_PAUSE_SERVICE
        }
        val pauseServicePendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, 
            1, 
            pauseServiceIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_skip)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_skip, "Open App", openAppPendingIntent)
            .addAction(R.drawable.ic_skip, "Pause Service", pauseServicePendingIntent)
            .build()
    }
}
