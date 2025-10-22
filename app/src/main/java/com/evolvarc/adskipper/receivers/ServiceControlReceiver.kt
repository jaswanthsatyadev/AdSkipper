package com.evolvarc.adskipper.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ServiceControlReceiver : BroadcastReceiver() {

    var onPauseService: (() -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PAUSE_SERVICE) {
            onPauseService?.invoke()
        }
    }

    companion object {
        const val ACTION_PAUSE_SERVICE = "com.evolvarc.adskipper.ACTION_PAUSE_SERVICE"
    }
}
