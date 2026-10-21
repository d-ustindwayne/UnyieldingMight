package com.unyieldingmight.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.R
import com.unyieldingmight.app.ui.common.UmLogo
import com.unyieldingmight.app.ui.theme.Ember
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.UmBlack

/**
 * Frame for login/registration. Phones get a single scrolling column with the logo on top;
 * wider screens get a branded photo panel beside the form.
 */
@Composable
fun AuthLayout(
    windowSize: WindowSizeClass,
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val wide = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
    Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (wide) BrandPanel(Modifier.weight(0.9f).fillMaxHeight())
        Box(Modifier.weight(1f).fillMaxHeight().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(48.dp)) {
                    onBack?.let {
                        IconButton(onClick = it, modifier = Modifier.align(Alignment.CenterStart)) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                        }
                    }
                }
                if (!wide) {
                    UmLogo(Modifier.size(width = 120.dp, height = 72.dp).align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(16.dp))
                } else {
                    Spacer(Modifier.height(48.dp))
                }
                Text(title, style = MaterialTheme.typography.headlineMedium)
                subtitle?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BrandPanel(modifier: Modifier) {
    Box(modifier.background(UmBlack)) {
        Image(
            painterResource(R.drawable.boxfit),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.45f),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, UmBlack.copy(alpha = 0.95f)))
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(40.dp)) {
            Image(
                painterResource(R.drawable.unyielding_might_logo), "Unyielding Might logo",
                colorFilter = ColorFilter.tint(Color.White), modifier = Modifier.size(width = 140.dp, height = 84.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text("UNYIELDING MIGHT", style = Overline, color = Ember)
            Spacer(Modifier.height(8.dp))
            Text(
                "Expert-led classes. Book in seconds.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Free for members, pay-as-you-go for everyone else.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
