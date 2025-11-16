package com.evolvarc.adskipper.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.evolvarc.adskipper.ui.home.viewmodel.HomeViewModel
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHowItWorks: () -> Unit
) {
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsStateWithLifecycle()
    val isYouTubeActive by viewModel.isYouTubeActive.collectAsStateWithLifecycle()
    val totalAdsSkipped by viewModel.totalAdsSkipped.collectAsStateWithLifecycle(initialValue = 0)
    // tutorial overlay removed — no first-visit overlay shown anymore

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.checkServiceStatus()
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenContent(
            isServiceEnabled = isServiceEnabled,
            isYouTubeActive = isYouTubeActive,
            totalAdsSkipped = totalAdsSkipped,
            paddingValues = paddingValues,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToHowItWorks = onNavigateToHowItWorks
        )

        // tutorial overlay intentionally disabled
    }
}

@Composable
fun HomeScreenContent(
    isServiceEnabled: Boolean,
    isYouTubeActive: Boolean,
    totalAdsSkipped: Int,
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHowItWorks: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Status Card - Material 3 Expressive Design with fade-in animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                StatusCard(
                    isServiceEnabled = isServiceEnabled,
                    isYouTubeActive = isYouTubeActive,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Warning Banner if Service Disabled with smooth animation
            AnimatedVisibility(
                visible = !isServiceEnabled,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    targetOffsetY = { -20 },
                    animationSpec = tween(300)
                )
            ) {
                Column {
                    WarningBanner(
                        onEnableClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Stats Card with fade-in animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 150)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                StatsCard(
                    totalAdsSkipped = totalAdsSkipped,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How It Works Card
            HowItWorksCard(
                onNavigateToHowItWorks = onNavigateToHowItWorks,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
}

@Composable
fun StatusCard(
    isServiceEnabled: Boolean,
    isYouTubeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        !isServiceEnabled -> "Service Disabled"
        isYouTubeActive -> "Actively Skipping"
        else -> "Ready"
    }

    val statusSubtext = when {
        !isServiceEnabled -> "Enable accessibility service to start"
        isYouTubeActive -> "Watching YouTube • Auto-skipping ads"
        else -> "Monitoring for YouTube ads"
    }

    val backgroundColor = when {
        isServiceEnabled -> Color(0xFFE8E0FF)  // Light purple background when enabled
        else -> Color(0xFFFFE8E8)  // Light red background when disabled
    }

    val statusColor = when {
        isServiceEnabled -> Color(0xFF6750A4)  // Purple circle when enabled
        else -> Color(0xFFB3261E)  // Red circle when disabled
    }

    val textColor = when {
        isServiceEnabled && isYouTubeActive -> Color(0xFF22C55E)  // Green text when watching
        isServiceEnabled -> Color(0xFFFFA500)  // Orange text when enabled but not watching
        else -> Color(0xFFB3261E)  // Red text when disabled
    }

    Card(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Status Circle
            AnimatedStatusCircle(
                isActive = isServiceEnabled,
                isYouTubeActive = isYouTubeActive,
                circleColor = statusColor,
                modifier = Modifier
                    .size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusSubtext,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (isServiceEnabled && isYouTubeActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "▶ YouTube Status: Watching",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AnimatedStatusCircle(
    isActive: Boolean,
    isYouTubeActive: Boolean,
    circleColor: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = circleColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "statusCircleColor"
    )

    // Pulsing animation for active state
    val scale = androidx.compose.runtime.remember { Animatable(1f) }
    
    LaunchedEffect(isYouTubeActive) {
        if (isYouTubeActive) {
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = if (isActive) {
                if (isYouTubeActive) "▶" else "✓"
            } else {
                "✕"
            },
            label = "statusIcon"
        ) { icon ->
            Text(
                text = icon,
                color = Color.White,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WarningBanner(
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEF2F2)  // Light red background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alert icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 24.sp
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Service Not Enabled",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFDC2626)
                )
                
                Text(
                    text = "Enable accessibility service to start auto-skipping ads",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF991B1B)
                )

                Button(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Enable Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    totalAdsSkipped: Int,
    modifier: Modifier = Modifier
) {
    // Animate the counter value
    val animatedCount = androidx.compose.runtime.remember { Animatable(0f) }
    
    LaunchedEffect(totalAdsSkipped) {
        animatedCount.animateTo(
            targetValue = totalAdsSkipped.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
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
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total Ads Skipped",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedContent(
                targetState = animatedCount.value.toInt(),
                label = "counterAnimation"
            ) { count ->
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFFFFA500)  // Golden color
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                
                Text(
                    text = "all-time total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val timeSaved = totalAdsSkipped * 5 // ~5 seconds per ad
            val minutesSaved = timeSaved / 60
            
            if (minutesSaved > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "⏱️ ~$minutesSaved minutes saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFA500).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HowItWorksCard(
    onNavigateToHowItWorks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onNavigateToHowItWorks() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.how_it_works),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Learn how AdSkipper detects and skips YouTube ads automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AdSkipperTheme {
        HomeScreenContent(
            isServiceEnabled = true,
            isYouTubeActive = true,
            totalAdsSkipped = 1247,
            onNavigateToSettings = {},
            onNavigateToHowItWorks = {}
        )
    }
}
