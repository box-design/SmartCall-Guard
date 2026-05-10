package com.smartcall.guard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smartcall.guard.MainActivity
import com.smartcall.guard.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GuardForegroundService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "guard_channel"
        const val CHANNEL_NAME = "守护服务"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createPersistentNotification(
            title = "来电守护运行中",
            content = "正在保护您的通话安全"
        )

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "来电守护前台服务通知"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createPersistentNotification(
        title: String,
        content: String
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntentFlags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    android.app.PendingIntent.FLAG_IMMUTABLE
                } else { 0 })

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, pendingIntentFlags
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
    }
}
