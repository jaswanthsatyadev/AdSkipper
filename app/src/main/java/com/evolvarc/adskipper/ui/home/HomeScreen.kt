package com.evolvarc.adskipper.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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

            // Status Card - Material 3 Expressive Design
            StatusCard(
                isServiceEnabled = isServiceEnabled,
                isYouTubeActive = isYouTubeActive,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Warning Banner if Service Disabled
            if (!isServiceEnabled) {
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

            // Stats Card
            StatsCard(
                totalAdsSkipped = totalAdsSkipped,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            )

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
        !isServiceEnabled -> "Disabled"
        isYouTubeActive -> "Active"
        else -> "Enabled"
    }

    val statusSubtext = when {
        !isServiceEnabled -> stringResource(R.string.waiting_for_app)
        isYouTubeActive -> "Currently watching YouTube"
        else -> "Ready to skip ads"
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
        targetValue = circleColor
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isActive) {
                if (isYouTubeActive) "▶" else "✓"
            } else {
                "✕"
            },
            color = Color.White,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WarningBanner(
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.enable_accessibility),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enable_now))
            }
        }
    }
}

@Composable
fun StatsCard(
    totalAdsSkipped: Int,
    modifier: Modifier = Modifier
) {
    val todayAds = 47  // Example value - can be made dynamic
    
    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Total Ads Skipped",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = totalAdsSkipped.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFFFFA500)  // Golden color
                )

                Text(
                    text = "all-time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),  // Light golden background
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "+$todayAds",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color(0xFFFFA500)
                    )
                    Text(
                        text = "today",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFA500).copy(alpha = 0.7f)
                    )
                }
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
