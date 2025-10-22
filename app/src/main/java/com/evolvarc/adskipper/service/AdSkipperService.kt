package com.evolvarc.adskipper.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.IntentFilter
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.evolvarc.adskipper.data.UserDataStore
import com.evolvarc.adskipper.notification.NotificationManager
import com.evolvarc.adskipper.receivers.ServiceControlReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class AdSkipperService : AccessibilityService() {

    @Inject
    lateinit var userDataStore: UserDataStore

    private val TAG = "AdSkipper_Service"
    private var lastEventTime = 0L
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var audioManager: AudioManager
    private lateinit var vibrator: Vibrator
    private var originalVolume = -1
    private var isMuted = false
    private val NOTIFICATION_ID = 1
    private val serviceControlReceiver = ServiceControlReceiver()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val filter = IntentFilter(ServiceControlReceiver.ACTION_PAUSE_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceControlReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceControlReceiver, filter)
        }

        serviceControlReceiver.onPauseService = {
            Log.d(TAG, "Pause service action received. Disabling service.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                disableSelf()
            }
        }

        serviceScope.launch {
            if (userDataStore.showNotification.first()) {
                NotificationManager.createNotificationChannel(this@AdSkipperService)
                val adsSkipped = userDataStore.totalAdsSkipped.first()
                val notification = NotificationManager.getNotification(this@AdSkipperService, adsSkipped)
                startForeground(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != "com.google.android.youtube") {
            return
        }

        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastEventTime < 500) { // Throttle events
            return
        }
        lastEventTime = currentTime

        val rootNode = rootInActiveWindow ?: return
        Log.d(TAG, "Accessibility event received from YouTube")

        serviceScope.launch {
            withTimeoutOrNull(3000) { // 3-second timeout for the search
                findAndClickButton(rootNode)
            }
            rootNode.recycle()
        }
    }

    private suspend fun findAndClickButton(node: AccessibilityNodeInfo) {
        // ... (Layers 1-5)
    }

    private suspend fun clickAndHandleAudio(button: AccessibilityNodeInfo, reason: String) {
        val isAutoMuteEnabled = userDataStore.autoMuteAds.first()
        val isVibrateOnSkipEnabled = userDataStore.vibrateOnSkip.first()
        val skipDelay = userDataStore.skipDelay.first().toLong()

        if(isAutoMuteEnabled) {
            Log.d(TAG, "Ad detected via $reason. Muting audio.")
            muteAudio()
        }

        if (isVibrateOnSkipEnabled) {
            vibrate()
        }

        Log.d(TAG, "Attempting to click button: $reason")
        delay(skipDelay) // Delay before clicking
        button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        
        // Update statistics
        userDataStore.incrementTotalAdsSkipped()
        userDataStore.addTimeSaved(5) // Assume 5 seconds saved per ad

        updateNotification()

        serviceScope.launch {
            delay(2000) // Wait 2 seconds before unmuting
            unmuteAudio()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private fun muteAudio() {
        if (!isMuted) {
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            isMuted = true
        }
    }

    private fun unmuteAudio() {
        if (isMuted && originalVolume != -1) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
            isMuted = false
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private fun updateNotification() {
        serviceScope.launch {
            if (userDataStore.showNotification.first()) {
                val adsSkipped = userDataStore.totalAdsSkipped.first()
                val notification = NotificationManager.getNotification(this@AdSkipperService, adsSkipped)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }
    
    // ... (rest of the service)
}
