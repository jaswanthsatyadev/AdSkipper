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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var showDonationDialog by remember { mutableStateOf(false) }
    
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
            // AppLogo will use attached `adskipper_logo` drawable if the resource exists
            com.evolvarc.adskipper.ui.common.AppLogo(
                modifier = Modifier.size(100.dp),
                size = 100.dp,
                cornerRadius = 20.dp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Name
        Text(
            text = "AdSkipper",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        // Version Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "v$appVersion",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Trust Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F8E9) // Very light green
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 24.sp)
                    Text(
                        text = "100% Privacy Focused",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF33691E)
                    )
                }
                
                Text(
                    text = "No login • No ads • No tracking • Offline",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF558B2F)
                )

                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PrivacyFeature("🔒", "Secure")
                    PrivacyFeature("⚡", "Fast")
                    PrivacyFeature("📖", "Open")
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
                iconContent = { 
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = {
                    uriHandler.openUri("https://github.com/jaswanthsatyadev/AdSkipper")
                }
            )
        }

        // Email Contact
        InfoItemCard(
            icon = Icons.Filled.Info,
            title = "Contact",
            value = "adskipper@evolvarc.com",
            iconBgColor = Color(0xFFEF4444),
            onClick = {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("adskipper@evolvarc.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "AdSkipper Feedback")
                }
                context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
            }
        )

        // Website Link
        InfoItemCard(
            icon = Icons.Filled.Star,
            title = "Website",
            value = "adskipper.evolvarc.com",
            iconBgColor = Color(0xFF6366F1),
            showArrow = true,
            onClick = {
                uriHandler.openUri("https://adskipper.evolvarc.com")
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
                title = "Please give us a 5 star rating",
                value = "on Play Store",
                iconBgColor = Color(0xFFFCD34D),
                showArrow = true,
                onClick = {
                    openPlayStore(context)
                }
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

        // Donation Section
        DonationCard(
            onClick = { showDonationDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy Policy Link
        InfoItemCard(
            icon = Icons.Filled.Info,
            title = "Privacy Policy",
            value = "View our privacy policy",
            iconBgColor = Color(0xFF10B981),
            showArrow = true,
            onClick = {
                uriHandler.openUri("https://adskipper.evolvarc.com/privacy")
            }
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
    
    // Donation Dialog
    if (showDonationDialog) {
        DonationDialog(
            onDismiss = { showDonationDialog = false },
            onIndiaClick = {
                showDonationDialog = false
                uriHandler.openUri("https://razorpay.me/@jaswanthsatyadev")
            },
            onOtherCountriesClick = {
                showDonationDialog = false
                uriHandler.openUri("https://buymeacoffee.com/jaswanthsatyadev")
            }
        )
    }
}

@Composable
fun DonationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFFFF8E53)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Heart Icon with pulsing effect
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "☕",
                        fontSize = 32.sp
                    )
                }

                Text(
                    text = "Support the Developer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "AdSkipper is free forever! Your support helps keep it ad-free and frequently updated.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buy Me a Coffee",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonationDialog(
    onDismiss: () -> Unit,
    onIndiaClick: () -> Unit,
    onOtherCountriesClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "☕",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose Your Region",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF4E8)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Your support keeps AdSkipper free for everyone.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            color = Color(0xFFD65A31)
                        )
                        Text(
                            text = "Every contribution helps us ship faster fixes, better updates, and keep the app simple and ad-free.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Choose the payment option that works best for you:",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // India Option
                Button(
                    onClick = onIndiaClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F9D58)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🇮🇳",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "India (Razorpay)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "UPI, Cards, NetBanking",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                // Other Countries Option
                Button(
                    onClick = onOtherCountriesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFDD00)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🌍",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Other Countries",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
                            )
                            Text(
                                text = "Buy Me a Coffee (PayPal, Cards)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Maybe Later")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
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

// Helper function to open Play Store
private fun openPlayStore(context: android.content.Context) {
    try {
        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("market://details?id=com.evolvarc.adskipper")
            setPackage("com.android.vending")
        }
        context.startActivity(playStoreIntent)
    } catch (e: Exception) {
        // Fallback to browser if Play Store app is not available
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.evolvarc.adskipper")
        }
        context.startActivity(browserIntent)
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AdSkipperTheme {
        AboutScreen()
    }
}
