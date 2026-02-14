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
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var audioManager: AudioManager
    private lateinit var vibrator: Vibrator
    private var originalVolume = -1
    private var isMuted = false
    private val NOTIFICATION_ID = 1
    // private val notificationsEnabled = false // Removed hardcoded flag
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

            // Removed automatic startForeground on connection. 
            // We wait for YouTube to be detected.
            // Check DataStore but don't show notification yet.
            serviceScope.launch {
                if (userDataStore.showNotification.first()) {
                     NotificationManager.createNotificationChannel(this@AdSkipperService)
                }
            }

            // Observe Language Selection
            serviceScope.launch {
                userDataStore.selectedLanguage.collect { languageCode ->
                    currentSkipTexts = SkipTextManager.getSkipTexts(languageCode)
                    Log.d(TAG, "Language updated to $languageCode. Loaded ${currentSkipTexts.size} skip words.")
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
            // Logic continues...

            // Throttle events
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastEventTime < 500) {
                return
            }
            lastEventTime = currentTime

            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                // Log.d(TAG, "Root node is null") 
                return
            }
            
            // Log.d(TAG, "Processing YouTube event")
            
            serviceScope.launch {
                try {
                    withTimeoutOrNull(3000) { 
                        findAndClickButton(rootNode)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in findAndClickButton: ${e.message}")
                } finally {
                    try {
                        rootNode.recycle()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onAccessibilityEvent: ${e.message}")
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
    


    private suspend fun findAndClickButton(node: AccessibilityNodeInfo) {

        
        // Check if we just clicked recently
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            return
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
                clickAndHandleAudio(match, "View ID: ${id.substringAfter("/")}")
                nodes.forEach { it.recycle() }
                return
            }
        }

        // If View IDs failed, check if we are in an ad context before efficient manual text scan
        if (!isInAdContext(node)) {
            return 
        }

        // Layer 3: Efficient Single-Pass Traversal for Text/Desc
        // Replaces the expensive loop of 60+ text searches
        try {
            traverseAndFindButton(node)
        } catch (e: Exception) {
            // Log.e(TAG, "Error in traversal", e)
        }
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

    private suspend fun checkNodeForSkipText(node: AccessibilityNodeInfo): Boolean {
        // Must be clickable or have clickable parent to be actionable
        val isActionable = node.isClickable || node.parent?.isClickable == true
        if (!isActionable) return false

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
             clickIfValid(node, "Text Match: $textTrimmed")
             return true
        }
        if (descTrimmed.isNotEmpty() && currentSkipTexts.contains(descTrimmed)) {
             clickIfValid(node, "Desc Match: $descTrimmed")
             return true
        }

        // 2. Contains Match with Word Boundary Check (Iterate Set - still fast in memory)
        // Only if we haven't found exact match.
        // Helpful for "Skip Ad in 5s" type buttons if they become clickable text.
        // IMPORTANT: Use word boundaries to avoid false matches (e.g., "pular" in "popular")
        val textLower = text.lowercase()
        val descLower = desc.lowercase()
        
        for (skipText in currentSkipTexts) {
             val skipLower = skipText.lowercase()
             
             // Word boundary check: Ensure the skip text is a complete word, not a substring
             // This prevents "pular" from matching "popular"
             val textMatches = textLower.isNotEmpty() && 
                 (textLower == skipLower || // Exact match
                  textLower.startsWith("$skipLower ") || // "skip ad" or "skip >"
                  textLower.endsWith(" $skipLower") || // "ad skip"
                  textLower.contains(" $skipLower ")) // "some skip text"
                  
             val descMatches = descLower.isNotEmpty() && 
                 (descLower == skipLower || 
                  descLower.startsWith("$skipLower ") || 
                  descLower.endsWith(" $skipLower") || 
                  descLower.contains(" $skipLower "))
             
             if (textMatches || descMatches) {
                  // Extra check: If it's a partial match, ensure it REALLY looks like a button
                  if (node.className?.contains("Button") == true || 
                      viewId.contains("button") || 
                      viewId.contains("skip")) {
                      clickIfValid(node, "Contains Match: $skipText")
                      return true
                  }
             }
        }
        
        return false
    }
    
    private suspend fun clickIfValid(node: AccessibilityNodeInfo, reason: String) {
        val target = if (node.isClickable) node else node.parent ?: node
        clickAndHandleAudio(target, reason)
    }

    /**
     * Validates that we're actually in an ad context.
     * Optimized to be lightweight.
     */
    private fun isInAdContext(node: AccessibilityNodeInfo): Boolean {
        // Fast Check: Look for known ad UI elements by View ID (System Indexed)
        val adIndicators = listOf(
            "com.google.android.youtube:id/ad_progress_bar",
            "com.google.android.youtube:id/ad_countdown",
            "com.google.android.youtube:id/ad_headline"
        )
        
        for (indicator in adIndicators) {
             val nodes = node.findAccessibilityNodeInfosByViewId(indicator)
             if (nodes.isNotEmpty()) {
                 nodes.forEach { it.recycle() }
                 return true
             }
        }
        
        // Fallback: Check for "Visit Advertiser" or similar buttons which are common
        val visitNodes = node.findAccessibilityNodeInfosByText("Visit advertiser")
        if (visitNodes.isNotEmpty()) {
            visitNodes.forEach { it.recycle() }
            return true
        }

        // Additional check: Look for "Ad" text which is common in ad context
        val adNodes = node.findAccessibilityNodeInfosByText("Ad")
        if (adNodes.isNotEmpty()) {
            adNodes.forEach { it.recycle() }
            return true
        }

        // If no ad indicators found, we are NOT in an ad context.
        // This prevents clicking on non-ad buttons (like "Popular" on channel pages)
        // Layer 1&2 already checked for the explicit skip button IDs.
        // Only proceed with text scanning if we confirmed we're in an ad context.
        
        return false // Only process when we're actually in an ad context
    }
    
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
        serviceScope.launch {
            if (userDataStore.showNotification.first()) {
                val adsSkipped = userDataStore.totalAdsSkipped.first()
                val notification = NotificationManager.getNotificationActive(
                    this@AdSkipperService, 
                    adsSkipped, 
                    "YouTube"
                )
                
                if (shouldStartForeground) {
                    try {
                        startForeground(NOTIFICATION_ID, notification)
                        isForegroundActive = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting foreground: ${e.message}")
                    }
                } else {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
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
    }
}
