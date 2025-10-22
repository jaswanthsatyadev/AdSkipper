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
    private var lastClickTime = 0L
    private var currentActiveApp: String? = null  // Track which app is currently active
    private var isAppActive = false  // Track if service should be active
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var audioManager: AudioManager
    private lateinit var vibrator: Vibrator
    private var originalVolume = -1
    private var isMuted = false
    private val NOTIFICATION_ID = 1
    private val serviceControlReceiver = ServiceControlReceiver()
    
    // Prevent repeated clicking - minimum 5 seconds between clicks
    private val MIN_CLICK_INTERVAL = 5000L
    
    // List of apps that support ad detection
    // Currently YouTube, but can easily add more apps (Facebook, Instagram, TikTok, etc.)
    companion object {
        private val MONITORED_APPS = setOf(
            "com.google.android.youtube",  // YouTube
            // Add more apps here in future:
            // "com.facebook.katana",  // Facebook
            // "com.instagram.android",  // Instagram
            // "com.zhiliaoapp.musically",  // TikTok
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        
        try {
            // Initialize audio manager
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Initialize vibrator
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Register broadcast receiver
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

            // Start foreground service with notification
            serviceScope.launch {
                try {
                    if (userDataStore.showNotification.first()) {
                        NotificationManager.createNotificationChannel(this@AdSkipperService)
                        val adsSkipped = userDataStore.totalAdsSkipped.first()
                        val notification = NotificationManager.getNotification(this@AdSkipperService, adsSkipped)
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting foreground service: ${e.message}", e)
                }
            }
            
            Log.d(TAG, "Accessibility service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing accessibility service: ${e.message}", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName = event?.packageName?.toString() ?: return
            
            // Check if the event is from a monitored app
            if (packageName !in MONITORED_APPS) {
                // App is not monitored - check if we need to deactivate
                if (isAppActive) {
                    handleAppDeactivated(packageName)
                }
                return
            }
            
            // We're in a monitored app
            if (currentActiveApp != packageName) {
                handleAppActivated(packageName)
            }

            // Only process accessibility events if the app should be active
            if (!isAppActive) {
                return
            }

            val currentTime = SystemClock.uptimeMillis()
            // Increase throttle to 2 seconds to reduce processing load
            if (currentTime - lastEventTime < 2000) {
                return
            }
            lastEventTime = currentTime

            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.d(TAG, "Root node is null, skipping event")
                return
            }
            
            Log.d(TAG, "Processing $packageName accessibility event")

            serviceScope.launch {
                try {
                    withTimeoutOrNull(3000) { // 3-second timeout for the search
                        findAndClickButton(rootNode)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in findAndClickButton: ${e.message}", e)
                } finally {
                    try {
                        rootNode.recycle()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error recycling root node: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onAccessibilityEvent: ${e.message}", e)
        }
    }
    
    /**
     * Called when a monitored app becomes active (YouTube, etc.)
     */
    private fun handleAppActivated(packageName: String) {
        isAppActive = true
        currentActiveApp = packageName
        Log.d(TAG, "App activated: $packageName - Service is now ACTIVE")
        
        // Restore original mute state and start fresh
        if (isMuted) {
            unmuteAudio()
        }
        
        // Update notification to show we're watching this app
        serviceScope.launch {
            val appName = when (packageName) {
                "com.google.android.youtube" -> "YouTube"
                else -> packageName.substringAfterLast('.')
            }
            Log.d(TAG, "Now monitoring: $appName for ads")
            updateNotificationForApp(appName)
        }
    }
    
    /**
     * Called when a monitored app becomes inactive (user switches away from YouTube)
     */
    private fun handleAppDeactivated(packageName: String) {
        isAppActive = false
        currentActiveApp = null
        Log.d(TAG, "App deactivated: $packageName - Service is now IDLE")
        
        // Restore audio if muted
        if (isMuted) {
            unmuteAudio()
        }
        
        // Hide notification when app is not active
        serviceScope.launch {
            Log.d(TAG, "Stopped monitoring - hiding notification")
            try {
                // Stop showing foreground notification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding notification: ${e.message}")
            }
        }
    }

    private suspend fun findAndClickButton(node: AccessibilityNodeInfo) {
        // Check if we just clicked recently - prevent rapid repeated clicks
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            Log.d(TAG, "Skipping click - too soon after last click (${(currentTime - lastClickTime)/1000}s ago)")
            return
        }
        
        // First, verify we're actually in an ad context
        if (!isInAdContext(node)) {
            Log.d(TAG, "Not in ad context - skipping button search")
            return
        }
        
        // Layer 1: Search by View ID - com.google.android.youtube:id/skip_ad_button
        val layer1Nodes = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/skip_ad_button")
        if (layer1Nodes.isNotEmpty()) {
            Log.d(TAG, "Layer 1: Found skip button by ID 'skip_ad_button'")
            clickAndHandleAudio(layer1Nodes[0], "View ID: skip_ad_button")
            layer1Nodes.forEach { it.recycle() }
            return
        }

        // Layer 2: Search by View ID - com.google.android.youtube:id/skip_button
        val layer2Nodes = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/skip_button")
        if (layer2Nodes.isNotEmpty()) {
            Log.d(TAG, "Layer 2: Found skip button by ID 'skip_button'")
            clickAndHandleAudio(layer2Nodes[0], "View ID: skip_button")
            layer2Nodes.forEach { it.recycle() }
            return
        }

        // Layer 3: Text-based search - ONLY for "Skip Ad" (more specific)
        // Avoid generic "Skip" to prevent clicking wrong buttons
        val skipTexts = listOf("Skip Ad", "Skip ad", "SKIP AD")
        for (text in skipTexts) {
            val layer3Nodes = node.findAccessibilityNodeInfosByText(text)
            if (layer3Nodes.isNotEmpty()) {
                for (textNode in layer3Nodes) {
                    // Must be clickable or have clickable parent
                    if (textNode.isClickable || textNode.parent?.isClickable == true) {
                        // Additional validation: must be a Button type
                        val className = (if (textNode.isClickable) textNode else textNode.parent)?.className?.toString() ?: ""
                        if (className.contains("Button", ignoreCase = true)) {
                            Log.d(TAG, "Layer 3: Found skip button by text '$text'")
                            val clickTarget = if (textNode.isClickable) textNode else textNode.parent
                            clickTarget?.let {
                                clickAndHandleAudio(it, "Text: $text")
                                layer3Nodes.forEach { it.recycle() }
                                return
                            }
                        }
                    }
                }
                layer3Nodes.forEach { it.recycle() }
            }
        }

        // Layer 4 & 5: DISABLED to prevent false positives
        // These layers are too aggressive and click random UI elements
        // Only Layers 1-3 are safe enough for production use
        
        Log.d(TAG, "No skip button found in safe detection layers (1-3)")

        Log.d(TAG, "No skip button found in any layer")
    }

    /**
     * Validates that we're actually in an ad context before searching for skip buttons.
     * This prevents clicking random buttons during normal YouTube usage.
     */
    private fun isInAdContext(node: AccessibilityNodeInfo): Boolean {
        // Check for ad indicators in the UI hierarchy
        val adIndicators = listOf(
            "com.google.android.youtube:id/ad_",
            "com.google.android.youtube:id/skip_ad",
            "com.google.android.youtube:id/skip_button",
            "com.google.android.youtube:id/video_ads",
            "ad overlay",
            "advertisement"
        )
        
        // Search for any ad-related view IDs or text
        for (indicator in adIndicators) {
            if (searchForIndicator(node, indicator)) {
                Log.d(TAG, "Ad context confirmed - found indicator: $indicator")
                return true
            }
        }
        
        // Also check for "Skip" text which strongly suggests an ad
        val skipTexts = node.findAccessibilityNodeInfosByText("Skip")
        if (skipTexts.isNotEmpty()) {
            skipTexts.forEach { it.recycle() }
            Log.d(TAG, "Ad context confirmed - found 'Skip' text")
            return true
        }
        
        Log.d(TAG, "No ad context detected")
        return false
    }
    
    /**
     * Recursively search for ad indicator in node hierarchy
     */
    private fun searchForIndicator(node: AccessibilityNodeInfo?, indicator: String): Boolean {
        if (node == null) return false
        
        // Check viewIdResourceName
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains(indicator.lowercase())) {
            return true
        }
        
        // Check text
        val text = node.text?.toString()?.lowercase() ?: ""
        if (text.contains(indicator.lowercase())) {
            return true
        }
        
        // Check content description
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (contentDesc.contains(indicator.lowercase())) {
            return true
        }
        
        // Search children (limit depth to prevent performance issues)
        for (i in 0 until minOf(node.childCount, 20)) {
            val child = node.getChild(i) ?: continue
            if (searchForIndicator(child, indicator)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        
        return false
    }

    private fun findNodeByContentDescription(node: AccessibilityNodeInfo?, searchText: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val contentDesc = node.contentDescription?.toString()?.lowercase()
        if (contentDesc != null && contentDesc.contains(searchText.lowercase()) && node.isClickable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByContentDescription(child, searchText)
            if (result != null) {
                child.recycle()
                return result
            }
            child.recycle()
        }

        return null
    }

    private fun findButtonInTopRightQuadrant(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Define top-right quadrant (right half, top 40% of screen)
        val topRightMinX = screenWidth / 2
        val topRightMaxY = (screenHeight * 0.4).toInt()

        return findButtonInRegion(node, topRightMinX, 0, screenWidth, topRightMaxY)
    }

    private fun findButtonInRegion(
        node: AccessibilityNodeInfo?,
        minX: Int,
        minY: Int,
        maxX: Int,
        maxY: Int
    ): AccessibilityNodeInfo? {
        if (node == null) return null

        // Check if current node is a clickable button in the region
        if (node.isClickable && node.className?.contains("Button") == true) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            
            if (rect.centerX() in minX..maxX && rect.centerY() in minY..maxY) {
                // Additional validation: skip button text hints
                val text = node.text?.toString()?.lowercase()
                val contentDesc = node.contentDescription?.toString()?.lowercase()
                
                if (text?.contains("skip") == true || contentDesc?.contains("skip") == true) {
                    return node
                }
                
                // If in exact top-right corner and is a button, likely the skip button
                if (rect.centerX() > (maxX * 0.8) && rect.centerY() < (maxY * 0.5)) {
                    return node
                }
            }
        }

        // Recursively search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findButtonInRegion(child, minX, minY, maxX, maxY)
            if (result != null) {
                child.recycle()
                return result
            }
            child.recycle()
        }

        return null
    }

    private suspend fun clickAndHandleAudio(button: AccessibilityNodeInfo, reason: String) {
        try {
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
            
            val clicked = button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clicked) {
                Log.d(TAG, "Successfully clicked skip button via $reason")
                
                // Update last click timestamp to prevent rapid repeated clicks
                lastClickTime = SystemClock.uptimeMillis()
                
                // Update statistics
                userDataStore.incrementTotalAdsSkipped()
                userDataStore.addTimeSaved(5) // Assume 5 seconds saved per ad

                updateNotification()

                // Unmute after a delay if auto-mute was enabled
                if (isAutoMuteEnabled) {
                    serviceScope.launch {
                        delay(2000) // Wait 2 seconds before unmuting
                        unmuteAudio()
                    }
                }
            } else {
                Log.w(TAG, "Failed to click button via $reason")
                unmuteAudio() // Unmute immediately if click failed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in clickAndHandleAudio: ${e.message}", e)
            unmuteAudio() // Ensure audio is restored on error
        }
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating: ${e.message}")
        }
    }

    private fun muteAudio() {
        try {
            if (!isMuted) {
                originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                isMuted = true
                Log.d(TAG, "Audio muted (original volume: $originalVolume)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error muting audio: ${e.message}", e)
        }
    }

    private fun unmuteAudio() {
        try {
            if (isMuted && originalVolume != -1) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                isMuted = false
                Log.d(TAG, "Audio unmuted (restored volume: $originalVolume)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unmuting audio: ${e.message}", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed - cleaning up")
        
        // Restore audio if muted
        unmuteAudio()
        
        // Cancel all coroutines
        serviceJob.cancel()
        
        // Unregister broadcast receiver
        try {
            unregisterReceiver(serviceControlReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
        
        // Stop foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun updateNotificationForApp(appName: String) {
        serviceScope.launch {
            if (userDataStore.showNotification.first()) {
                val adsSkipped = userDataStore.totalAdsSkipped.first()
                val notification = NotificationManager.getNotificationActive(
                    this@AdSkipperService,
                    adsSkipped,
                    appName
                )
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }
    
    private fun updateNotificationForIdleState() {
        serviceScope.launch {
            if (userDataStore.showNotification.first()) {
                val adsSkipped = userDataStore.totalAdsSkipped.first()
                val notification = NotificationManager.getNotificationIdle(
                    this@AdSkipperService,
                    adsSkipped
                )
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
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
