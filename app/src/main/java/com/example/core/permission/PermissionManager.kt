package com.example.core.permission

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.automation.service.GameAutomationAccessibilityService

data class PermissionStatus(
    val hasOverlayPermission: Boolean,
    val hasAccessibilityPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isAllEssentialGranted: Boolean
)

object PermissionManager {

    fun checkPermissions(context: Context): PermissionStatus {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val hasAccessibility = isAccessibilityServiceEnabled(context)

        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val isAll = hasOverlay && hasAccessibility && hasNotification

        return PermissionStatus(
            hasOverlayPermission = hasOverlay,
            hasAccessibilityPermission = hasAccessibility,
            hasNotificationPermission = hasNotification,
            isAllEssentialGranted = isAll
        )
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (GameAutomationAccessibilityService.instance != null) {
            return true
        }

        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedPackage = context.packageName

        return enabledServices.any { service ->
            service.resolveInfo?.serviceInfo?.packageName == expectedPackage
        }
    }

    fun openOverlaySettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun openAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
