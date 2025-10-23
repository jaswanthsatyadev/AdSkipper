package com.evolvarc.adskipper.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check     // For vibrate on skip
import androidx.compose.material.icons.filled.Notifications  // For hide notification
import androidx.compose.material.icons.filled.Info    // For skip delay (clock icon)
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evolvarc.adskipper.R
import com.evolvarc.adskipper.ui.settings.viewmodel.SettingsViewModel
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import com.evolvarc.adskipper.utils.AppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateUp: () -> Unit
) {
    val isVibrateOnSkipEnabled by viewModel.vibrateOnSkip.collectAsStateWithLifecycle()
    val isShowNotificationEnabled by viewModel.showNotification.collectAsStateWithLifecycle()
    val skipDelay by viewModel.skipDelay.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 24.dp + paddingValues.calculateBottomPadding()
            )
        ) {
            item {
                ServiceSettingsSection(
                    isVibrateOnSkipEnabled = isVibrateOnSkipEnabled,
                    isShowNotificationEnabled = isShowNotificationEnabled,
                    skipDelay = skipDelay,
                    onVibrateOnSkipChanged = { viewModel.setVibrateOnSkip(it) },
                    onShowNotificationChanged = { viewModel.setShowNotification(it) },
                    onSkipDelayChanged = { viewModel.setSkipDelay(it) }
                )
            }

            item {
                AboutSection()
            }
        }
    }
}

@Composable
fun ServiceSettingsSection(
    isVibrateOnSkipEnabled: Boolean,
    isShowNotificationEnabled: Boolean,
    skipDelay: Int,
    onVibrateOnSkipChanged: (Boolean) -> Unit,
    onShowNotificationChanged: (Boolean) -> Unit,
    onSkipDelayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.service_settings_title),
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Vibrate on Skip
        SettingItemCard(
            icon = Icons.Filled.Notifications,
            title = stringResource(R.string.vibrate_on_skip),
            subtitle = "Haptic feedback when ad is skipped",
            checked = isVibrateOnSkipEnabled,
            onCheckedChange = onVibrateOnSkipChanged,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )

        // Hide Notification
        SettingItemCard(
            icon = Icons.Filled.Notifications,
            title = "Hide notification",
            subtitle = "Remove notification when not needed",
            checked = isShowNotificationEnabled,
            onCheckedChange = onShowNotificationChanged,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            inverted = true
        )

        // Skip Delay
        SkipDelayCard(
            skipDelay = skipDelay,
            onSkipDelayChanged = onSkipDelayChanged
        )
    }
}

@Composable
fun SettingItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
    inverted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Switch
            Switch(
                checked = if (inverted) !checked else checked,
                onCheckedChange = { onCheckedChange(if (inverted) !it else it) },
                modifier = Modifier.padding(8.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun SkipDelayCard(
    skipDelay: Int,
    onSkipDelayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.skip_delay),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wait before clicking skip button",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Slider with range 0.1 to 0.5 seconds (100 to 500 ms)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Slider(
                    value = skipDelay.toFloat(),
                    onValueChange = { onSkipDelayChanged(it.toInt()) },
                    valueRange = 100f..500f,
                    steps = 40,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "%.1fs".format(skipDelay / 1000f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AboutSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = AppUtils.getAppVersion(context)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "About",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // App Version
        AboutItemCard(
            icon = Icons.Filled.Info,
            title = stringResource(R.string.app_version),
            value = appVersion,
            iconBgColor = Color(0xFF6366F1)
        )

        // Developer
        AboutItemCard(
            icon = Icons.Rounded.Person,
            title = stringResource(R.string.developer_details),
            value = "Jaswanth Satya Dev",
            iconBgColor = Color(0xFF7C3AED),
            onClick = {
                val linkedInUrl = "https://www.linkedin.com/in/jaswanth-satya-dev/"
                uriHandler.openUri(linkedInUrl)
            }
        )

        // GitHub
        AboutItemCard(
            icon = Icons.Rounded.Favorite,
            title = "GitHub Repository",
            value = "View source code",
            iconBgColor = Color(0xFF1F2937),
            onClick = {
                val githubUrl = "https://github.com/jaswanthsatyadev"
                uriHandler.openUri(githubUrl)
            }
        )

        // Rate on Play Store
        AboutItemCard(
            icon = Icons.Filled.Star,
            title = stringResource(R.string.rate_on_play_store),
            value = "",
            iconBgColor = Color(0xFFFCD34D),
            showArrow = true
        )

        // Share with Friends
        AboutItemCard(
            icon = Icons.Filled.Share,
            title = "Share with friends",
            value = "",
            iconBgColor = Color(0xFF8B5CF6),
            onClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out AdSkipper - Auto skip YouTube ads! https://play.google.com/store/apps/details?id=com.evolvarc.adskipper")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share AdSkipper"))
            },
            showArrow = true
        )

        // Feedback & Support
        AboutItemCard(
            icon = Icons.Rounded.Settings,
            title = "Feedback & Support",
            value = "",
            iconBgColor = Color(0xFF06B6D4),
            showArrow = true
        )
    }
}

@Composable
fun AboutItemCard(
    icon: ImageVector,
    title: String,
    value: String = "",
    iconBgColor: Color,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arrow if needed
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AdSkipperTheme {
        SettingsScreen(onNavigateUp = {})
    }
}
