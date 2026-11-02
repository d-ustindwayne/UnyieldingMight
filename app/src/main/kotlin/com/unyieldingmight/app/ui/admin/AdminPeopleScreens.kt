package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.ActivityLevelPicker
import com.unyieldingmight.app.ui.common.AddressFields
import com.unyieldingmight.app.ui.common.AddressInput
import com.unyieldingmight.app.ui.common.BodyFields
import com.unyieldingmight.app.ui.common.BodyInput
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.DateField
import com.unyieldingmight.app.ui.common.DropdownField
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.PasswordField
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.SegmentedChoice
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.label
import com.unyieldingmight.app.ui.common.rememberLeaveGuard
import com.unyieldingmight.app.ui.navigation.AdminUserEditRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Success
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AdminCreateUserRequest
import com.unyieldingmight.shared.model.AdminUpdateUserRequest
import com.unyieldingmight.shared.model.AdminUserDto
import com.unyieldingmight.shared.model.Gender
import com.unyieldingmight.shared.model.NewsletterRequest
import com.unyieldingmight.shared.model.NewsletterType
import com.unyieldingmight.shared.model.Role
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// ---------- Members ----------

class AdminUsersViewModel(container: AppContainer) : LoadViewModel<List<AdminUserDto>>(container) {
    var query by mutableStateOf("")
        private set
    var showDeactivated by mutableStateOf(false)
        private set
    private var debounce: Job? = null

    override suspend fun load() = container.api.adminUsers(query.ifBlank { null }, deactivated = showDeactivated)

    fun search(text: String) {
        query = text
        debounce?.cancel()
        debounce = viewModelScope.launch { delay(300); refresh() }
    }

    fun showDeactivated(show: Boolean) {
        if (show == showDeactivated) return
        showDeactivated = show
        refresh()
    }

    fun setRole(user: AdminUserDto, role: Role) = act(block = { container.api.setRole(user.id, role) }) {
        container.messenger.success("Role updated", "${user.firstName} is now ${if (role == Role.ADMIN) "an admin" else "a customer"}.")
        refresh()
    }

    fun setActive(user: AdminUserDto, active: Boolean) = act(block = { container.api.setActive(user.id, active) }) {
        container.messenger.success(if (active) "${user.firstName}'s account reactivated" else "${user.firstName}'s account deactivated", it.message)
        refresh()
    }
}

/** A confirmation for a role or account change: title, explanation, confirm label and the action. */
private data class PendingChange(val title: String, val text: String, val confirm: String, val action: () -> Unit)

@Composable
fun AdminUsersScreen(me: UserDto) {
    val vm = containerViewModel { AdminUsersViewModel(it) }
    val state by vm.state.collectAsState()
    val shell = LocalShell.current
    var pending by remember { mutableStateOf<PendingChange?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        "Members",
        topLevel = true,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { shell.nav.navigate(AdminUserEditRoute()) },
                icon = { Icon(Icons.Rounded.PersonAdd, null) },
                text = { Text("Add member") },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Centered(800.dp) {
                Column(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
                    UmTextField(vm.query, vm::search, "Search by name or email", leadingIcon = Icons.Rounded.Search)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!vm.showDeactivated, onClick = { vm.showDeactivated(false) }, label = { Text("Active") })
                        FilterChip(vm.showDeactivated, onClick = { vm.showDeactivated(true) }, label = { Text("Deactivated") })
                    }
                }
            }
            LoadContent(state, vm::refresh) { users ->
                Centered(800.dp) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (users.isEmpty()) {
                            item {
                                EmptyState(
                                    Icons.Rounded.Group,
                                    if (vm.showDeactivated) "No deactivated accounts" else "No members found",
                                    if (vm.query.isBlank()) "Accounts will appear here." else "Try a different name or email.",
                                )
                            }
                        }
                        items(users, key = { it.id }) { u ->
                            MemberCard(
                                u, isMe = u.id == me.id,
                                onEdit = { shell.nav.navigate(AdminUserEditRoute(u.id)) },
                                onRole = {
                                    val newRole = if (u.role == Role.ADMIN) Role.CUSTOMER else Role.ADMIN
                                    pending = if (newRole == Role.ADMIN) {
                                        PendingChange("Give admin access?", "${u.firstName} will be able to manage classes, the shop and other members.", "Make admin") { vm.setRole(u, newRole) }
                                    } else {
                                        PendingChange("Remove admin access?", "${u.firstName} will lose access to the admin tools immediately.", "Remove admin") { vm.setRole(u, newRole) }
                                    }
                                },
                                onActive = {
                                    pending = if (u.active) {
                                        PendingChange(
                                            "Deactivate ${u.firstName}'s account?",
                                            "They're signed out straight away and can't log in. Their upcoming class bookings are cancelled and refunded in full; past bookings, payments and orders are kept. You can reactivate the account at any time.",
                                            "Deactivate",
                                        ) { vm.setActive(u, false) }
                                    } else {
                                        PendingChange("Reactivate ${u.firstName}'s account?", "They'll be able to log in again.", "Reactivate") { vm.setActive(u, true) }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    pending?.let { change ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(change.title) },
            text = { Text(change.text) },
            confirmButton = { TextButton(onClick = { change.action(); pending = null }) { Text(change.confirm) } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } },
        )
    }
}

/** Top to bottom: name and role, email, join date, then the actions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberCard(u: AdminUserDto, isMe: Boolean, onEdit: () -> Unit, onRole: () -> Unit, onActive: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                Text("${u.firstName} ${u.lastName}" + if (isMe) " (you)" else "", style = MaterialTheme.typography.titleMedium)
                when {
                    u.role == Role.ADMIN -> Pill("Admin", MaterialTheme.colorScheme.primary)
                    u.isMember -> Pill("Member", Success)
                    else -> Pill("Customer", MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!u.active) Pill("Deactivated", MaterialTheme.colorScheme.error)
            }
            Text(u.email, style = MaterialTheme.typography.bodyMedium)
            Text("Joined ${Format.fullDate(u.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MemberAction("Edit info", Icons.Rounded.Edit, onEdit)
                if (!isMe && u.active) {
                    MemberAction(if (u.role == Role.ADMIN) "Remove admin" else "Make admin", Icons.Rounded.AdminPanelSettings, onRole)
                }
                if (!isMe) {
                    MemberAction(if (u.active) "Deactivate" else "Reactivate", if (u.active) Icons.Rounded.PersonOff else Icons.Rounded.Restore, onActive, destructive = u.active)
                }
            }
        }
    }
}

@Composable
private fun MemberAction(label: String, icon: ImageVector, onClick: () -> Unit, destructive: Boolean = false) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        colors = if (destructive) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonColors(),
    ) {
        Icon(icon, null, Modifier.padding(end = 6.dp))
        Text(label)
    }
}

// ---------- Add / edit a member ----------

class AdminUserEditViewModel(container: AppContainer, private val id: Int) : LoadViewModel<UserDto?>(container) {
    val saving = MutableStateFlow(false)

    override suspend fun load(): UserDto? = if (id > 0) container.api.adminUser(id) else null

    init { refresh() }

    fun create(request: AdminCreateUserRequest, onDone: () -> Unit) = act(saving, { container.api.createUser(request) }) { user ->
        container.messenger.success(
            "Account created",
            if (request.password != null) "Give ${user.firstName} their temporary password. They can change it in Settings → Change password."
            else "We've emailed ${user.email} with how to set their password.",
        )
        onDone()
    }

    fun update(request: AdminUpdateUserRequest, onDone: () -> Unit) = act(saving, { container.api.updateUser(id, request) }) { user ->
        // An admin editing their own account: keep the sidebar and profile in step.
        if (user.id == container.session.currentUser?.id) container.session.updateUser(user)
        container.messenger.success("Details saved", "${user.fullName}'s account has been updated.")
        onDone()
    }
}

/** How a new member gets their first password. */
private enum class PasswordSetup(val label: String) { TEMPORARY("Set a temporary one"), EMAIL("Email them a code") }

/** Form state for [AdminUserEditScreen], created once the account (if any) has loaded. */
private class MemberForm(user: UserDto?) {
    var firstName by mutableStateOf(user?.firstName.orEmpty())
    var lastName by mutableStateOf(user?.lastName.orEmpty())
    var email by mutableStateOf(user?.email.orEmpty())
    var dateOfBirth by mutableStateOf(user?.dateOfBirth.orEmpty())
    var gender by mutableStateOf(user?.gender ?: Gender.OTHER)
    val address = AddressInput(user?.address)
    val body = BodyInput(user?.stats?.heightCm, user?.stats?.weightKg)
    var activity by mutableStateOf(user?.stats?.activityLevel ?: ActivityLevel.MODERATE)
    var passwordSetup by mutableStateOf(PasswordSetup.TEMPORARY)
    var password by mutableStateOf("")
    var showErrors by mutableStateOf(false)

    /** Everything the admin can change, for detecting unsaved edits. */
    fun snapshot(): List<Any?> = listOf(
        firstName, lastName, email, dateOfBirth, gender, address.toDto(), body.heightInCm(), body.weightInKg(), activity, password,
    )
}

@Composable
fun AdminUserEditScreen(id: Int) {
    val vm = containerViewModel(key = "edit-user-$id") { AdminUserEditViewModel(it, id) }
    val state by vm.state.collectAsState()
    val saving by vm.saving.collectAsState()
    val shell = LocalShell.current
    var dirty by remember { mutableStateOf(false) }
    val leave = rememberLeaveGuard(dirty) { shell.nav.navigateUp() }

    ScreenScaffold(if (id > 0) "Edit member" else "Add member", onBack = leave) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { user ->
            val form = remember(user) { MemberForm(user) }
            val initial = remember(user) { form.snapshot() }
            SideEffect { dirty = form.snapshot() != initial }
            val creating = user == null
            // Admin accounts have no body stats, so there's nothing to edit there.
            val hasStats = creating || user.stats != null
            val errors = mapOf(
                "first" to Validation.name(form.firstName, "First name"),
                "last" to Validation.name(form.lastName, "Last name"),
                "email" to Validation.email(form.email),
                "dob" to if (form.dateOfBirth.isBlank()) "Choose a date of birth" else null,
                "password" to if (creating && form.passwordSetup == PasswordSetup.TEMPORARY) Validation.password(form.password) else null,
            )
            val shown = if (form.showErrors) errors else emptyMap()

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Centered(640.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (creating) {
                            Text(
                                "Create an account for someone at the front desk. They can log in straight away.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        SectionHeader("Personal details")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            UmTextField(form.firstName, { form.firstName = it }, "First name", Modifier.weight(1f), error = shown["first"])
                            UmTextField(form.lastName, { form.lastName = it }, "Last name", Modifier.weight(1f), error = shown["last"])
                        }
                        UmTextField(form.email, { form.email = it.trim() }, "Email", error = shown["email"], keyboardType = KeyboardType.Email)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DateField("Date of birth", form.dateOfBirth, { form.dateOfBirth = it }, error = shown["dob"], modifier = Modifier.weight(1f))
                            DropdownField("Gender", Gender.entries, form.gender, { form.gender = it }, { it.label() }, Modifier.weight(1f))
                        }
                        SectionHeader("Address", modifier = Modifier.padding(top = 8.dp))
                        AddressFields(form.address)
                        if (hasStats) {
                            SectionHeader("Body stats", modifier = Modifier.padding(top = 8.dp), overline = "Used for calorie targets & recommendations")
                            BodyFields(form.body)
                            ActivityLevelPicker(form.activity) { form.activity = it }
                        }
                        if (creating) {
                            SectionHeader("Password", modifier = Modifier.padding(top = 8.dp))
                            SegmentedChoice(PasswordSetup.entries, form.passwordSetup, { form.passwordSetup = it }, { it.label })
                            if (form.passwordSetup == PasswordSetup.TEMPORARY) {
                                PasswordField(form.password, { form.password = it }, "Temporary password", error = shown["password"])
                                Text(
                                    "Tell them this password. They can change it later in Settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    "We'll email them how to set their own password: in the app, they tap \"Forgot your password?\" and get a code.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        PrimaryButton(
                            if (creating) "Create account" else "Save changes",
                            onClick = {
                                form.showErrors = true
                                form.address.showErrors = true
                                form.body.showErrors = true
                                val valid = errors.values.all { it == null } && form.address.isValid() && (!hasStats || form.body.isValid())
                                if (!valid) return@PrimaryButton
                                if (creating) {
                                    vm.create(
                                        AdminCreateUserRequest(
                                            email = form.email,
                                            firstName = form.firstName.trim(),
                                            lastName = form.lastName.trim(),
                                            dateOfBirth = form.dateOfBirth,
                                            gender = form.gender,
                                            address = form.address.toDto(),
                                            heightCm = form.body.heightInCm()!!,
                                            weightKg = form.body.weightInKg()!!,
                                            activityLevel = form.activity,
                                            password = form.password.takeIf { form.passwordSetup == PasswordSetup.TEMPORARY },
                                        )
                                    ) { shell.nav.navigateUp() }
                                } else {
                                    vm.update(
                                        AdminUpdateUserRequest(
                                            email = form.email,
                                            firstName = form.firstName.trim(),
                                            lastName = form.lastName.trim(),
                                            dateOfBirth = form.dateOfBirth,
                                            gender = form.gender,
                                            address = form.address.toDto(),
                                            heightCm = if (hasStats) form.body.heightInCm() else null,
                                            weightKg = if (hasStats) form.body.weightInKg() else null,
                                            activityLevel = if (hasStats) form.activity else null,
                                        )
                                    ) { shell.nav.navigateUp() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            loading = saving,
                        )
                    }
                }
            }
        }
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
    val shell = LocalShell.current
    val leave = rememberLeaveGuard(vm.message.isNotBlank() || vm.bullets.any { it.isNotBlank() }) { shell.nav.navigateUp() }

    ScreenScaffold("Newsletter", onBack = leave) { padding ->
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
