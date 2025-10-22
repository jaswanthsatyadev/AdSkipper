package com.evolvarc.adskipper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                val onboardingComplete by userDataStore.onboardingComplete.collectAsState(initial = false)

                if (onboardingComplete) {
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
                } else {
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
