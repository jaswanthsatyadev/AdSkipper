package com.evolvarc.adskipper.ui.about

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evolvarc.adskipper.R
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
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), clip = false)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_round),
                contentDescription = "AdSkipper logo",
                modifier = Modifier.size(88.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Trust Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)  // Light green
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 28.sp)
                    Text(
                        text = "Privacy First",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1B5E20)
                    )
                }
                
                Text(
                    text = "No login • No ads • No tracking • 100% offline\nAll processing happens on your device\nWe never collect, store, or transmit your data",
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF2E7D32)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PrivacyFeature("🔒", "Secure")
                    PrivacyFeature("📖", "Open Source")
                    PrivacyFeature("💝", "Free Forever")
                }
            }
        }

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

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItemCard(
                icon = Icons.Filled.Person,
                title = "LinkedIn",
                value = "linkedin.com/in/jaswanth-satya-dev",
                iconBgColor = Color(0xFF0A66C2),
                iconContent = { LinkedInGlyph() },
                onClick = {
                    uriHandler.openUri("https://www.linkedin.com/in/jaswanth-satya-dev/")
                }
            )

            InfoItemCard(
                icon = Icons.Filled.Share,
                title = "X",
                value = "@jaswanthsatydev",
                iconBgColor = Color.Black,
                iconContent = { XGlyph() },
                onClick = {
                    uriHandler.openUri("https://x.com/jaswanthsatydev")
                }
            )

            InfoItemCard(
                icon = Icons.Filled.Favorite,
                title = "GitHub",
                value = "github.com/jaswanthsatyadev/AdSkipper",
                iconBgColor = Color(0xFF24292E),
                iconContent = { GitHubGlyph() },
                onClick = {
                    uriHandler.openUri("https://github.com/jaswanthsatyadev/AdSkipper")
                }
            )
        }

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

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoItemCard(
                icon = Icons.Filled.Star,
                title = "Rate on Play Store",
                value = "Help us grow",
                iconBgColor = Color(0xFFFCD34D),
                showArrow = true
            )

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
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Terms & Conditions",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AdSkipper is an assistive accessibility tool that simply automates the same tap you would perform on the \"Skip Ad\" button.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "We do not block, filter, or modify YouTube content. The YouTube app remains untouched and all actions happen locally on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "By using AdSkipper you agree to use it responsibly, comply with YouTube's Terms of Service, and accept that you are solely responsible for how the service is used.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

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
    iconContent: (@Composable () -> Unit)? = null,
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
                if (iconContent != null) {
                    iconContent()
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
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

@Composable
fun LinkedInGlyph() {
    Text(
        text = "in",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        ),
        color = Color.White
    )
}

@Composable
fun XGlyph() {
    Text(
        text = "X",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 1.sp
        ),
        color = Color.White
    )
}

@Composable
fun GitHubGlyph() {
    Canvas(modifier = Modifier.size(26.dp)) {
        val bg = Color.White
        val accent = Color(0xFF24292E)
        val headRadius = size.minDimension / 2.6f
        val center = Offset(size.width / 2f, size.height / 2f + 1.5f)
        val earWidth = headRadius * 0.9f
        val earHeight = headRadius * 0.9f

        val leftEar = Path().apply {
            moveTo(center.x - earWidth, center.y - headRadius)
            lineTo(center.x - earWidth * 0.4f, center.y - headRadius * 0.4f)
            lineTo(center.x - earWidth * 1.2f, center.y - headRadius * 0.3f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(center.x + earWidth, center.y - headRadius)
            lineTo(center.x + earWidth * 0.4f, center.y - headRadius * 0.4f)
            lineTo(center.x + earWidth * 1.2f, center.y - headRadius * 0.3f)
            close()
        }

        drawPath(leftEar, bg)
        drawPath(rightEar, bg)
        drawCircle(color = bg, radius = headRadius, center = center)

        drawCircle(
            color = accent,
            radius = headRadius * 0.2f,
            center = center + Offset(-headRadius * 0.45f, -headRadius * 0.1f)
        )
        drawCircle(
            color = accent,
            radius = headRadius * 0.2f,
            center = center + Offset(headRadius * 0.45f, -headRadius * 0.1f)
        )

        drawLine(
            color = accent,
            start = center + Offset(-headRadius * 0.5f, headRadius * 0.4f),
            end = center + Offset(headRadius * 0.5f, headRadius * 0.4f),
            strokeWidth = headRadius * 0.25f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PrivacyFeature(icon: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF2E7D32)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AdSkipperTheme {
        AboutScreen()
    }
}
