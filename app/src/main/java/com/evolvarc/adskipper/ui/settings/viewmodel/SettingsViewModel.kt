package com.evolvarc.adskipper.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evolvarc.adskipper.data.UserDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataStore: UserDataStore
) : ViewModel() {

    val autoMuteAds: StateFlow<Boolean> = userDataStore.autoMuteAds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setAutoMuteAds(isAutoMute: Boolean) {
        viewModelScope.launch {
            userDataStore.setAutoMuteAds(isAutoMute)
        }
    }

    val vibrateOnSkip: StateFlow<Boolean> = userDataStore.vibrateOnSkip
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setVibrateOnSkip(isVibrate: Boolean) {
        viewModelScope.launch {
            userDataStore.setVibrateOnSkip(isVibrate)
        }
    }

    val showNotification: StateFlow<Boolean> = userDataStore.showNotification
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setShowNotification(isShow: Boolean) {
        viewModelScope.launch {
            userDataStore.setShowNotification(isShow)
        }
    }

    val totalAdsSkipped: StateFlow<Int> = userDataStore.totalAdsSkipped
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTimeSaved: StateFlow<Long> = userDataStore.totalTimeSaved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun resetStatistics() {
        viewModelScope.launch {
            userDataStore.resetStatistics()
        }
    }

    val skipDelay: StateFlow<Int> = userDataStore.skipDelay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150)

    fun setSkipDelay(delay: Int) {
        viewModelScope.launch {
            userDataStore.setSkipDelay(delay)
        }
    }
}
