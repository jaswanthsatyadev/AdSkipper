package com.evolvarc.adskipper.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.evolvarc.adskipper.R

enum class NavigationItem(val route: String, val label: Int, val icon: ImageVector) {
    HOME("home", R.string.nav_home, Icons.Filled.Home),
    HOW_IT_WORKS("how_it_works", R.string.nav_how_it_works, Icons.Filled.Info),
    SUBSCRIPTION("subscription", R.string.nav_subscription, Icons.Filled.Star),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NavigationItem.values().forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                label = { Text(stringResource(item.label)) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
