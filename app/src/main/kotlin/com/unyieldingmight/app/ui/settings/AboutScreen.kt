package com.unyieldingmight.app.ui.settings

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.BuildConfig
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.UmLogo
import com.unyieldingmight.app.ui.shell.ScreenScaffold

private data class Credit(val name: String, val role: String, val github: String)

private val credits = listOf(
    Credit("Dustin Dwayne Diaz", "Backend (original) · Kotlin, Ktor & Compose rewrite", "d-ustindwayne"),
    Credit("SaplingBeing", "Frontend & technical documentation (original)", "SaplingBeing"),
    Credit("darawesome", "Frontend & technical documentation (original)", "darawesome"),
)

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    ScreenScaffold("About") { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Centered(640.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UmLogo(Modifier.size(width = 140.dp, height = 84.dp))
                    Text("Unyielding Might", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "A gym booking app that removes the friction from booking expert-led classes: free for members, pay-as-you-go for everyone else.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    SectionHeader("Credits", overline = "The team")
                    Text(
                        "Unyielding Might started as a group project. This version is a ground-up rewrite that builds on the original team's design, flows and class content.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Column {
                            credits.forEach { credit ->
                                Surface(onClick = { open(context, "https://github.com/${credit.github}") }, color = MaterialTheme.colorScheme.surfaceContainer) {
                                    ListItem(
                                        headlineContent = { Text(credit.name) },
                                        supportingContent = { Text(credit.role) },
                                        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Open GitHub profile") },
                                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    )
                                }
                            }
                        }
                    }

                    SectionHeader("Built with", overline = "Tech")
                    Text(
                        "Kotlin · Jetpack Compose & Material 3 · Ktor (server & client) · MySQL · Stripe · NewsAPI",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private fun open(context: android.content.Context, url: String) {
    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
}
