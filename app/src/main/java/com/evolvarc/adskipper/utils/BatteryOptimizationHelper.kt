package com.evolvarc.adskipper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

object BatteryOptimizationHelper {
    
    private const val TAG = "BatteryOptimization"
    
    /**
     * Check if the app is exempted from battery optimization
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = pm.isIgnoringBatteryOptimizations(context.packageName)
            Log.d(TAG, "Battery optimization status: ${if (isIgnoring) "Disabled (Good)" else "Enabled (May affect service)"}")
            return isIgnoring
        }
        // Battery optimization doesn't exist on Android 5.x and below
        return true
    }
    
    /**
     * Request the user to disable battery optimization for this app
     */
    fun requestBatteryOptimizationExemption(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                // Try direct exemption request first
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened battery optimization exemption dialog")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to open direct exemption, trying settings: ${e.message}")
                try {
                    // Fallback to battery optimization settings page
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to open any battery settings: ${e2.message}")
                }
            }
        }
    }
    
    /**
     * Get manufacturer-specific instructions for disabling battery optimization
     * Different OEMs have different settings locations and additional restrictions
     */
    fun getManufacturerSpecificInstructions(context: Context): String? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("samsung") -> 
                "Samsung Tip: Go to Settings → Apps → AdSkipper → Battery → Optimize battery usage → All apps → AdSkipper → Don't optimize"
            
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> 
                "Xiaomi/Redmi/POCO Tip: Settings → Apps → Manage apps → AdSkipper → Battery saver → No restrictions. Also: Settings → Additional settings → Battery & performance → Manage apps battery usage → AdSkipper → No restrictions"
            
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> 
                "Huawei/Honor Tip: Settings → Apps → AdSkipper → Battery → App launch → Manage manually (enable Auto-launch, Secondary launch, Run in background)"
            
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> 
                "Oppo/Realme Tip: Settings → Battery → Power saving mode → AdSkipper → Don't optimize. Also check: Settings → App Management → AdSkipper → Battery usage → Allow background activity"
            
            manufacturer.contains("vivo") -> 
                "Vivo Tip: Settings → Battery → Background power consumption management → AdSkipper → Allow high background power consumption"
            
            manufacturer.contains("oneplus") -> 
                "OnePlus Tip: Settings → Battery → Battery optimization → AdSkipper → Don't optimize. Also: Settings → Apps → AdSkipper → Battery usage → Allow background activity"
            
            manufacturer.contains("asus") -> 
                "Asus Tip: Auto-start Manager → AdSkipper → Allow. Also: Mobile Manager → PowerMaster → Auto-start → AdSkipper → Allow"
            
            manufacturer.contains("nokia") -> 
                "Nokia Tip: Settings → Apps & notifications → AdSkipper → Advanced → Battery → Battery optimization → Not optimized"
            
            manufacturer.contains("motorola") -> 
                "Motorola Tip: Settings → Battery → Battery optimization → Not optimized → All apps → AdSkipper → Don't optimize"
            
            else -> null
        }
    }
    
    /**
     * Get a user-friendly explanation of why battery optimization matters
     */
    fun getExplanation(): String {
        return """
            Battery optimization can prevent AdSkipper from running in the background, which means it won't be able to skip ads when you watch YouTube.
            
            For best performance, we recommend disabling battery optimization for AdSkipper. This will ensure the service runs reliably without being stopped by the system.
            
            Don't worry - AdSkipper is designed to be lightweight and won't significantly impact your battery life.
        """.trimIndent()
    }
}
