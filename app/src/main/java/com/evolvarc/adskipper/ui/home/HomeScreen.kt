package com.evolvarc.adskipper.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import com.evolvarc.adskipper.ui.home.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHowItWorks: () -> Unit
) {
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsStateWithLifecycle()
    val isYouTubeActive by viewModel.isYouTubeActive.collectAsStateWithLifecycle()
    val totalAdsSkipped by viewModel.totalAdsSkipped.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkServiceStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        isServiceEnabled = isServiceEnabled,
        isYouTubeActive = isYouTubeActive,
        totalAdsSkipped = totalAdsSkipped,
        paddingValues = paddingValues,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToHowItWorks = onNavigateToHowItWorks
    )
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
    var showDisclosureDialog by remember { mutableStateOf(false) }

    if (showDisclosureDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDisclosureDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Accessibility Service Disclosure",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Why we use this permission:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = "To automatically detect and skip YouTube ads.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF616161)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "How it works:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = "AdSkipper detects the 'Skip Ad' button on your screen and creates a tap action to skip it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF616161)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "What data is collected:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = "No data is collected or shared. The service runs locally on your device.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF616161)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showDisclosureDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                             border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E88E5)),
                             colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E88E5))
                        ) {
                             Text("Decline")
                        }

                        Button(
                            onClick = {
                                showDisclosureDialog = false
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                context.startActivity(intent)
                            },
                             modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(paddingValues) // Apply scaffold padding
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {


        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AdSkipper",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = Color(0xFF1E88E5)
            )
            
            androidx.compose.material3.IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        StatusCard(
            isServiceEnabled = isServiceEnabled,
            isYouTubeActive = isYouTubeActive
        )

        if (!isServiceEnabled) {
            WarningBanner(
                onEnableClick = {
                    showDisclosureDialog = true
                }
            )
        }

        StatsCard(totalAdsSkipped = totalAdsSkipped)
        
        HowItWorksCard(onNavigateToHowItWorks = onNavigateToHowItWorks)
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatusCard(
    isServiceEnabled: Boolean,
    isYouTubeActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // Get context here
    val statusText = when {
        !isServiceEnabled -> "Service Disabled"
        isYouTubeActive -> "AdSkipper Active"
        else -> "Ready to Skip"
    }

    val statusSubtext = when {
        !isServiceEnabled -> "Enable accessibility to start"
        isYouTubeActive -> "Watching YouTube • Scanning for ads"
        else -> "Open YouTube to start skipping"
    }

    // Gradient Colors
    val activeGradient = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32)) // Light Green -> Dark Green
    val disabledGradient = listOf(Color(0xFFEF5350), Color(0xFFC62828)) // Light Red -> Dark Red
    val readyGradient = listOf(Color(0xFF66BB6A), Color(0xFF43A047)) // Medium Green -> Green

    val backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = when {
            !isServiceEnabled -> disabledGradient
            isYouTubeActive -> activeGradient
            else -> readyGradient
        }
    )

    Card(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Important for gradient
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Status Circle
                AnimatedStatusCircle(
                    isActive = isServiceEnabled,
                    isYouTubeActive = isYouTubeActive,
                    circleColor = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusSubtext,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                
                if (isServiceEnabled && !isYouTubeActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                            if (intent != null) context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                         Icon(Icons.Rounded.PlayArrow, null, Modifier.size(18.dp))
                         Spacer(Modifier.width(8.dp))
                         Text("Open YouTube", fontSize = 14.sp)
                    }
                }
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
    // Pulsing animation for attention
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)  // Light red background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alert icon with pulse
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .graphicsLayer { alpha = pulseAlpha },
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

                val buttonInteraction = androidx.compose.runtime.remember { MutableInteractionSource() }
                val isButtonPressed by buttonInteraction.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isButtonPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "buttonScale"
                )

                Button(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    interactionSource = buttonInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
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
    // ... existing stats card ...
    // Blue Gradient
    val blueGradient = listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
    
    Card(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Brush.linearGradient(blueGradient))
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Total Skipped",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = "$totalAdsSkipped Ads",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    
                    val timeSaved = totalAdsSkipped * 5 / 60
                    if (timeSaved > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Rounded.Info, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                             Spacer(Modifier.width(4.dp))
                             Text(
                                text = "Saved ~$timeSaved mins",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                // Icon Illustration
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀", fontSize = 32.sp)
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
    val interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "scale"
    )

    Card(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onNavigateToHowItWorks() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "How it works",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )

                Text(
                    text = "Learn about our safe ad detection tech",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE3F2FD), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
            }
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
