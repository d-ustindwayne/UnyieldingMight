package com.unyieldingmight.app.ui.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.rememberIsMember
import com.unyieldingmight.app.ui.navigation.ClassDetailRoute
import com.unyieldingmight.app.ui.navigation.ClassesRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.shell.navigateTopLevel
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus

class BookingsViewModel(container: AppContainer) : LoadViewModel<List<BookingDto>>(container) {
    override suspend fun load() = container.api.myBookings()
}

@Composable
fun BookingsScreen() {
    val vm = containerViewModel { BookingsViewModel(it) }
    val state by vm.state.collectAsState()
    val shell = LocalShell.current
    val isMember = rememberIsMember()
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold("My bookings") { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { bookings ->
            val now = System.currentTimeMillis()
            val upcoming = bookings.filter { it.status == BookingStatus.BOOKED && it.gymClass.endsAt > now }.sortedBy { it.gymClass.startsAt }
            val past = bookings - upcoming.toSet()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(340.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (bookings.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.EventAvailable, "No bookings yet", "Find a class that suits you and book your first session.") {
                            OutlinedButton(onClick = { shell.nav.navigateTopLevel(ClassesRoute) }) { Text("Browse classes") }
                        }
                    }
                }
                section("Upcoming", upcoming, isMember) { shell.nav.navigate(ClassDetailRoute(it)) }
                section("History", past, isMember) { shell.nav.navigate(ClassDetailRoute(it)) }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.section(
    title: String,
    bookings: List<BookingDto>,
    isMember: Boolean,
    onOpen: (Int) -> Unit,
) {
    if (bookings.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        Text(title.uppercase(), style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
    items(bookings, key = { it.id }) { booking ->
        val gymClass = when (booking.status) {
            BookingStatus.BOOKED -> booking.gymClass
            // Show the booking's own outcome rather than the class's live state.
            BookingStatus.CLASS_CANCELLED -> booking.gymClass.copy(status = com.unyieldingmight.shared.model.ClassStatus.CANCELLED, bookedByMe = false)
            else -> booking.gymClass.copy(bookedByMe = false)
        }
        ClassCard(gymClass, onClick = { onOpen(booking.gymClass.id) }, showDate = true, isMember = isMember)
    }
}
