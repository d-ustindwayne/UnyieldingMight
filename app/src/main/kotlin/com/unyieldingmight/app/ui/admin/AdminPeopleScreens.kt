package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.DropdownField
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Success
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.NewsletterType
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// ---------- Users & roles ----------

class AdminUsersViewModel(container: AppContainer) : LoadViewModel<List<AdminUserDto>>(container) {
    var query by mutableStateOf("")
        private set
    private var debounce: Job? = null

    override suspend fun load() = container.api.adminUsers(query.ifBlank { null })

    fun search(text: String) {
        query = text
        debounce?.cancel()
        debounce = viewModelScope.launch { delay(300); refresh() }
    }

    fun setRole(user: AdminUserDto, role: Role) = act(block = { container.api.setRole(user.id, role) }) {
        container.messenger.success("Role updated", "${user.firstName} is now ${if (role == Role.ADMIN) "an admin" else "a customer"}.")
        refresh()
    }
}

@Composable
fun AdminUsersScreen(me: UserDto) {
    val vm = containerViewModel { AdminUsersViewModel(it) }
    val state by vm.state.collectAsState()
    var changing by remember { mutableStateOf<AdminUserDto?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold("Members & roles") { padding ->
        Column(Modifier.padding(padding)) {
            Centered(800.dp) {
                UmTextField(vm.query, vm::search, "Search by name or email", Modifier.padding(16.dp), leadingIcon = Icons.Rounded.Search)
            }
            LoadContent(state, vm::refresh) { users ->
                Centered(800.dp) {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(users, key = { it.id }) { u ->
                            ListItem(
                                headlineContent = { Text("${u.firstName} ${u.lastName}") },
                                supportingContent = { Text("${u.email} · joined ${Format.fullDate(u.createdAt)}") },
                                trailingContent = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (u.isMember) Pill("Member", Success)
                                        if (u.role == Role.ADMIN) Pill("Admin", MaterialTheme.colorScheme.primary)
                                        if (u.id != me.id) {
                                            TextButton(onClick = { changing = u }) { Text(if (u.role == Role.ADMIN) "Demote" else "Make admin") }
                                        }
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    changing?.let { u ->
        val newRole = if (u.role == Role.ADMIN) Role.CUSTOMER else Role.ADMIN
        AlertDialog(
            onDismissRequest = { changing = null },
            title = { Text(if (newRole == Role.ADMIN) "Give admin access?" else "Remove admin access?") },
            text = {
                Text(
                    if (newRole == Role.ADMIN) "${u.firstName} will be able to manage classes, the shop and other members."
                    else "${u.firstName} will lose access to the admin dashboard immediately."
                )
            },
            confirmButton = { TextButton(onClick = { vm.setRole(u, newRole); changing = null }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { changing = null }) { Text("Cancel") } },
        )
    }
}

// ---------- Newsletter ----------

class AdminNewsletterViewModel(private val container: AppContainer) : ViewModel() {
    var type by mutableStateOf(NewsletterType.UPDATES)
    var message by mutableStateOf("")
    val bullets = mutableStateListOf<String>()
    val sending = MutableStateFlow(false)

    fun send() {
        viewModelScope.launch {
            sending.value = true
            try {
                val result = container.api.sendNewsletter(NewsletterRequest(type, message.trim(), bullets.filter { it.isNotBlank() }))
                container.messenger.success("Newsletter sent", "Queued for ${result.recipients} opted-in member(s).")
                message = ""
                bullets.clear()
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                sending.value = false
            }
        }
    }
}

private val newsletterTypes = listOf(NewsletterType.UPDATES, NewsletterType.INFORMATIONAL, NewsletterType.PROMOTIONAL)

@Composable
fun AdminNewsletterScreen() {
    val vm = containerViewModel { AdminNewsletterViewModel(it) }
    val sending by vm.sending.collectAsState()
    var confirming by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold("Newsletter") { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Centered(640.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sent to every member who has the newsletter switched on in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DropdownField("Type", newsletterTypes, vm.type, { vm.type = it }, {
                        when (it) {
                            NewsletterType.UPDATES -> "Updates: \"New Update Alert!\""
                            NewsletterType.INFORMATIONAL -> "Informational: \"Unyielding Insight\""
                            else -> "Promotional: \"Unlock Your Exclusive Discount\""
                        }
                    })
                    UmTextField(vm.message, { vm.message = it }, "Message", singleLine = false, minLines = 5)
                    Text("Bullet points", style = MaterialTheme.typography.labelLarge)
                    vm.bullets.forEachIndexed { i, b ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UmTextField(b, { vm.bullets[i] = it }, "Point ${i + 1}", Modifier.weight(1f))
                            IconButton(onClick = { vm.bullets.removeAt(i) }) { Icon(Icons.Rounded.Close, "Remove point") }
                        }
                    }
                    TextButton(onClick = { vm.bullets.add("") }) { Icon(Icons.Rounded.Add, null); Text("Add bullet point") }
                    PrimaryButton(
                        "Send newsletter", { confirming = true }, Modifier.fillMaxWidth(),
                        enabled = vm.message.isNotBlank(), loading = sending, icon = Icons.AutoMirrored.Rounded.Send,
                    )
                }
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Send to all opted-in members?") },
            text = { Text("Emails go out straight away and can't be recalled.") },
            confirmButton = { TextButton(onClick = { vm.send(); confirming = false }) { Text("Send") } },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } },
        )
    }
}
