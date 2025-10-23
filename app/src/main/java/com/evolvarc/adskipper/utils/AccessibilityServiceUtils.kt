package com.evolvarc.adskipper.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.evolvarc.adskipper.service.AdSkipperService

object AccessibilityServiceUtils {
    private const val TAG = "AccessibilityCheck"

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            Log.d(TAG, "Enabled services string: $enabledServices")

            if (enabledServices.isNullOrEmpty()) {
                Log.d(TAG, "No accessibility services enabled")
                return false
            }

            val packageName = context.packageName
            Log.d(TAG, "App package name: $packageName")

            // Create the expected component name in the proper format
            val componentName = ComponentName(packageName, "com.evolvarc.adskipper.service.AdSkipperService")
            val flattenedName = componentName.flattenToString()
            Log.d(TAG, "Looking for component: $flattenedName")

            // Parse the enabled services string (colon-separated list)
            val enabledServicesList = enabledServices.split(':')
            Log.d(TAG, "Number of enabled services: ${enabledServicesList.size}")

            for ((index, service) in enabledServicesList.withIndex()) {
                val trimmedService = service.trim()
                Log.d(TAG, "Service[$index]: '$trimmedService'")

                // Direct match
                if (trimmedService.equals(flattenedName, ignoreCase = true)) {
                    Log.d(TAG, "✓ Found exact match!")
                    return true
                }

                // Match by package name
                if (trimmedService.startsWith(packageName, ignoreCase = true)) {
                    Log.d(TAG, "✓ Found package name match!")
                    return true
                }

                // Match if contains AdSkipperService
                if (trimmedService.contains("AdSkipperService", ignoreCase = true)) {
                    Log.d(TAG, "✓ Found AdSkipperService substring match!")
                    return true
                }

                // Try alternative format matching
                if (trimmedService.contains(".service.AdSkipperService", ignoreCase = true)) {
                    Log.d(TAG, "✓ Found alternative format match!")
                    return true
                }
            }

            Log.d(TAG, "✗ Service NOT found in enabled list")
            return false

        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service: ${e.message}", e)
            return false
        }
    }
}
