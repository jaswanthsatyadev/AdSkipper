package com.evolvarc.adskipper.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.ui.settings.viewmodel.SettingsViewModel
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import com.evolvarc.adskipper.utils.AppUtils

import com.evolvarc.adskipper.utils.BatteryOptimizationHelper

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import android.os.Build

import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.TextButton
import com.evolvarc.adskipper.service.SkipTextManager
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateToTroubleshoot: () -> Unit
) {
    val isVibrateOnSkipEnabled by viewModel.vibrateOnSkip.collectAsStateWithLifecycle()
    val isShowNotificationEnabled by viewModel.showNotification.collectAsStateWithLifecycle()
    val skipDelay by viewModel.skipDelay.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Settings Title with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E88E5))
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
        }

        // Replaced Expandable TroubleshootCard with Navigation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onNavigateToTroubleshoot),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), // Light Blue for emphasis
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E88E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build, // Use Build instead of Settings for "Fix"
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "App Not Working?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0)
                    )
                    Text(
                        text = "Tap here to troubleshoot issues",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2)
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Should be ArrowForward but using Back mirrored
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.rotate(180f) // Point right
                )
            }
        }

        ServiceSettingsSection(
            isVibrateOnSkipEnabled = isVibrateOnSkipEnabled,
            isShowNotificationEnabled = isShowNotificationEnabled,
            skipDelay = skipDelay,
            selectedLanguage = selectedLanguage,
            onVibrateOnSkipChanged = { viewModel.setVibrateOnSkip(it) },
            onShowNotificationChanged = { viewModel.setShowNotification(it) },
            onSkipDelayChanged = { viewModel.setSkipDelay(it) },
            onLanguageChanged = { viewModel.setSelectedLanguage(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// TroubleshootCard was removed upon refactoring to a dedicated screen.

@Composable
fun ServiceSettingsSection(
    isVibrateOnSkipEnabled: Boolean,
    isShowNotificationEnabled: Boolean,
    skipDelay: Int,
    selectedLanguage: String,
    onVibrateOnSkipChanged: (Boolean) -> Unit,
    onShowNotificationChanged: (Boolean) -> Unit,
    onSkipDelayChanged: (Int) -> Unit,
    onLanguageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelected = {
                onLanguageChanged(it)
                showLanguageDialog = false
            },
            onDismissRequest = { showLanguageDialog = false }
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Ad Skipping Preferences",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color(0xFF1E88E5),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Language Selection
        LanguageSelectionCard(
            selectedLanguage = selectedLanguage,
            onClick = { showLanguageDialog = true }
        )

        // Vibrate on Skip with animation
        AnimatedSettingCard(
            icon = Icons.Filled.Phone,
            title = "Vibration Feedback",
            subtitle = "Feel haptic feedback when ads are skipped",
            checked = isVibrateOnSkipEnabled,
            onCheckedChange = onVibrateOnSkipChanged,
            accentColor = Color(0xFF4CAF50)
        )

        // Hide Notification with animation
        AnimatedSettingCard(
            icon = Icons.Filled.Notifications,
            title = "Hide Notifications",
            subtitle = "Keep your notification bar clean",
            checked = !isShowNotificationEnabled,
            onCheckedChange = { onShowNotificationChanged(!it) },
            accentColor = Color(0xFFE53935)
        )

        // Battery Optimization card
        BatteryOptimizationCard()

        // Skip Delay
        SkipDelayCard(
            skipDelay = skipDelay,
            onSkipDelayChanged = onSkipDelayChanged
        )
    }
}

@Composable
fun AnimatedSettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    // Animated colors and sizes
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.1f) else Color.White,
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFFBDBDBD),
        animationSpec = spring(),
        label = "iconBgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.4f) else Color(0xFFE0E0E0),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (checked) accentColor else Color(0xFF212121)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            // Animated Switch
            AnimatedSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                accentColor = accentColor
            )
        }
    }
}

@Composable
fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color(0xFFBDBDBD),
        animationSpec = tween(200),
        label = "thumbColor"
    )
    
    val trackColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFFE0E0E0),
        animationSpec = tween(200),
        label = "trackColor"
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = thumbColor,
            checkedTrackColor = trackColor,
            uncheckedThumbColor = thumbColor,
            uncheckedTrackColor = trackColor
        )
    )
}

@Composable
fun SkipDelayCard(
    skipDelay: Int,
    onSkipDelayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val delayLabel = remember(skipDelay) {
        val labels = listOf("0.1s", "0.2s", "0.3s", "0.4s", "0.5s")
        val index = skipDelay.coerceIn(0, labels.lastIndex)
        labels[index]
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Skip Delay",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Wait before skipping ads",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }

                // Display current delay value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                            text = delayLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Slider - 0.1s to 0.5s (stored as 0-4, where 0=0.1s, 1=0.2s, etc)
            Slider(
                value = skipDelay.toFloat(),
                onValueChange = { onSkipDelayChanged(it.roundToInt().coerceIn(0, 4)) },
                valueRange = 0f..4f,
                steps = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFFFF9800),
                    activeTrackColor = Color(0xFFFF9800),
                    inactiveTrackColor = Color(0xFFFFE0B2)
                )
            )

            // Helper text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.1s (Fast)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    text = "0.5s (Safe)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
fun BatteryOptimizationCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
    val packageName = context.packageName
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isBatteryOptimizationDisabled by androidx.compose.runtime.remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(packageName))
    }
    var pendingRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(pendingRefresh) {
        if (pendingRefresh) {
            delay(1200)
            isBatteryOptimizationDisabled = powerManager.isIgnoringBatteryOptimizations(packageName)
            pendingRefresh = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptimizationDisabled = powerManager.isIgnoringBatteryOptimizations(packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isBatteryOptimizationDisabled) Color(0xFF9C27B0).copy(alpha = 0.1f) else Color.White,
        animationSpec = tween(300),
        label = "bgColor"
    )
    
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isBatteryOptimizationDisabled) Color(0xFF9C27B0) else Color(0xFFBDBDBD),
        animationSpec = spring(),
        label = "iconBgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isBatteryOptimizationDisabled) Color(0xFF9C27B0).copy(alpha = 0.4f) else Color(0xFFE0E0E0),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated icon background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Run in Battery Saver",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isBatteryOptimizationDisabled) Color(0xFF9C27B0) else Color(0xFF212121)
                )
                Text(
                    text = "Keep skipping ads even in battery saving mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            // Animated Switch
            AnimatedSwitch(
                checked = isBatteryOptimizationDisabled,
                onCheckedChange = { checked ->
                    if (checked && !isBatteryOptimizationDisabled) {
                        // Request battery optimization exemption
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:$packageName")
                            }
                            context.startActivity(intent)
                            pendingRefresh = true
                        } catch (e: Exception) {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            }
                            pendingRefresh = true
                        }
                    } else if (!checked && isBatteryOptimizationDisabled) {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                        pendingRefresh = true
                    }
                },
                accentColor = Color(0xFF9C27B0)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AdSkipperTheme {
        SettingsScreen(
            onNavigateToTroubleshoot = {}
        )
    }
}

@Composable
fun LanguageSelectionCard(
    selectedLanguage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languageName = if (selectedLanguage == "ALL") {
        "Auto-Detect (All Languages)"
    } else {
        SkipTextManager.languages.find { it.code == selectedLanguage }?.displayName ?: selectedLanguage
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF673AB7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Skip Language",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = languageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF673AB7),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                "Select Language",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    LanguageOptionItem(
                        text = "Auto-Detect (All Languages)",
                        isSelected = currentLanguage == "ALL",
                        onClick = { onLanguageSelected("ALL") }
                    )
                }

                items(SkipTextManager.languages.sortedBy { it.displayName }) { language ->
                    LanguageOptionItem(
                        text = language.displayName,
                        isSelected = currentLanguage == language.code,
                        onClick = { onLanguageSelected(language.code) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun LanguageOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFF673AB7).copy(alpha = 0.1f) else Color.Transparent)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) Color(0xFF673AB7) else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color(0xFF673AB7),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
