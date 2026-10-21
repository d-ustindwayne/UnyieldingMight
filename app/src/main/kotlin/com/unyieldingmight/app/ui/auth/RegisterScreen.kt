package com.unyieldingmight.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.ui.auth.RegisterViewModel.Step
import com.unyieldingmight.app.ui.common.ActivityLevelPicker
import com.unyieldingmight.app.ui.common.AddressFields
import com.unyieldingmight.app.ui.common.BodyFields
import com.unyieldingmight.app.ui.common.DateField
import com.unyieldingmight.app.ui.common.PasswordField
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SecondaryButton
import com.unyieldingmight.app.ui.common.SegmentedChoice
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.digitsOnly
import com.unyieldingmight.app.ui.common.label
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.domain.Validation
import com.unyieldingmight.shared.model.Gender
import java.time.LocalDate
import java.time.Period

@Composable
fun RegisterScreen(windowSize: WindowSizeClass, onExit: () -> Unit) {
    val vm = containerViewModel { RegisterViewModel(it) }
    val loading by vm.loading.collectAsState()
    val back = { if (!vm.back()) onExit() }
    BackHandler(onBack = back)

    val title = if (vm.step == Step.DONE && vm.membership != null) "Membership linked!" else vm.step.title
    AuthLayout(windowSize, title, vm.step.subtitle, onBack = back) {
        Text("STEP ${vm.stepNumber} OF ${vm.totalSteps}", style = Overline, color = MaterialTheme.colorScheme.primary)
        LinearProgressIndicator(
            progress = { vm.stepNumber / vm.totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        when (vm.step) {
            Step.ACCOUNT -> AccountStep(vm)
            Step.VERIFY -> VerifyStep(vm, loading)
            Step.MEMBER -> MemberStep(vm)
            Step.LINK -> LinkStep(vm)
            Step.DETAILS -> DetailsStep(vm)
            Step.STATS -> BodyFields(vm.body)
            Step.ACTIVITY -> ActivityLevelPicker(vm.activityLevel) { vm.activityLevel = it }
            Step.DONE -> DoneStep(vm)
        }
        if (vm.step != Step.MEMBER) {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = when (vm.step) {
                    Step.ACCOUNT -> "Register"
                    Step.VERIFY -> "Confirm"
                    Step.LINK -> "Link"
                    Step.DONE -> "Get started"
                    else -> "Proceed"
                },
                onClick = vm::next,
                modifier = Modifier.fillMaxWidth(),
                loading = loading,
            )
        }
        if (vm.step == Step.ACCOUNT) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onExit) { Text("Log in") }
            }
        }
    }
}

@Composable
private fun AccountStep(vm: RegisterViewModel) {
    val errors = if (vm.showErrors) vm.accountErrors() else emptyMap()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UmTextField(vm.firstName, { vm.firstName = it }, "First name", Modifier.weight(1f), error = errors["first"])
        UmTextField(vm.lastName, { vm.lastName = it }, "Last name", Modifier.weight(1f), error = errors["last"])
    }
    UmTextField(vm.email, { vm.email = it.trim() }, "Email", error = errors["email"], keyboardType = KeyboardType.Email, leadingIcon = Icons.Rounded.Email)
    PasswordField(vm.password, { vm.password = it }, error = errors["password"])
    PasswordField(vm.confirmPassword, { vm.confirmPassword = it }, "Confirm password", error = errors["confirm"], imeAction = ImeAction.Done, onDone = vm::next)
    Text(
        "At least ${Validation.MIN_PASSWORD_LENGTH} characters with letters and numbers.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun VerifyStep(vm: RegisterViewModel, loading: Boolean) {
    Text("Code sent to ${vm.email}", style = MaterialTheme.typography.bodyMedium)
    UmTextField(
        vm.code, { vm.code = it.digitsOnly(6) }, "6-digit code",
        error = if (vm.showErrors) Validation.otp(vm.code) else null,
        keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done, onDone = vm::next, leadingIcon = Icons.Rounded.Pin,
    )
    TextButton(onClick = vm::resendCode, enabled = !loading) { Text("Didn't get it? Send a new code") }
}

@Composable
private fun MemberStep(vm: RegisterViewModel) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CardMembership, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                "Classes are free for members. Non-members can still book and pay per class.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    PrimaryButton("Link my membership", { vm.answerMember(true) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Badge)
    SecondaryButton("No, I'm not a member", { vm.answerMember(false) }, Modifier.fillMaxWidth())
}

@Composable
private fun LinkStep(vm: RegisterViewModel) {
    UmTextField(
        vm.membershipIdText, { vm.membershipIdText = it.digitsOnly(9) }, "Membership ID",
        error = if (vm.showErrors && vm.membershipIdText.toIntOrNull() == null) "Enter your membership ID" else null,
        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onDone = vm::next, leadingIcon = Icons.Rounded.Badge,
        supporting = "The membership must be registered to ${vm.email}.",
    )
}

@Composable
private fun DetailsStep(vm: RegisterViewModel) {
    DateField("Date of birth", vm.dateOfBirth, { vm.dateOfBirth = it }, error = if (vm.showErrors) vm.dobError() else null)
    Text("Gender", style = MaterialTheme.typography.labelLarge)
    SegmentedChoice(Gender.entries, vm.gender, { vm.gender = it }, { it.label() })
    Spacer(Modifier.height(8.dp))
    Text("Address", style = MaterialTheme.typography.labelLarge)
    AddressFields(vm.address)
}

@Composable
private fun DoneStep(vm: RegisterViewModel) {
    val height = vm.body.heightInCm()
    val weight = vm.body.weightInKg()
    val age = runCatching { Period.between(LocalDate.parse(vm.dateOfBirth), LocalDate.now()).years }.getOrNull()
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            Text("${vm.firstName} ${vm.lastName}", style = MaterialTheme.typography.titleMedium)
            Text(vm.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            vm.membership?.let { Text("Membership #${it.membershipId}", color = MaterialTheme.colorScheme.primary) }
            if (height != null && weight != null && age != null) {
                val stats = Fitness.bodyStats(height, weight, vm.activityLevel, age, vm.gender)
                Text(
                    "Estimated daily energy: ${stats.tdee} kcal · BMI ${stats.bmi}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
