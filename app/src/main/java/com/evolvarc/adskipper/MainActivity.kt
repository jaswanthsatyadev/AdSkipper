package com.evolvarc.adskipper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.evolvarc.adskipper.data.UserDataStore
import com.evolvarc.adskipper.ui.home.HomeScreen
import com.evolvarc.adskipper.ui.onboarding.OnboardingScreen
import com.evolvarc.adskipper.ui.settings.SettingsScreen
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdSkipperTheme {
                // Use null as initial state to show loading screen instead of guessing
                val onboardingComplete by userDataStore.onboardingComplete.collectAsState(initial = null)

                when (onboardingComplete) {
                    null -> {
                        // Show loading screen while data is being fetched
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    true -> {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    onNavigateToSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(onNavigateUp = { navController.navigateUp() })
                            }
                        }
                    }
                    false -> {
                        OnboardingScreen(
                            onOnboardingFinished = {
                                lifecycleScope.launch {
                                    userDataStore.setOnboardingComplete(true)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
