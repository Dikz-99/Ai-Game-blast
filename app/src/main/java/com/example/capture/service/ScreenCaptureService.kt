package com.example.capture.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.capture.engine.ScreenCaptureEngine
import com.example.core.model.GameState
import com.example.core.safety.SafetyController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenCaptureService : Service() {

    companion object {
        const val CHANNEL_ID = "game_blast_capture_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_CAPTURE"
        const val ACTION_STOP = "ACTION_STOP_CAPTURE"
        const val ACTION_EMERGENCY_STOP = "ACTION_EMERGENCY_STOP"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        const val EXTRA_TARGET_FPS = "EXTRA_TARGET_FPS"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _currentGameState = MutableStateFlow(GameState.IDLE)
        val currentGameState: StateFlow<GameState> = _currentGameState.asStateFlow()

        fun setGameState(state: GameState) {
            _currentGameState.value = state
        }
    }

    private var mediaProjection: MediaProjection? = null
    private var captureEngine: ScreenCaptureEngine? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val targetFps = intent.getIntExtra(EXTRA_TARGET_FPS, 10)
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                if (resultCode != 0 && resultData != null) {
                    startForegroundServiceWithNotification()
                    initMediaProjection(resultCode, resultData, targetFps)
                    _isServiceRunning.value = true
                    _currentGameState.value = GameState.SCANNING
                }
            }
            ACTION_STOP -> {
                stopCapture()
                stopSelf()
            }
            ACTION_EMERGENCY_STOP -> {
                SafetyController.triggerEmergencyStop("Triggered from notification kill switch")
                _currentGameState.value = GameState.EMERGENCY_STOPPED
                stopCapture()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification("AI Game Blast is active", "Capturing & analyzing screen frame buffer")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initMediaProjection(resultCode: Int, data: Intent, targetFps: Int) {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                stopCapture()
            }
        }, null)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val densityDpi = metrics.densityDpi

        mediaProjection?.let { mp ->
            captureEngine = ScreenCaptureEngine(
                mediaProjection = mp,
                screenWidth = width,
                screenHeight = height,
                screenDensityDpi = densityDpi,
                targetFps = targetFps
            )
            captureEngine?.start()
        }
    }

    private fun stopCapture() {
        captureEngine?.stop()
        captureEngine = null
        mediaProjection?.stop()
        mediaProjection = null
        _isServiceRunning.value = false
        _currentGameState.value = GameState.IDLE
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emergencyIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_EMERGENCY_STOP
        }
        val pendingEmergency = PendingIntent.getService(
            this,
            1,
            emergencyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_delete, "EMERGENCY STOP", pendingEmergency)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Game Blast Automation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows live AI automation status and emergency stop control"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
    }
}
