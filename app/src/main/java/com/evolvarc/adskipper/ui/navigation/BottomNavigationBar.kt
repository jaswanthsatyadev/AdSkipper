package com.evolvarc.adskipper.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evolvarc.adskipper.R

enum class NavigationItem(val route: String, val label: Int, val icon: ImageVector) {
    HOME("home", R.string.nav_home, Icons.Filled.Home),
    HOW_IT_WORKS("how_it_works", R.string.nav_how_it_works, Icons.Filled.Info),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings),
    ABOUT("about", R.string.nav_about, Icons.Filled.Info)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationItem.values().forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        item.icon, 
                        contentDescription = stringResource(item.label)
                    ) 
                },
                label = { 
                    Text(
                        text = stringResource(item.label),
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    ) 
                },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E88E5),
                    selectedTextColor = Color(0xFF1E88E5),
                    unselectedIconColor = Color(0xFF757575),
                    unselectedTextColor = Color(0xFF757575),
                    indicatorColor = Color(0xFF1E88E5).copy(alpha = 0.12f)
                )
            )
        }
    }
}
