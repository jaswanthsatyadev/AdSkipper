package com.evolvarc.adskipper.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userDataStore: com.evolvarc.adskipper.data.UserDataStore
) : ViewModel() {

    private val _onboardingStep: MutableStateFlow<OnboardingStep> = MutableStateFlow(OnboardingStep.Welcome)
    val onboardingStep: StateFlow<OnboardingStep> = _onboardingStep.asStateFlow()

    fun saveLanguage(languageCode: String) {
        // You might want to save this to DataStore right away
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            userDataStore.setSelectedLanguage(languageCode)
        }
        nextStep()
    }

    fun nextStep() {
        when (_onboardingStep.value) {
            OnboardingStep.Welcome -> _onboardingStep.value = OnboardingStep.Language
            OnboardingStep.Language -> _onboardingStep.value = OnboardingStep.NotificationPermission
            OnboardingStep.NotificationPermission -> _onboardingStep.value = OnboardingStep.BatteryOptimization
            OnboardingStep.BatteryOptimization -> _onboardingStep.value = OnboardingStep.AccessibilityPermission
            OnboardingStep.AccessibilityPermission -> _onboardingStep.value = OnboardingStep.Claim
            OnboardingStep.Claim -> { /* Onboarding finished */ }
        }
    }
}

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object Language : OnboardingStep()
    object NotificationPermission : OnboardingStep()
    object BatteryOptimization : OnboardingStep()
    object AccessibilityPermission : OnboardingStep()
    object Claim : OnboardingStep()
}
