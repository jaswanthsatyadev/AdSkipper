package com.evolvarc.adskipper.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdSkippedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.evolvarc.adskipper.AD_SKIPPED" && context != null) {
            Toast.makeText(
                context,
                "Ad Skipper, skipped an ad for you ;)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
