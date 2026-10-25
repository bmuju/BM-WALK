package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.TrackingStatus
import com.example.util.GpsMetricsCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var trackingManager: TrackingManager
    private val notificationId = 1001
    private val channelId = "bm_walk_tracking_channel"

    override fun onCreate() {
        super.onCreate()
        trackingManager = TrackingManager.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> {
                startForegroundWithNotification()
                observeMetrics()
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("Acquiring GPS Signal...", "Move Beyond Ordinary")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun observeMetrics() {
        scope.launch {
            trackingManager.metrics.collectLatest { metrics ->
                val status = trackingManager.status.value
                if (status == TrackingStatus.TRACKING || status == TrackingStatus.PAUSED) {
                    val statusText = if (status == TrackingStatus.PAUSED) "[PAUSED] " else ""
                    val title = "${statusText}BM × WALK • ${GpsMetricsCalculator.formatDistance(metrics.distanceMeters)}"
                    val content = "Time: ${GpsMetricsCalculator.formatDuration(metrics.durationSeconds)} | Pace: ${GpsMetricsCalculator.formatPace(metrics.avgPaceSecondsPerKm)}"
                    updateNotification(title, content)
                }
            }
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildNotification(title, content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Active Workout Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live distance, time, and pace while recording a walk"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_STOP = "com.example.service.action.STOP"

        fun startService(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }
}
