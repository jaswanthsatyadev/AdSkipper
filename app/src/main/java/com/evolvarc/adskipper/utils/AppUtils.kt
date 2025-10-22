package com.evolvarc.adskipper.utils

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }
}
