package com.unyieldingmight.app.ui.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SecondaryButton
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.navigation.AdminClassEditRoute
import com.unyieldingmight.app.ui.navigation.MembershipRoute
import com.unyieldingmight.app.ui.payment.rememberPaymentLauncher
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.PaymentIntentDto
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class ClassDetail(val gymClass: GymClassDto, val booking: BookingDto?)

class ClassDetailViewModel(container: AppContainer, private val id: Int) : LoadViewModel<ClassDetail>(container) {
    val busy = MutableStateFlow(false)
    val pendingPayment = MutableStateFlow<PaymentIntentDto?>(null)

    override suspend fun load(): ClassDetail = coroutineScope {
        val gymClass = async { container.api.gymClass(id) }
        val bookings = async { container.api.myBookings() }
        ClassDetail(gymClass.await(), bookings.await().firstOrNull { it.gymClass.id == id && it.status == BookingStatus.BOOKED })
    }

    init { refresh() }

    fun book() = act(busy, { container.api.bookClass(id) }) { response ->
        response.booking?.let {
            container.messenger.success("You're booked in!", "See you at ${it.gymClass.name} ${Format.relativeDay(it.gymClass.startsAt).lowercase()} at ${Format.time(it.gymClass.startsAt)}.")
            refresh()
        }
        response.payment?.let { pendingPayment.value = it }
    }

    fun cancelBooking(bookingId: Int) = act(busy, { container.api.cancelBooking(bookingId) }) {
        container.messenger.info("Booking cancelled", it.message)
        refresh()
    }

    fun onPaymentFinished() {
        pendingPayment.value = null
        viewModelScope.launch { refresh() }
    }
}

@Composable
fun ClassDetailScreen(id: Int, user: UserDto) {
    val vm = containerViewModel(key = "class-$id") { ClassDetailViewModel(it, id) }
    val state by vm.state.collectAsState()
    val busy by vm.busy.collectAsState()
    val pending by vm.pendingPayment.collectAsState()
    val shell = LocalShell.current
    val payments = rememberPaymentLauncher { vm.onPaymentFinished() }
    var confirmCancel by remember { mutableStateOf<BookingDto?>(null) }

    pending?.let { intent ->
        LaunchedEffect(intent.paymentId) {
            // Clear first so a rotation doesn't present the payment sheet a second time.
            vm.pendingPayment.value = null
            payments.launch(intent)
        }
    }

    ScreenScaffold(
        title = state.dataOrNull?.gymClass?.name ?: "Class",
        actions = {
            if (user.isAdmin) TextButton(onClick = { shell.nav.navigate(AdminClassEditRoute(id)) }) { Text("Edit") }
        },
    ) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { detail ->
            val c = detail.gymClass
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth >= 760.dp
                val booking: @Composable () -> Unit = {
                    BookingPanel(
                        detail, user, busy || payments.busy,
                        onBook = vm::book,
                        onCancel = { confirmCancel = it },
                        onMembership = { shell.nav.navigate(MembershipRoute) },
                    )
                }
                if (wide) {
                    Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(Modifier.weight(1.4f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ClassInfo(c)
                            TrainerCard(c)
                        }
                        Box(Modifier.weight(1f)) { booking() }
                    }
                } else {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // On phones the booking panel comes before the trainer so the main action is reached sooner.
                        ClassInfo(c)
                        booking()
                        TrainerCard(c)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    confirmCancel?.let { booking ->
        val refundable = booking.paidCents > 0 && booking.gymClass.startsAt - System.currentTimeMillis() > 24 * 3_600_000L
        AlertDialog(
            onDismissRequest = { confirmCancel = null },
            icon = { Icon(Icons.Rounded.EventBusy, null) },
            title = { Text("Cancel booking?") },
            text = {
                Text(
                    when {
                        booking.paidCents == 0 -> "Your spot in ${booking.gymClass.name} will be released for someone else."
                        refundable -> "You'll be refunded ${Money.format(booking.paidCents)}."
                        else -> "This class starts within 24 hours, so your payment won't be refunded."
                    }
                )
            },
            confirmButton = { TextButton(onClick = { vm.cancelBooking(booking.id); confirmCancel = null }) { Text("Cancel booking") } },
            dismissButton = { TextButton(onClick = { confirmCancel = null }) { Text("Keep it") } },
        )
    }
}

@Composable
private fun ClassInfo(c: GymClassDto) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            ClassArt(c.category, Modifier.fillMaxWidth().height(200.dp), iconSize = 80.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(c.category.label.uppercase(), style = Overline, color = MaterialTheme.colorScheme.primary)
            IntensityPill(c.intensity)
            ClassStatusPill(c)
        }
        Text(c.name, style = MaterialTheme.typography.headlineMedium)
        Text(c.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(Icons.Rounded.CalendarMonth, "Date", Format.fullDate(c.startsAt))
                InfoRow(Icons.Rounded.AccessTime, "Time", "${Format.timeRange(c.startsAt, c.endsAt)} (${Format.durationMinutes(c.durationMinutes)})")
                InfoRow(Icons.Rounded.LocalFireDepartment, "Average burn", "${c.avgCalories} kcal")
                InfoRow(Icons.Rounded.Sell, "Price", Money.format(c.priceCents, "Free") + if (c.priceCents > 0) " (free for members)" else "")
            }
        }
    }
}

@Composable
private fun TrainerCard(c: GymClassDto) {
    val t = c.trainer ?: return
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Rounded.Person, null, Modifier.padding(10.dp).size(28.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("YOUR TRAINER", style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(t.fullName, style = MaterialTheme.typography.titleMedium)
                Text(t.specialty, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(t.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BookingPanel(
    detail: ClassDetail,
    user: UserDto,
    busy: Boolean,
    onBook: () -> Unit,
    onCancel: (BookingDto) -> Unit,
    onMembership: () -> Unit,
) {
    val c = detail.gymClass
    val started = c.startsAt <= System.currentTimeMillis()
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CapacityBar(c)
            when {
                detail.booking != null -> {
                    Text("You're booked in", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Arrive 10 minutes early and bring a towel and water.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!started) SecondaryButton("Cancel booking", { onCancel(detail.booking) }, Modifier.fillMaxWidth(), enabled = !busy)
                }
                c.status == ClassStatus.CANCELLED -> Text("This class has been cancelled.", style = MaterialTheme.typography.titleMedium)
                c.status == ClassStatus.COMPLETED || started -> Text("This class has already started.", style = MaterialTheme.typography.titleMedium)
                c.isFull -> Text("This class is full. Check back later in case a spot opens up.", style = MaterialTheme.typography.titleMedium)
                else -> {
                    val free = user.isMember || c.priceCents == 0
                    Text(
                        if (free) "Free" else Money.format(c.priceCents),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        when {
                            user.isMember -> "Included with your membership."
                            c.priceCents == 0 -> "This class is free for everyone."
                            else -> "Pay per class, or become a member to book every class for free."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PrimaryButton(if (free) "Book my spot" else "Book & pay", onBook, Modifier.fillMaxWidth(), loading = busy)
                    if (!user.isMember && !user.isAdmin && c.priceCents > 0) {
                        TextButton(onClick = onMembership, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("View membership plans") }
                    }
                }
            }
        }
    }
}
