package com.unyieldingmight.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.data.UnitSystem
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.PasswordField
import com.unyieldingmight.app.ui.common.SegmentedChoice
import com.unyieldingmight.app.ui.navigation.AboutRoute
import com.unyieldingmight.app.ui.navigation.MembershipRoute
import com.unyieldingmight.app.ui.navigation.ProfileRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.ThemeMode
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.UpdateProfileRequest
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(user: UserDto) {
    val container = LocalContainer.current
    val shell = LocalShell.current
    val scope = rememberCoroutineScope()
    val theme by container.preferences.theme.collectAsState()
    val units by container.preferences.units.collectAsState()
    var changingPassword by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold("Settings", topLevel = true) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Centered(640.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Group("Appearance") {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Theme", style = MaterialTheme.typography.labelLarge)
                            SegmentedChoice(ThemeMode.entries, theme, { scope.launch { container.preferences.setTheme(it) } }, {
                                when (it) { ThemeMode.SYSTEM -> "System"; ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark" }
                            })
                            Spacer(Modifier.height(8.dp))
                            Text("Units", style = MaterialTheme.typography.labelLarge)
                            SegmentedChoice(UnitSystem.entries, units, { scope.launch { container.preferences.setUnits(it) } }, {
                                if (it == UnitSystem.METRIC) "Metric (kg, cm)" else "Imperial (lb, ft)"
                            })
                        }
                    }
                    Group("Account") {
                        if (user.isAdmin) {
                            SettingsRow(Icons.Rounded.Person, "Profile", "Name and address") { shell.nav.navigate(ProfileRoute) }
                        } else {
                            SettingsRow(Icons.Rounded.Person, "Profile & body stats", "Name, address, height, weight") { shell.nav.navigate(ProfileRoute) }
                            SettingsRow(Icons.Rounded.CardMembership, "Membership", if (user.isMember) "Active" else "Link or buy a membership") { shell.nav.navigate(MembershipRoute) }
                        }
                        SettingsRow(Icons.Rounded.Lock, "Change password", null) { changingPassword = true }
                    }
                    Group("Notifications") {
                        ListItem(
                            headlineContent = { Text("Email newsletter") },
                            supportingContent = { Text("Tips, offers and gym updates. Booking emails are always sent.") },
                            leadingContent = { Icon(Icons.Rounded.Mail, null) },
                            trailingContent = {
                                Switch(checked = user.newsletterOptIn, onCheckedChange = { optIn ->
                                    scope.launch {
                                        runCatching { container.api.updateProfile(UpdateProfileRequest(newsletterOptIn = optIn)) }
                                            .onSuccess { container.session.updateUser(it) }
                                            .onFailure { container.messenger.error(it) }
                                    }
                                })
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        )
                    }
                    Group("About") {
                        SettingsRow(Icons.Rounded.Info, "About Unyielding Might", "Credits, version and tech") { shell.nav.navigate(AboutRoute) }
                        SettingsRow(Icons.AutoMirrored.Rounded.Logout, "Log out", user.email) {
                            scope.launch {
                                container.cart.clear()
                                container.session.signOut()
                            }
                        }
                    }
                }
            }
        }
    }

    if (changingPassword) ChangePasswordDialog { changingPassword = false }
}

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surfaceContainer) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = subtitle?.let { { Text(it) } },
            leadingContent = { Icon(icon, null) },
            trailingContent = { Icon(Icons.AutoMirrored.Rounded.ArrowForward, null) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit) {
    val container = LocalContainer.current
    val scope = rememberCoroutineScope()
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    fun save() {
        showErrors = true
        if (current.isEmpty() || Validation.password(new) != null) return
        scope.launch {
            saving = true
            try {
                container.messenger.success("Password changed", container.api.changePassword(current, new).message)
                onDismiss()
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                saving = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PasswordField(current, { current = it }, "Current password", error = if (showErrors && current.isEmpty()) "Enter your current password" else null)
                PasswordField(new, { new = it }, "New password", error = if (showErrors) Validation.password(new) else null, imeAction = ImeAction.Done, onDone = ::save)
            }
        },
        confirmButton = { TextButton(onClick = ::save, enabled = !saving) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
