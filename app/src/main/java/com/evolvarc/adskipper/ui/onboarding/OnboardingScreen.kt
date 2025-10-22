package com.evolvarc.adskipper.ui.onboarding

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.ui.onboarding.viewmodel.OnboardingStep
import com.evolvarc.adskipper.ui.onboarding.viewmodel.OnboardingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingFinished: () -> Unit
) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()

    AnimatedContent(targetState = step) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                OnboardingStep.Welcome -> WelcomeStep { viewModel.nextStep() }
                OnboardingStep.NotificationPermission -> NotificationPermissionStep { viewModel.nextStep() }
                OnboardingStep.AccessibilityPermission -> AccessibilityPermissionStep(onOnboardingFinished)
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Text(
        text = stringResource(id = R.string.welcome_to_adskipper),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(id = R.string.adskipper_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = onNext) {
        Text(text = stringResource(id = R.string.get_started))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionStep(onNext: () -> Unit) {
    val notificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (notificationPermission.status.isGranted) {
        LaunchedEffect(Unit) {
            onNext()
        }
    }

    Text(
        text = "Enable Notifications",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "We need to show a notification to keep the service running.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = { notificationPermission.launchPermissionRequest() }) {
        Text(text = "Enable Notifications")
    }
}

@Composable
fun AccessibilityPermissionStep(onOnboardingFinished: () -> Unit) {
    val context = LocalContext.current

    Text(
        text = stringResource(id = R.string.enable_accessibility_service),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(id = R.string.accessibility_service_description),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = { 
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }) {
        Text(text = stringResource(id = R.string.open_settings))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onOnboardingFinished) {
        Text(text = stringResource(id = R.string.done))
    }
}
