package com.evolvarc.adskipper.ui.about

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evolvarc.adskipper.ui.theme.AdSkipperTheme
import com.evolvarc.adskipper.utils.AppUtils

@Composable
fun AboutScreen(
    paddingValues: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val appVersion = AppUtils.getAppVersion(context)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App Icon
        Text(
            text = "👑",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // App Name
        Text(
            text = "AdSkipper",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        // Version
        Text(
            text = "Version $appVersion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Automatically skip YouTube ads",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // App Info Section
        Text(
            text = "App Info",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Developer - LinkedIn
        InfoItemCard(
            icon = Icons.Filled.Person,
            title = "Developer",
            value = "Jaswanth Satya Dev",
            iconBgColor = Color(0xFF0A66C2),
            onClick = {
                uriHandler.openUri("https://www.linkedin.com/in/jaswanth-satya-dev/")
            }
        )

        // X (Twitter)
        InfoItemCard(
            icon = Icons.Filled.Share,
            title = "X (Twitter)",
            value = "@jaswanthsatydev",
            iconBgColor = Color(0xFF000000),
            onClick = {
                uriHandler.openUri("https://x.com/jaswanthsatydev")
            }
        )

        // GitHub
        InfoItemCard(
            icon = Icons.Filled.Favorite,
            title = "GitHub",
            value = "jaswanthsatyadev",
            iconBgColor = Color(0xFF1F2937),
            onClick = {
                uriHandler.openUri("https://github.com/jaswanthsatyadev")
            }
        )

        // Email Contact
        InfoItemCard(
            icon = Icons.Filled.Info,
            title = "Contact",
            value = "contact@evolvarc.com",
            iconBgColor = Color(0xFFEF4444),
            onClick = {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@evolvarc.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "AdSkipper Feedback")
                }
                context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Section
        Text(
            text = "Support AdSkipper",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rate on Play Store
        InfoItemCard(
            icon = Icons.Filled.Star,
            title = "Rate on Play Store",
            value = "Help us grow",
            iconBgColor = Color(0xFFFCD34D),
            showArrow = true
        )

        // Share with Friends
        InfoItemCard(
            icon = Icons.Filled.Share,
            title = "Share with Friends",
            value = "Spread the word",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = "Made with ❤️ for a better YouTube experience",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "© 2025 AdSkipper",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun InfoItemCard(
    icon: ImageVector,
    title: String,
    value: String,
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
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title and value
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showArrow) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AdSkipperTheme {
        AboutScreen()
    }
}
