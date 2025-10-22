package com.evolvarc.adskipper.ui.home.viewmodel

import android.app.Application
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

    val autoMuteAds: StateFlow<Boolean> = userDataStore.autoMuteAds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isFirstHomeScreenVisit: StateFlow<Boolean> = userDataStore.isFirstHomeScreenVisit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        checkServiceStatus()
    }

    fun checkServiceStatus() {
        _isServiceEnabled.value = AccessibilityServiceUtils.isAccessibilityServiceEnabled(application)
    }

    fun setAutoMuteAds(isAutoMute: Boolean) {
        viewModelScope.launch {
            userDataStore.setAutoMuteAds(isAutoMute)
        }
    }

    fun setFirstHomeScreenVisit(isFirst: Boolean) {
        viewModelScope.launch {
            userDataStore.setFirstHomeScreenVisit(isFirst)
        }
    }
}
