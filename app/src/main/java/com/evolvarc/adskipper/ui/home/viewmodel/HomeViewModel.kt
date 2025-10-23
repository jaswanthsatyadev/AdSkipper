package com.evolvarc.adskipper.ui.home.viewmodel

import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evolvarc.adskipper.data.UserDataStore
import com.evolvarc.adskipper.utils.AccessibilityServiceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDataStore: UserDataStore,
    private val application: Application
) : ViewModel() {

    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()
    
    private val _isYouTubeActive = MutableStateFlow(false)
    val isYouTubeActive: StateFlow<Boolean> = _isYouTubeActive.asStateFlow()

    val totalAdsSkipped: StateFlow<Int> = userDataStore.totalAdsSkipped
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isFirstHomeScreenVisit: StateFlow<Boolean> = userDataStore.isFirstHomeScreenVisit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        checkServiceStatus()
    }

    fun checkServiceStatus() {
        // Check if accessibility service is enabled
        val isAccessibilityEnabled = AccessibilityServiceUtils.isAccessibilityServiceEnabled(application)
        _isServiceEnabled.value = isAccessibilityEnabled
        
        // Also check if YouTube is currently running (for display purposes)
        val isYouTubeRunning = if (isAccessibilityEnabled) isYouTubeForegroundApp() else false
        _isYouTubeActive.value = isYouTubeRunning
    }

    private fun isYouTubeForegroundApp(): Boolean {
        return try {
            val usageStatsManager = application.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()
            val stats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    currentTime - 2000,
                    currentTime
                )
            } else {
                @Suppress("DEPRECATION")
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    currentTime - 2000,
                    currentTime
                )
            }

            val youtubeStats = stats.find { it.packageName == "com.google.android.youtube" }
            youtubeStats != null && (currentTime - youtubeStats.lastTimeUsed) < 2000
        } catch (e: Exception) {
            false
        }
    }

    fun setFirstHomeScreenVisit(isFirst: Boolean) {
        viewModelScope.launch {
            userDataStore.setFirstHomeScreenVisit(isFirst)
        }
    }
}
