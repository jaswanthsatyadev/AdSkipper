package com.evolvarc.adskipper.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.IntentFilter
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.evolvarc.adskipper.data.UserDataStore
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
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private val searchChannel = kotlinx.coroutines.channels.Channel<Unit>(kotlinx.coroutines.channels.Channel.CONFLATED)

    private lateinit var audioManager: AudioManager
    private lateinit var vibrator: Vibrator
    private var originalVolume = -1
    private var isMuted = false
    private val NOTIFICATION_ID = 1
    private var isForegroundActive = false
    private val serviceControlReceiver = ServiceControlReceiver()
    private var currentSkipTexts: Set<String> = emptySet()
    
    // Prevent repeated clicking - minimum 5 seconds between clicks
    private val MIN_CLICK_INTERVAL = 5000L

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

            // Notifications are intentionally disabled in production for now.
            clearLegacyNotification()

            // Observe Language Selection
            serviceScope.launch {
                userDataStore.selectedLanguage.collect { languageCode ->
                    currentSkipTexts = SkipTextManager.getSkipTexts(languageCode)
                    Log.d(TAG, "Language updated to $languageCode. Loaded ${currentSkipTexts.size} skip words.")
                }
            }
            
            // Background search processor for debounced events
            serviceScope.launch {
                for (event in searchChannel) {
                    performSearch()
                    delay(150) // Small cooldown to prevent CPU hogging and allow UI updates
                }
            }
            
            Log.d(TAG, "Accessibility service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing accessibility service: ${e.message}", e)
        }
    }



    // ... (imports and class def)
    private var isYouTubeActive = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val packageName = event?.packageName?.toString() ?: return
            
            // Ignore SystemUI events (notification shade, volume controls, etc.)
            // so we don't think we left YouTube just because the user adjusted volume.
            if (packageName == "com.android.systemui" || packageName == "android") {
                return
            }

            val isYouTube = packageName == "com.google.android.youtube"

            // Smart Notification & Service Lifecycle Logic:
            if (isYouTube) {
                if (!isYouTubeActive) {
                    isYouTubeActive = true
                    Log.d(TAG, "YouTube detected ($packageName) - Activating service UI")
                    updateNotification(true) // Start Foreground
                }
                // Refresh/Keep-alive logical timestamp if needed
            } else {
                // We are in another app (and it's not SystemUI).
                // Only disable if we were previously active AND it's a significant window change.
                if (isYouTubeActive) {
                    // Only WINDOW_STATE_CHANGED consistently signals an Activity switch.
                    // Content changes from keyboards (Gboard) or other overlays shouldn't kill the service.
                    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                        isYouTubeActive = false
                        Log.d(TAG, "Exited YouTube (Active: $packageName) - Hiding service UI")
                        stopForegroundSafely()
                    }
                }
            }

            // ONLY process ad logic if we are strictly in YouTube
            if (!isYouTube) {
                return 
            }
            
            // Queue a search request. If multiple events fire rapidly, 
            // the CONFLATED channel will just drop intermediate requests 
            // and process the latest state, preventing dropped/missed events.
            searchChannel.trySend(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error in onAccessibilityEvent: ${e.message}")
        }
    }

    private suspend fun performSearch() {
        val allWindows = try { windows } catch (e: Exception) { null }
        if (allWindows.isNullOrEmpty()) {
            val rootNode = try { rootInActiveWindow } catch (e: Exception) { null } ?: return
            try {
                withTimeoutOrNull(3000) { 
                    findAndClickButton(rootNode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in performSearch: ${e.message}")
            } finally {
                try { rootNode.recycle() } catch (e: Exception) {}
            }
            return
        }
        
        try {
            withTimeoutOrNull(3000) { 
                var found = false
                for (window in allWindows) {
                    val rootNode = window.root
                    if (rootNode != null) {
                        found = findAndClickButton(rootNode)
                        try { rootNode.recycle() } catch (e: Exception) {}
                        if (found) break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in performSearch loop: ${e.message}")
        }
    }

    private fun stopForegroundSafely() {
        if (isForegroundActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            isForegroundActive = false
        }
    }

    // ... (rest of class)
    


    private suspend fun findAndClickButton(node: AccessibilityNodeInfo): Boolean {

        // Check if we just clicked recently
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            return false
        }

        // Layer 1 & 2: Quick Search by View ID (Fastest & Safest)
        // These are indexed by the system and don't require manual traversal
        val viewIds = listOf(
            "com.google.android.youtube:id/skip_ad_button", 
            "com.google.android.youtube:id/skip_button"
        )
        
        for (id in viewIds) {
            val nodes = node.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) {
                val match = nodes[0]
                val clickableTarget = getClickableParent(match) ?: match
                if (clickableTarget.isEnabled) {
                    clickAndHandleAudio(clickableTarget, "View ID: ${id.substringAfter("/")}")
                    nodes.forEach { it.recycle() }
                    return true
                }
                nodes.forEach { it.recycle() }
            }
        }

        // Layer 3: Efficient Single-Pass Traversal for Text/Desc
        // Replaces the expensive loop of 60+ text searches
        try {
            return traverseAndFindButton(node)
        } catch (e: Exception) {
            // Log.e(TAG, "Error in traversal", e)
        }
        return false
    }

    /**
     * Efficiently traverses the node hierarchy once to find match.
     * Returns true if button found and clicked.
     */
    private suspend fun traverseAndFindButton(node: AccessibilityNodeInfo): Boolean {
        // Check current node
        if (checkNodeForSkipText(node)) {
            return true
        }
        
        // Recursively check children
        // Limit depth to avoid stack overflow on crazy hierarchies?
        // AccessibilityNodeInfo trees aren't usually deeper than 50-100.
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            if (traverseAndFindButton(child)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }

    private fun getClickableParent(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current = node
        while (current != null) {
            if (current.isClickable) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private suspend fun checkNodeForSkipText(node: AccessibilityNodeInfo): Boolean {
        // Must be clickable AND enabled to be actionable. 
        // Prevents clicking "Skip Ad in 5s" text when it's just a label or disabled button.
        val clickableTarget = getClickableParent(node)
        if (clickableTarget == null || !clickableTarget.isEnabled) return false

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        
        // Optimize: Check if text is empty
        if (text.isEmpty() && desc.isEmpty()) return false

        // Safety Filter 1: Ignore long text (Skip buttons are short)
        if (text.length > 25 || desc.length > 25) return false

        // Safety Filter 2: Check View ID for non-button indicators
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.isNotEmpty()) {
            if (viewId.contains("title") || viewId.contains("description") || 
                viewId.contains("subtitle") || viewId.contains("metadata")) {
                return false
            }
        }

        val textTrimmed = text.trim()
        val descTrimmed = desc.trim()

        // 1. Exact Match (Fastest) in Set
        if (textTrimmed.isNotEmpty() && currentSkipTexts.contains(textTrimmed)) {
             clickAndHandleAudio(clickableTarget, "Text Match: $textTrimmed")
             return true
        }
        if (descTrimmed.isNotEmpty() && currentSkipTexts.contains(descTrimmed)) {
             clickAndHandleAudio(clickableTarget, "Desc Match: $descTrimmed")
             return true
        }

        // 2. Contains Match with Word Boundary Check (Iterate Set - still fast in memory)
        val textLower = text.lowercase()
        val descLower = desc.lowercase()
        
        for (skipText in currentSkipTexts) {
             val skipLower = skipText.lowercase()
             
             // Word boundary check: Ensure the skip text is a complete word
             val textMatches = textLower.isNotEmpty() && 
                 (textLower == skipLower || 
                  textLower.startsWith("$skipLower ") || 
                  textLower.endsWith(" $skipLower") || 
                  textLower.contains(" $skipLower "))
                  
             val descMatches = descLower.isNotEmpty() && 
                 (descLower == skipLower || 
                  descLower.startsWith("$skipLower ") || 
                  descLower.endsWith(" $skipLower") || 
                  descLower.contains(" $skipLower "))
             
             if (textMatches || descMatches) {
                  // Extra check: If it's a partial match, ensure it REALLY looks like a button
                  if (clickableTarget.className?.contains("Button") == true || 
                      viewId.contains("button") || 
                      viewId.contains("skip")) {
                      clickAndHandleAudio(clickableTarget, "Contains Match: $skipText")
                      return true
                  }
             }
        }
        
        return false
    }

    // Removed isInAdContext as it was causing issues with localized ad text (failing to skip ads in other languages).
    
    // Dead code removed: searchForIndicator, checkForAdIndicators, findNodeByContentDescription, findButtonInTopRightQuadrant, findButtonInRegion

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

            Log.d(TAG, "⏳ Waiting ${skipDelay}ms before clicking button: $reason")
            delay(skipDelay) // Delay before clicking
            
            Log.d(TAG, "🖱️ Performing click action on button...")
            val clicked = button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "📊 Click action result: ${if (clicked) "SUCCESS ✅" else "FAILED ❌"}")
            
            if (clicked) {
                Log.d(TAG, "✅ Successfully clicked skip button via $reason")
                
                // Update last click timestamp to prevent rapid repeated clicks
                lastClickTime = SystemClock.uptimeMillis()
                Log.d(TAG, "⏱️ Last click timestamp updated: $lastClickTime")
                
                // Update statistics (ensure completion with proper coroutine scope)
                serviceScope.launch {
                    try {
                        userDataStore.incrementTotalAdsSkipped()
                        userDataStore.addTimeSaved(5) // Assume 5 seconds saved per ad
                        Log.d(TAG, "✅ Stats updated: counter incremented, 5s saved")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating stats: ${e.message}", e)
                    }
                }

                updateNotification(true)

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

    // ... (previous code)

    private fun updateNotification(shouldStartForeground: Boolean) {
        // Notifications are fully disabled by product decision.
        // Keep this method as a no-op so existing call sites remain stable.
        return
    }

    private fun clearLegacyNotification() {
        try {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing legacy notification: ${e.message}")
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
        
        // Stop foreground service if it was started
        if (isForegroundActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            isForegroundActive = false
        }

        clearLegacyNotification()
    }
}
