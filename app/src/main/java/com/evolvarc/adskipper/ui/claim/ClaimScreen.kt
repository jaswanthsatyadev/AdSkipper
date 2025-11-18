package com.evolvarc.adskipper.ui.claim

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evolvarc.adskipper.R
import kotlinx.coroutines.delay

@Composable
fun ClaimScreen(
    onClaimComplete: () -> Unit
) {
    var showInitial by remember { mutableStateOf(true) }
    var showClaim by remember { mutableStateOf(false) }
    var showWhirlpool by remember { mutableStateOf(false) }
    var showCheck by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var claimed by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (claimed) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(500)
        showClaim = true
    }

    LaunchedEffect(claimed) {
        if (claimed) {
            showWhirlpool = true
            delay(1400)
            showCheck = true
            delay(900)
            showSuccess = true
            delay(1400)
            onClaimComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF1565C0),
                        Color(0xFF0D47A1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = showWhirlpool,
            enter = fadeIn(tween(400)) + scaleIn(tween(400)),
            exit = fadeOut(tween(300))
        ) {
            WhirlpoolEffect(
                modifier = Modifier.size(280.dp)
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = showCheck,
            enter = fadeIn(tween(500)) + scaleIn(tween(500)),
            exit = fadeOut(tween(300))
        ) {
            SuccessCheckmark(
                modifier = Modifier.size(140.dp),
                isVisible = showCheck
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo with bounce
            AnimatedVisibility(
                visible = showInitial && !showWhirlpool,
                enter = fadeIn(tween(600)) + scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                // Use AppLogo which will prefer attached `adskipper_logo` drawable if available
                com.evolvarc.adskipper.ui.common.AppLogo(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale),
                    size = 140.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title with shimmer
            AnimatedVisibility(
                visible = showInitial && !showWhirlpool,
                enter = fadeIn(tween(600, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { -50 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
            ) {
                Text(
                    text = if (showSuccess) "Welcome Aboard! 🎉" else "Claim Your AdSkipper",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 36.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            AnimatedVisibility(
                visible = showInitial && !showSuccess && !showWhirlpool,
                enter = fadeIn(tween(600, delayMillis = 400))
            ) {
                Text(
                    text = "Free Forever • No Ads • No Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Claim button with shimmer effect
            AnimatedVisibility(
                visible = showClaim && !claimed,
                enter = fadeIn(tween(600, delayMillis = 600)) +
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
            ) {
                Button(
                    onClick = { claimed = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(32.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎁 CLAIM FREE ACCESS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.graphicsLayer { alpha = shimmerAlpha },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Success message
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn(tween(600)) + scaleIn(tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Access Activated",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "Welcome to the AdSkipper club",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Features list
            if (!claimed && showClaim) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(600, delayMillis = 800)) +
                            slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        FeatureBadge("⚡", "Skip ads in milliseconds")
                        FeatureBadge("🔒", "100% Private & Secure")
                        FeatureBadge("💝", "No hidden costs, ever")
                        FeatureBadge("📱", "Works offline")
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureBadge(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WhirlpoolEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "whirlpool")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = pulse
            scaleY = pulse
        }
    ) {
        val arcColors = listOf(
            Color(0xFFBBDEFB),
            Color(0xFF82B1FF),
            Color.White
        )
        val maxInset = size.minDimension * 0.35f
        val steps = 5
        repeat(steps) { index ->
            val inset = (index / steps.toFloat()) * maxInset
            val strokeWidth = 18f - index * 2.5f
            drawArc(
                brush = Brush.sweepGradient(arcColors),
                startAngle = index * 22f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(
                    width = size.width - inset * 2,
                    height = size.height - inset * 2
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                alpha = 0.85f - index * 0.12f
            )
        }
    }
}

@Composable
fun SuccessCheckmark(modifier: Modifier = Modifier, isVisible: Boolean) {
    val progress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "checkProgress"
    )

    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.55f)
            lineTo(size.width * 0.45f, size.height * 0.78f)
            lineTo(size.width * 0.82f, size.height * 0.28f)
        }

        val measure = PathMeasure()
        measure.setPath(path, false)
        val segment = Path()
        measure.getSegment(0f, measure.length * progress, segment, true)

        drawPath(
            path = segment,
            color = Color.White,
            style = Stroke(width = 14f, cap = StrokeCap.Round)
        )
    }
}
