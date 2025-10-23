package com.evolvarc.adskipper

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evolvarc.adskipper.data.UserDataStore
import com.evolvarc.adskipper.receivers.AdSkippedReceiver
import com.evolvarc.adskipper.ui.home.HomeScreen
import com.evolvarc.adskipper.ui.howitworks.HowItWorksScreen
import com.evolvarc.adskipper.ui.navigation.BottomNavigationBar
import com.evolvarc.adskipper.ui.onboarding.OnboardingScreen
import com.evolvarc.adskipper.ui.settings.SettingsScreen
import com.evolvarc.adskipper.ui.subscription.SubscriptionScreen
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userDataStore: UserDataStore
    
    private val adSkippedReceiver = AdSkippedReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Register ad skipped receiver
        val filter = IntentFilter("com.evolvarc.adskipper.AD_SKIPPED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(adSkippedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(adSkippedReceiver, filter)
        }
        
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
                        MainAppScreen()
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
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(adSkippedReceiver)
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                HomeScreen(
                    paddingValues = paddingValues,
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHowItWorks = { navController.navigate("how_it_works") }
                )
            }
            composable("how_it_works") {
                HowItWorksScreen(paddingValues = paddingValues)
            }
            composable("subscription") {
                SubscriptionScreen(paddingValues = paddingValues)
            }
            composable("settings") {
                SettingsScreen(paddingValues = paddingValues, onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}
