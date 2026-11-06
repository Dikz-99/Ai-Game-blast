package com.example.ui.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.capture.service.ScreenCaptureService
import com.example.core.model.GameState
import com.example.core.safety.SafetyController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FloatingAssistantService : Service() {

    companion object {
        const val CHANNEL_ID = "floating_assistant_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_SHOW = "ACTION_SHOW_FLOATING"
        const val ACTION_HIDE = "ACTION_HIDE_FLOATING"

        private val _isFloatingVisible = MutableStateFlow(false)
        val isFloatingVisible: StateFlow<Boolean> = _isFloatingVisible.asStateFlow()
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                startForegroundNotification()
                showFloatingWindow()
                _isFloatingVisible.value = true
            }
            ACTION_HIDE -> {
                removeFloatingWindow()
                _isFloatingVisible.value = false
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Game Blast HUD")
            .setContentText("Floating overlay is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showFloatingWindow() {
        if (floatingView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        // Programmatic HUD Container for smooth standalone overlay performance
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E60B0C12"))
            setPadding(28, 20, 28, 20)
            elevation = 16f
        }

        val title = TextView(this).apply {
            text = "⚡ AI BLAST"
            setTextColor(Color.parseColor("#00E5FF"))
            typeface = Typeface.DEFAULT_BOLD
            textSize = 13f
        }

        val status = TextView(this).apply {
            text = "● MONITORING"
            setTextColor(Color.parseColor("#00E676"))
            textSize = 11f
        }

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 0)
        }

        val stopButton = Button(this).apply {
            text = "KILL"
            setBackgroundColor(Color.parseColor("#FF2A55"))
            setTextColor(Color.WHITE)
            textSize = 11f
            setOnClickListener {
                SafetyController.triggerEmergencyStop("Triggered from Floating HUD")
                ScreenCaptureService.setGameState(GameState.EMERGENCY_STOPPED)
                status.text = "● STOPPED"
                status.setTextColor(Color.parseColor("#FF2A55"))
            }
        }

        val pauseButton = Button(this).apply {
            text = "PAUSE"
            setBackgroundColor(Color.parseColor("#1F2438"))
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 11f
            setOnClickListener {
                if (text == "PAUSE") {
                    ScreenCaptureService.setGameState(GameState.PAUSED)
                    text = "RESUME"
                    status.text = "● PAUSED"
                    status.setTextColor(Color.parseColor("#FFB703"))
                } else {
                    ScreenCaptureService.setGameState(GameState.SCANNING)
                    text = "PAUSE"
                    status.text = "● ACTIVE"
                    status.setTextColor(Color.parseColor("#00E676"))
                }
            }
        }

        btnRow.addView(pauseButton)
        btnRow.addView(stopButton)

        container.addView(title)
        container.addView(status)
        container.addView(btnRow)

        // Make floating container draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                else -> false
            }
        }

        floatingView = container
        try {
            windowManager?.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeFloatingWindow() {
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            floatingView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI Blast Floating HUD",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingWindow()
    }
}
