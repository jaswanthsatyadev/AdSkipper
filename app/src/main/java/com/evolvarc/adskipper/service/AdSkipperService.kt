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
    private val notificationsEnabled = false
    private var isForegroundActive = false
    private val serviceControlReceiver = ServiceControlReceiver()
    
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

            if (notificationsEnabled) {
                // Start foreground notification - service only runs when YouTube is active (packageNames config)
                serviceScope.launch {
                    try {
                        if (userDataStore.showNotification.first()) {
                            NotificationManager.createNotificationChannel(this@AdSkipperService)
                            val adsSkipped = userDataStore.totalAdsSkipped.first()
                            val notification = NotificationManager.getNotificationActive(
                                this@AdSkipperService,
                                adsSkipped,
                                "YouTube"
                            )
                            startForeground(NOTIFICATION_ID, notification)
                            isForegroundActive = true
                            Log.d(TAG, "Foreground notification started")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting foreground notification: ${e.message}", e)
                    }
                }
            }
            
            Log.d(TAG, "Accessibility service initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing accessibility service: ${e.message}", e)
        }
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            // Throttle events to reduce processing load (reduced to 500ms for better detection)
            val currentTime = SystemClock.uptimeMillis()
            if (currentTime - lastEventTime < 500) {
                return
            }
            lastEventTime = currentTime

            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.d(TAG, "Root node is null, skipping event")
                return
            }
            
            Log.d(TAG, "Processing YouTube accessibility event")

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
    


    private suspend fun findAndClickButton(node: AccessibilityNodeInfo) {
        val searchStartTime = SystemClock.uptimeMillis()
        
        // Check if we just clicked recently - prevent rapid repeated clicks
        val currentTime = SystemClock.uptimeMillis()
        if (currentTime - lastClickTime < MIN_CLICK_INTERVAL) {
            Log.d(TAG, "⏳ Skipping detection - too soon after last click (${(currentTime - lastClickTime)/1000.0}s ago, minimum ${MIN_CLICK_INTERVAL/1000}s)")
            return
        }
        
        Log.d(TAG, "🔍 Starting ad detection layers...")
        
        // First, verify we're actually in an ad context
        if (!isInAdContext(node)) {
            Log.d(TAG, "❌ Not in ad context - skipping button search")
            return
        }
        
        Log.d(TAG, "✅ Ad context confirmed - searching for skip button...")
        
        // Layer 1: Search by View ID - com.google.android.youtube:id/skip_ad_button
        Log.d(TAG, "🔍 Layer 1: Searching by View ID 'skip_ad_button'...")
        val layer1Nodes = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/skip_ad_button")
        Log.d(TAG, "📊 Layer 1: Found ${layer1Nodes.size} nodes with View ID 'skip_ad_button'")
        if (layer1Nodes.isNotEmpty()) {
            val searchTime = SystemClock.uptimeMillis() - searchStartTime
            Log.d(TAG, "⚡ Layer 1: Found skip button in ${searchTime}ms (View ID: skip_ad_button)")
            clickAndHandleAudio(layer1Nodes[0], "View ID: skip_ad_button")
            layer1Nodes.forEach { it.recycle() }
            return  // Early exit - fastest path
        }

        // Layer 2: Search by View ID - com.google.android.youtube:id/skip_button
        Log.d(TAG, "🔍 Layer 2: Searching by View ID 'skip_button'...")
        val layer2Nodes = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/skip_button")
        Log.d(TAG, "📊 Layer 2: Found ${layer2Nodes.size} nodes with View ID 'skip_button'")
        if (layer2Nodes.isNotEmpty()) {
            val searchTime = SystemClock.uptimeMillis() - searchStartTime
            Log.d(TAG, "⚡ Layer 2: Found skip button in ${searchTime}ms (View ID: skip_button)")
            clickAndHandleAudio(layer2Nodes[0], "View ID: skip_button")
            layer2Nodes.forEach { it.recycle() }
            return  // Early exit
        }

        // Layer 3: Text-based search - Multiple variations for different devices/languages
        Log.d(TAG, "🔍 Layer 3: Searching by text content...")
        val skipTexts = listOf(
            // English
            "Skip Ad", "Skip ad", "SKIP AD",
            "Skip Ads", "Skip ads", "SKIP ADS",
            "Skip", "SKIP",
            // Spanish
            "Saltar anuncio", "Saltar", "SALTAR",
            "Omitir anuncio", "Omitir",
            // Hindi
            "विज्ञापन छोड़ें", "छोड़ें",
            // Dutch
            "Advertentie overslaan", "Overslaan",
            // Polish
            "Pomiń reklamę", "Pomiń",
            // French
            "Ignorer l'annonce", "Ignorer", "Passer",
            "Ignorer la publicité",
            // German
            "Anzeige überspringen", "Überspringen",
            "Werbung überspringen",
            // Russian
            "Пропустить объявление", "Пропустить",
            "Пропустить рекламу",
            // Japanese
            "広告をスキップ", "スキップ",
            // Korean
            "광고 건너뛰기", "건너뛰기",
            // Arabic
            "تخطي الإعلان", "تخطي",
            // Thai
            "ข้ามโฆษณา", "ข้าม",
            // Vietnamese
            "Bỏ qua quảng cáo", "Bỏ qua",
            // Hungarian
            "Hirdetés kihagyása", "Kihagyás",
            // Romanian
            "Omite anunțul", "Omite",
            "Omiteți anunțul",
            // Swedish
            "Hoppa över annons", "Hoppa över",
            // Danish
            "Spring annonce over", "Spring over",
            // Finnish
            "Ohita mainos", "Ohita",
            // Norwegian
            "Hopp over annonse", "Hopp over",
            // Ukrainian
            "Пропустити оголошення", "Пропустити",
            "Пропустити рекламу",
            // Filipino (Tagalog)
            "Laktawan ang ad", "Laktawan",
            "Laktawan ang patalastas",
            // Bengali
            "বিজ্ঞাপন এড়িয়ে যান", "এড়িয়ে যান",
            // Urdu
            "اشتہار چھوڑیں", "چھوڑیں",
            // Portuguese
            "Pular anúncio", "Pular",
            // Italian
            "Salta annuncio", "Salta", "Ignora"
        )
        for (text in skipTexts) {
            val layer3Nodes = node.findAccessibilityNodeInfosByText(text)
            if (layer3Nodes.isNotEmpty()) {
                Log.d(TAG, "📊 Layer 3: Found ${layer3Nodes.size} nodes with text '$text'")
                for (textNode in layer3Nodes) {
                    // Must be clickable or have clickable parent
                    if (textNode.isClickable || textNode.parent?.isClickable == true) {
                        val clickTarget = if (textNode.isClickable) textNode else textNode.parent
                        clickTarget?.let {
                            // Validate it's actually a button and in reasonable position
                            val className = it.className?.toString() ?: ""
                            Log.d(TAG, "🔍 Layer 3: Checking node with className: $className")
                            if (className.contains("Button", ignoreCase = true) || 
                                className.contains("View", ignoreCase = true)) {
                                Log.d(TAG, "✅ Layer 3: Found valid skip button by text '$text' (className: $className)")
                                clickAndHandleAudio(it, "Text: $text")
                                layer3Nodes.forEach { n -> n.recycle() }
                                return
                            }
                        }
                    }
                }
                layer3Nodes.forEach { it.recycle() }
            }
        }
        
        Log.d(TAG, "❌ Layer 3: No skip button found by text")
        
        // Layer 4: Content Description search (for accessibility-enabled devices)
        Log.d(TAG, "🔍 Layer 4: Searching by content description...")
        val contentDescriptions = listOf(
            "Skip ad", "Skip Ad", "Skip",
            "Skip ads", "Skip Ads",
            "Advertisement skip", "Ad skip"
        )
        for (desc in contentDescriptions) {
            val descNode = findNodeByContentDescription(node, desc)
            if (descNode != null) {
                Log.d(TAG, "✅ Layer 4: Found skip button by content description '$desc'")
                clickAndHandleAudio(descNode, "Content Desc: $desc")
                descNode.recycle()
                return
            }
        }
        
        Log.d(TAG, "❌ Layer 4: No skip button found by content description")
        Log.d(TAG, "❌ All detection layers exhausted - no skip button found")
        
        // Layer 5: DISABLED - Fuzzy search is too aggressive
        // It can click on wrong buttons like "Skip intro", "Skip to next video", etc.
        // Only Layers 1-4 (View ID, Text matching, Content Description) are safe
        
        Log.d(TAG, "No skip button found in safe detection layers (1-4)")

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
                Log.d(TAG, "✅ Ad context confirmed - found indicator: $indicator")
                return true
            }
        }
        
        // FALLBACK: Look for "skip" text with ad-related context
        // This is more lenient than before but still safer than blocking all detection
        val skipNodes = node.findAccessibilityNodeInfosByText("skip")
        if (skipNodes.isNotEmpty()) {
            for (skipNode in skipNodes) {
                val text = skipNode.text?.toString()?.lowercase() ?: ""
                val contentDesc = skipNode.contentDescription?.toString()?.lowercase() ?: ""
                
                // Check if it's likely an ad skip button (contains "ad" or is a button)
                if (text.contains("ad") || contentDesc.contains("ad") || 
                    skipNode.className?.contains("Button") == true) {
                    skipNodes.forEach { it.recycle() }
                    Log.d(TAG, "⚠️ Ad context inferred from skip button with ad-related text")
                    return true
                }
            }
            skipNodes.forEach { it.recycle() }
        }
        
        // CRITICAL FIX: If we're in YouTube and layers 1-4 exist, allow them to try
        // This prevents the overly restrictive check from blocking legitimate ad detection
        Log.w(TAG, "⚡ No explicit ad context found, but allowing detection layers to attempt")
        return true  // Changed from false - this was blocking all detection!
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

        private fun checkForAdIndicators(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        
        // Look for ad-related text or content that indicates an ad is playing
        val adIndicators = listOf(
            "ad", "advertisement", "sponsored",
            "skip ad", "skip ads", "video will play",
            "you can skip", "advertisement will end"
        )
        
        fun searchNode(n: AccessibilityNodeInfo?): Boolean {
            if (n == null) return false
            
            val text = n.text?.toString()?.lowercase() ?: ""
            val contentDesc = n.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = n.viewIdResourceName?.lowercase() ?: ""
            
            // Check if any ad indicator is present
            for (indicator in adIndicators) {
                if (text.contains(indicator) || contentDesc.contains(indicator) || viewId.contains("ad")) {
                    return true
                }
            }
            
            // Recursively check children
            for (i in 0 until n.childCount) {
                val child = n.getChild(i)
                if (child != null) {
                    if (searchNode(child)) {
                        child.recycle()
                        return true
                    }
                    child.recycle()
                }
            }
            
            return false
        }
        
        return searchNode(node)
    }
    
    private fun findNodeByContentDescription(node: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val contentDesc = node.contentDescription?.toString()?.lowercase()
        if (contentDesc != null && contentDesc.contains(description.lowercase()) && node.isClickable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByContentDescription(child, description)
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
                
                // Show "Skipped ad for you ;)" toast message
                serviceScope.launch(Dispatchers.Main) {
                    try {
                        Toast.makeText(
                            this@AdSkipperService,
                            "Skipped ad for you ;)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing toast: ${e.message}")
                    }
                }
                
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

                updateNotification()
                
                // Broadcast ad skip event to show toast
                val intent = android.content.Intent("com.evolvarc.adskipper.AD_SKIPPED")
                sendBroadcast(intent)

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
        
        // Stop foreground service if it was started
        if (notificationsEnabled && isForegroundActive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            isForegroundActive = false
        }
    }

    private fun updateNotificationForApp(appName: String) {
        if (!notificationsEnabled) return
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
        if (!notificationsEnabled) return
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
        if (!notificationsEnabled) return
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
