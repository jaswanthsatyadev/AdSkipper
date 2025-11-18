package com.evolvarc.adskipper.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.evolvarc.adskipper.R

/**
 * Loads `R.drawable.adskipper_logo` if present in the app resources (this is the image you attached),
 * otherwise falls back to the existing launcher foreground drawable (`R.drawable.ic_launcher_foreground`).
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    contentDescription: String = "App Logo",
    cornerRadius: Dp = 12.dp
) {
    val context = LocalContext.current
    // Check if an attached drawable named `adskipper_logo` exists
    val resId = remember { context.resources.getIdentifier("adskipper_logo", "drawable", context.packageName) }
    val painterRes = if (resId != 0) resId else R.drawable.ic_launcher_foreground

    Image(
        painter = painterResource(id = painterRes),
        contentDescription = contentDescription,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(Modifier.size(size)),
        contentScale = ContentScale.Crop
    )
}
