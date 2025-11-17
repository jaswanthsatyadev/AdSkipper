package com.evolvarc.adskipper.ui.onboarding

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.ui.onboarding.viewmodel.OnboardingStep
import com.evolvarc.adskipper.ui.onboarding.viewmodel.OnboardingViewModel
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import com.evolvarc.adskipper.utils.AccessibilityServiceUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingFinished: () -> Unit
) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (fadeIn(animationSpec = tween(400)) + 
             slideInHorizontally(
                 initialOffsetX = { fullWidth -> fullWidth },
                 animationSpec = spring(
                     dampingRatio = Spring.DampingRatioMediumBouncy,
                     stiffness = Spring.StiffnessMedium
                 )
             )).togetherWith(
                fadeOut(animationSpec = tween(200)) + 
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(200)
                )
            )
        },
        label = "onboardingStep"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceEvenly,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Crown Icon with fade-in animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(800)) + 
                    slideInVertically(
                        initialOffsetY = { -100 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
        ) {
            Text(
                text = "👑",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Headline with fade-in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) + 
                    slideInVertically(
                        initialOffsetY = { -50 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
        ) {
            Text(
                text = "Welcome to AdSkipper",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description with fade-in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 300))
        ) {
            Text(
                text = "Automatically skip YouTube ads and reclaim your viewing time",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Privacy & Trust Badges
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 350))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Why Choose AdSkipper?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // First row of badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PrivacyBadge(icon = "🔒", text = "No Login")
                    PrivacyBadge(icon = "🚫", text = "No Ads")
                    PrivacyBadge(icon = "📵", text = "100% Offline")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Second row of badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PrivacyBadge(icon = "👁️", text = "No Tracking")
                    PrivacyBadge(icon = "💝", text = "Free Forever")
                    PrivacyBadge(icon = "📖", text = "Open Source")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Privacy Statement
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 400))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "🛡️ Your privacy is our priority. All processing happens on your device. We never collect, store, or transmit your data.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Feature Cards with staggered animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 400)) + 
                    slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
        ) {
            FeatureHighlightCard(
                icon = "⚡",
                title = "Ultra Fast",
                description = "Skip ads in milliseconds"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 500)) + 
                    slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
        ) {
            FeatureHighlightCard(
                icon = "🎯",
                title = "100% Accurate",
                description = "Never clicks the wrong button"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 600)) + 
                    slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
        ) {
            FeatureHighlightCard(
                icon = "🔒",
                title = "Private & Secure",
                description = "Your data stays on your device"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // CTA Button with fade-in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 700)) + 
                    slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip onboarding option with fade-in
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 800))
        ) {
            FilledTonalButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureHighlightCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 28.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Card(
            modifier = Modifier
                .size(100.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Enable Notifications",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We'll notify you when ads are being skipped. You can disable this anytime in settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { notificationPermission.launchPermissionRequest() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Enable Notifications",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccessibilityPermissionStep(onOnboardingFinished: () -> Unit) {
    val context = LocalContext.current
    var isServiceEnabled by androidx.compose.runtime.remember { mutableStateOf(false) }
    
    // Auto-detect accessibility service state
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Check every second
            val enabled = AccessibilityServiceUtils.isAccessibilityServiceEnabled(context)
            if (enabled && !isServiceEnabled) {
                isServiceEnabled = true
                // Auto-advance after short delay when service is enabled
                delay(800)
                onOnboardingFinished()
                break
            }
            isServiceEnabled = enabled
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with status indicator
        Box(contentAlignment = Alignment.TopEnd) {
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isServiceEnabled) 
                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isServiceEnabled) Icons.Filled.Check else Icons.Filled.Settings,
                        contentDescription = "Accessibility",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            // Status badge
            if (isServiceEnabled) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = 8.dp, y = (-8).dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = isServiceEnabled,
            label = "titleAnimation"
        ) { enabled ->
            Text(
                text = if (enabled) "Service Enabled!" else "Enable Accessibility Service",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center,
                color = if (enabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = isServiceEnabled,
            label = "descAnimation"
        ) { enabled ->
            Text(
                text = if (enabled) 
                    "Great! AdSkipper is ready to skip ads automatically."
                else
                    "AdSkipper needs accessibility access to detect and skip ads for you.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Progress indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (isServiceEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .animateContentSize()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isServiceEnabled) "Connected" else "Waiting for permission...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isServiceEnabled) FontWeight.Bold else FontWeight.Normal,
                    color = if (isServiceEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isServiceEnabled) {
            // Steps with enhanced animations
            StepCard(number = "1", text = "Tap 'Open Settings' below")
            Spacer(modifier = Modifier.height(12.dp))
            StepCard(number = "2", text = "Click 'Downloaded Apps' section")
            Spacer(modifier = Modifier.height(12.dp))
            StepCard(number = "3", text = "Find 'AdSkipper' in the list")
            Spacer(modifier = Modifier.height(12.dp))
            StepCard(number = "4", text = "Toggle it ON and come back")

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { 
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Settings",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            // Show success state
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Redirecting to app...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepCard(
    number: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$number. $text",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PrivacyBadge(icon: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
