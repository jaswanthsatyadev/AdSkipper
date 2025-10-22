package com.evolvarc.adskipper.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    onNavigateUp: () -> Unit
) {
    val isAutoMuteEnabled by viewModel.autoMuteAds.collectAsStateWithLifecycle()
    val isVibrateOnSkipEnabled by viewModel.vibrateOnSkip.collectAsStateWithLifecycle()
    val isShowNotificationEnabled by viewModel.showNotification.collectAsStateWithLifecycle()
    val totalAdsSkipped by viewModel.totalAdsSkipped.collectAsStateWithLifecycle()
    val totalTimeSaved by viewModel.totalTimeSaved.collectAsStateWithLifecycle()
    val skipDelay by viewModel.skipDelay.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Statistics") },
            text = { Text("Are you sure you want to reset all statistics? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetStatistics()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ServiceSettingsSection(
                isAutoMuteEnabled = isAutoMuteEnabled,
                isVibrateOnSkipEnabled = isVibrateOnSkipEnabled,
                isShowNotificationEnabled = isShowNotificationEnabled,
                skipDelay = skipDelay,
                onAutoMuteChanged = { viewModel.setAutoMuteAds(it) },
                onVibrateOnSkipChanged = { viewModel.setVibrateOnSkip(it) },
                onShowNotificationChanged = { viewModel.setShowNotification(it) },
                onSkipDelayChanged = { viewModel.setSkipDelay(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatisticsSection(totalAdsSkipped, totalTimeSaved, onResetClick = { showResetDialog = true })
            Spacer(modifier = Modifier.height(16.dp))
            AboutSection()
            Spacer(modifier = Modifier.height(16.dp))
            PremiumSection()
        }
    }
}

@Composable
fun ServiceSettingsSection(
    isAutoMuteEnabled: Boolean,
    isVibrateOnSkipEnabled: Boolean,
    isShowNotificationEnabled: Boolean,
    skipDelay: Int,
    onAutoMuteChanged: (Boolean) -> Unit,
    onVibrateOnSkipChanged: (Boolean) -> Unit,
    onShowNotificationChanged: (Boolean) -> Unit,
    onSkipDelayChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.service_settings_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingRow(title = stringResource(id = R.string.auto_mute_ads), checked = isAutoMuteEnabled, onCheckedChange = onAutoMuteChanged)
                SettingRow(title = stringResource(id = R.string.vibrate_on_skip), checked = isVibrateOnSkipEnabled, onCheckedChange = onVibrateOnSkipChanged)
                SettingRow(title = stringResource(id = R.string.show_notification), checked = isShowNotificationEnabled, onCheckedChange = onShowNotificationChanged)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${stringResource(id = R.string.skip_delay)}: ${skipDelay}ms")
                Slider(
                    value = skipDelay.toFloat(),
                    onValueChange = { onSkipDelayChanged(it.toInt()) },
                    valueRange = 0f..1000f,
                    steps = 20
                )
            }
        }
    }
}

@Composable
fun StatisticsSection(totalAdsSkipped: Int, totalTimeSaved: Long, onResetClick: () -> Unit) {
    Column {
        Text(
            text = stringResource(id = R.string.statistics_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("$totalAdsSkipped ads skipped")
                Text("${totalTimeSaved / 60} minutes saved")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onResetClick) {
                    Text(stringResource(id = R.string.reset_statistics))
                }
            }
        }
    }
}

@Composable
fun AboutSection() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = AppUtils.getAppVersion(context)

    Column {
        Text(
            text = stringResource(id = R.string.about_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${stringResource(id = R.string.app_version)}: $appVersion", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(id = R.string.developer_details), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.linkedin_profile),
                    modifier = Modifier.clickable { uriHandler.openUri("https://www.linkedin.com/in/jaswanth-satya-dev/") },
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.github_profile),
                    modifier = Modifier.clickable { uriHandler.openUri("https://github.com/jaswanthsatyadev") },
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(id = R.string.rate_on_play_store))
                }
            }
        }
    }
}

@Composable
fun PremiumSection() {
    Column {
        Text(
            text = stringResource(id = R.string.premium_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.go_premium),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = stringResource(id = R.string.premium_features))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "- Pro ad detection engine")
                Text(text = "- Detailed statistics")
                Text(text = "- No ads (in this app)")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {}, enabled = false) {
                    Text(stringResource(id = R.string.coming_soon))
                }
            }
        }
    }
}

@Composable
fun SettingRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AdSkipperTheme {
        SettingsScreen(onNavigateUp = {})
    }
}
