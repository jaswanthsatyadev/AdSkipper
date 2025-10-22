package com.evolvarc.adskipper.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _onboardingStep: MutableStateFlow<OnboardingStep> = MutableStateFlow(OnboardingStep.Welcome)
    val onboardingStep: StateFlow<OnboardingStep> = _onboardingStep.asStateFlow()

    fun nextStep() {
        when (_onboardingStep.value) {
            OnboardingStep.Welcome -> _onboardingStep.value = OnboardingStep.NotificationPermission
            OnboardingStep.NotificationPermission -> _onboardingStep.value = OnboardingStep.AccessibilityPermission
            OnboardingStep.AccessibilityPermission -> { /* Onboarding finished */ }
        }
    }
}

sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object NotificationPermission : OnboardingStep()
    object AccessibilityPermission : OnboardingStep()
}
