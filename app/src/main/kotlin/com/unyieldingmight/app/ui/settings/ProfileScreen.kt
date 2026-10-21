package com.unyieldingmight.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.label
import com.unyieldingmight.app.ui.shell.Avatar
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.UpdateProfileRequest
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val container: AppContainer, user: UserDto) : ViewModel() {
    var firstName by mutableStateOf(user.firstName)
    var lastName by mutableStateOf(user.lastName)
    val address = AddressInput(user.address)
    val body = BodyInput(user.stats?.heightCm, user.stats?.weightKg, container.preferences.units.value)
    var activity by mutableStateOf(user.stats?.activityLevel ?: ActivityLevel.MODERATE)
    var showErrors by mutableStateOf(false)
    val saving = MutableStateFlow(false)
    private val hasStats = user.stats != null

    fun save(onSaved: () -> Unit) {
        showErrors = true
        address.showErrors = true
        body.showErrors = true
        val valid = Validation.name(firstName) == null && Validation.name(lastName) == null && address.isValid() && (!hasStats || body.isValid())
        if (!valid) return
        viewModelScope.launch {
            saving.value = true
            try {
                val updated = container.api.updateProfile(
                    UpdateProfileRequest(
                        firstName = firstName,
                        lastName = lastName,
                        address = address.toDto(),
                        heightCm = if (hasStats) body.heightInCm() else null,
                        weightKg = if (hasStats) body.weightInKg() else null,
                        activityLevel = if (hasStats) activity else null,
                    )
                )
                container.session.updateUser(updated)
                container.messenger.success("Profile saved", "Your details and calorie targets have been updated.")
                onSaved()
            } catch (e: Exception) {
                container.messenger.error(e)
            } finally {
                saving.value = false
            }
        }
    }
}

@Composable
fun ProfileScreen(user: UserDto) {
    val vm = containerViewModel { ProfileViewModel(it, user) }
    val saving by vm.saving.collectAsState()
    val shell = LocalShell.current

    ScreenScaffold("My profile") { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Centered(640.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Avatar(user, 72)
                        Column {
                            Text(user.fullName, style = MaterialTheme.typography.titleLarge)
                            Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Born ${user.dateOfBirth} · ${user.gender.label()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    SectionHeader("Personal details")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        UmTextField(vm.firstName, { vm.firstName = it }, "First name", Modifier.weight(1f), error = if (vm.showErrors) Validation.name(vm.firstName) else null)
                        UmTextField(vm.lastName, { vm.lastName = it }, "Last name", Modifier.weight(1f), error = if (vm.showErrors) Validation.name(vm.lastName) else null)
                    }
                    AddressFields(vm.address)
                    if (user.stats != null) {
                        SectionHeader("Body stats", overline = "Used for TDEE & recommendations")
                        BodyFields(vm.body)
                        SectionHeader("Activity level")
                        ActivityLevelPicker(vm.activity) { vm.activity = it }
                    }
                    PrimaryButton("Save changes", { vm.save { shell.nav.navigateUp() } }, Modifier.fillMaxWidth(), loading = saving)
                }
            }
        }
    }
}
