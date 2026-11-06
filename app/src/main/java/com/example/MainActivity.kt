package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.core.permission.PermissionManager
import com.example.core.safety.SafetyController
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.CyberDark
import com.example.ui.theme.GameBlastTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startAiAutomation(result.resultCode, result.data!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Step 1: Configure App Check before UI or Firebase operations
        com.example.core.security.configureAppCheck(this, intent)

        enableEdgeToEdge()

        // Pass window bounds to SafetyController for boundary validation
        window.decorView.post {
            val width = window.decorView.width
            val height = window.decorView.height
            if (width > 0 && height > 0) {
                SafetyController.updateScreenDimensions(width, height)
            }
        }

        setContent {
            GameBlastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberDark
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        onRequestMediaProjection = { requestScreenCapture() },
                        onOpenOverlaySettings = {
                            startActivity(PermissionManager.openOverlaySettingsIntent(this))
                        },
                        onOpenAccessibilitySettings = {
                            startActivity(PermissionManager.openAccessibilitySettingsIntent())
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    private fun requestScreenCapture() {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(mpManager.createScreenCaptureIntent())
    }
}
