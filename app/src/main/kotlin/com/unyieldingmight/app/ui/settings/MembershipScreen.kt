package com.unyieldingmight.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SecondaryButton
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.digitsOnly
import com.unyieldingmight.app.ui.payment.rememberPaymentLauncher
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.MembershipPlanDto
import com.unyieldingmight.shared.model.MembershipStatus
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.UserDto
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MembershipViewModel(container: AppContainer) : LoadViewModel<List<MembershipPlanDto>>(container) {
    val busy = MutableStateFlow(false)
    val pendingPayment = MutableStateFlow<PaymentIntentDto?>(null)

    override suspend fun load() = container.api.membershipPlans()

    init { refresh() }

    fun link(id: Int) = act(busy, { container.api.linkMembership(id) }) {
        container.session.updateUser(it)
        container.messenger.success("Membership linked", "Every class is now included. Enjoy!")
    }

    fun buy(plan: MembershipPlanDto) = act(busy, { container.api.purchaseMembership(plan.id) }) { pendingPayment.value = it }

    fun onPaymentFinished() {
        pendingPayment.value = null
        viewModelScope.launch { container.session.refreshUser() }
    }
}

@Composable
fun MembershipScreen(user: UserDto) {
    val vm = containerViewModel { MembershipViewModel(it) }
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val pending by vm.pendingPayment.collectAsState()
    var membershipId by rememberSaveable { mutableStateOf("") }
    val payments = rememberPaymentLauncher { vm.onPaymentFinished() }

    pending?.let { intent -> LaunchedEffect(intent.paymentId) { payments.launch(intent) } }

    ScreenScaffold("Membership") { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { plans ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Centered(900.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        StatusCard(user)
                        if (!user.isAdmin) {
                            SectionHeader(if (user.isMember) "Renew or upgrade" else "Choose a plan", overline = "Membership plans")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                plans.forEach { plan ->
                                    PlanCard(plan, highlighted = plan.code == "QUARTERLY", busy = busy || payments.busy, Modifier.weight(1f).widthIn(min = 240.dp)) { vm.buy(plan) }
                                }
                            }
                            if (!user.isMember) {
                                SectionHeader("Already have a card?", overline = "Link a membership")
                                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            "If you signed up at the front desk, enter the Membership ID from the back of your card. It must be registered to ${user.email}.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        UmTextField(
                                            membershipId, { membershipId = it.digitsOnly(9) }, "Membership ID",
                                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, leadingIcon = Icons.Rounded.Badge,
                                        )
                                        SecondaryButton("Link membership", { membershipId.toIntOrNull()?.let(vm::link) }, Modifier.fillMaxWidth(), enabled = membershipId.isNotEmpty() && !busy)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(user: UserDto) {
    val membership = user.membership
    Surface(shape = MaterialTheme.shapes.extraLarge, color = if (user.isMember) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Rounded.CardMembership, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        user.isAdmin -> "STAFF ACCOUNT"
                        membership == null -> "NO MEMBERSHIP"
                        else -> "MEMBERSHIP #${membership.id}"
                    },
                    style = Overline,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    when {
                        user.isAdmin -> "Staff accounts don't need a membership"
                        membership == null -> "You're paying per class"
                        membership.status == MembershipStatus.ACTIVE -> (membership.planName ?: "Gym") + " membership · active"
                        else -> "Your membership has ${membership.status.name.lowercase()}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                membership?.expiresAt?.let {
                    Text(
                        (if (membership.status == MembershipStatus.ACTIVE) "Renews or ends " else "Ended ") + Format.fullDate(it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (membership != null && membership.expiresAt == null && membership.status == MembershipStatus.ACTIVE) {
                    Text("Ongoing membership managed by the front desk", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: MembershipPlanDto, highlighted: Boolean, busy: Boolean, modifier: Modifier, onBuy: () -> Unit) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (highlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (highlighted) Text("MOST POPULAR", style = Overline, color = MaterialTheme.colorScheme.primary)
            Text(plan.name, style = MaterialTheme.typography.titleLarge)
            Text(Money.format(plan.priceCents), style = MaterialTheme.typography.headlineMedium)
            Text("for ${plan.durationDays} days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(plan.description, style = MaterialTheme.typography.bodyMedium)
            listOf("Every class included", "Book instantly, no payment step").forEach {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (highlighted) PrimaryButton("Choose ${plan.name}", onBuy, Modifier.fillMaxWidth(), loading = busy)
            else SecondaryButton("Choose ${plan.name}", onBuy, Modifier.fillMaxWidth(), enabled = !busy)
        }
    }
}
