package com.evolvarc.adskipper

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evolvarc.adskipper.data.UserDataStore
import com.evolvarc.adskipper.receivers.AdSkippedReceiver
import com.evolvarc.adskipper.ui.about.AboutScreen
import com.evolvarc.adskipper.ui.home.HomeScreen
import com.evolvarc.adskipper.ui.howitworks.HowItWorksScreen
import com.evolvarc.adskipper.ui.navigation.BottomNavigationBar
import com.evolvarc.adskipper.ui.onboarding.OnboardingScreen
import com.evolvarc.adskipper.ui.settings.SettingsScreen
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
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val orderedRoutes = listOf("home", "how_it_works", "settings", "about")

    val slideSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun AnimatedContentTransitionScope<NavBackStackEntry>.directionFor(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): AnimatedContentTransitionScope.SlideDirection {
        val initialIndex = orderedRoutes.indexOf(initial.destination.route)
        val targetIndex = orderedRoutes.indexOf(target.destination.route)
        if (initialIndex == -1 || targetIndex == -1) {
            return AnimatedContentTransitionScope.SlideDirection.Start
        }
        return if (targetIndex >= initialIndex) {
            AnimatedContentTransitionScope.SlideDirection.Start
        } else {
            AnimatedContentTransitionScope.SlideDirection.End
        }
    }

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
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val direction = directionFor(initialState, targetState)
                slideIntoContainer(
                    towards = direction,
                    animationSpec = slideSpec
                ) + fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                val direction = directionFor(initialState, targetState)
                slideOutOfContainer(
                    towards = direction,
                    animationSpec = slideSpec
                ) + fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
            },
            popEnterTransition = {
                val direction = directionFor(initialState, targetState)
                slideIntoContainer(
                    towards = direction,
                    animationSpec = slideSpec
                ) + fadeIn(animationSpec = tween(450, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                val direction = directionFor(initialState, targetState)
                slideOutOfContainer(
                    towards = direction,
                    animationSpec = slideSpec
                ) + fadeOut(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium))
            }
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
            composable("settings") {
                SettingsScreen(paddingValues = paddingValues)
            }
            composable("about") {
                AboutScreen(paddingValues = paddingValues)
            }
        }
    }
}
