package com.evolvarc.adskipper.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.evolvarc.adskipper.service.AdSkipperService

object AccessibilityServiceUtils {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = "${context.packageName}/${AdSkipperService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

        if (enabledServices == null || enabledServices.isEmpty()) {
            return false
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }

        return false
    }
}
