package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.classes.ClassCard
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.navigation.AdminClassEditRoute
import com.unyieldingmight.app.ui.navigation.ClassDetailRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.model.AdminBookingDto
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import kotlinx.coroutines.flow.MutableStateFlow

class AdminClassesViewModel(container: AppContainer) : LoadViewModel<List<GymClassDto>>(container) {
    val attendees = MutableStateFlow<Pair<GymClassDto, List<AdminBookingDto>>?>(null)

    override suspend fun load() = container.api.adminClasses()

    fun cancel(c: GymClassDto) = act(block = { container.api.cancelClass(c.id) }) {
        container.messenger.success("Class cancelled", it.message)
        refresh()
    }

    fun delete(c: GymClassDto) = act(block = { container.api.deleteClass(c.id) }) {
        container.messenger.success("Class deleted", "${c.name} has been removed from the schedule.")
        refresh()
    }

    fun showAttendees(c: GymClassDto) = act(block = { container.api.classAttendees(c.id) }) { attendees.value = c to it }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassesScreen() {
    val vm = containerViewModel { AdminClassesViewModel(it) }
    val state by vm.state.collectAsState()
    val attendees by vm.attendees.collectAsState()
    val shell = LocalShell.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        title = "Manage classes",
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { shell.nav.navigate(AdminClassEditRoute()) },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("New class") },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                listOf("Upcoming", "Past & cancelled").forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            LoadContent(state, vm::refresh) { all ->
                val now = System.currentTimeMillis()
                val shown = if (tab == 0) all.filter { it.status == ClassStatus.SCHEDULED && it.endsAt > now }
                else all.filter { it.status != ClassStatus.SCHEDULED || it.endsAt <= now }.sortedByDescending { it.startsAt }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(360.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (shown.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { EmptyState(Icons.Rounded.Block, "Nothing here", "No classes in this list.") }
                    }
                    items(shown, key = { it.id }) { c ->
                        Box {
                            ClassCard(c, onClick = { shell.nav.navigate(ClassDetailRoute(c.id)) }, showDate = true)
                            var menu by remember { mutableStateOf(false) }
                            Box(Modifier.align(Alignment.TopEnd)) {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "Class actions") }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Attendees (${c.bookedCount})") },
                                        leadingIcon = { Icon(Icons.Rounded.Groups, null) },
                                        onClick = { menu = false; vm.showAttendees(c) },
                                    )
                                    if (c.status == ClassStatus.SCHEDULED && c.startsAt > now) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                                            onClick = { menu = false; shell.nav.navigate(AdminClassEditRoute(c.id)) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Cancel class") },
                                            leadingIcon = { Icon(Icons.Rounded.Block, null) },
                                            onClick = {
                                                menu = false
                                                confirm = "Cancel ${c.name}? ${c.bookedCount} booked member(s) will be emailed and any payments refunded." to { vm.cancel(c) }
                                            },
                                        )
                                    }
                                    if (c.bookedCount == 0 || c.status != ClassStatus.SCHEDULED) {
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                                            onClick = { menu = false; confirm = "Delete ${c.name} permanently?" to { vm.delete(c) } },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    confirm?.let { (text, action) ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text("Are you sure?") },
            text = { Text(text) },
            confirmButton = { TextButton(onClick = { action(); confirm = null }) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Back") } },
        )
    }

    attendees?.let { (c, list) ->
        ModalBottomSheet(onDismissRequest = { vm.attendees.value = null }) {
            Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Text("ATTENDEES", style = Overline, color = MaterialTheme.colorScheme.primary)
                    Text(c.name, style = MaterialTheme.typography.titleLarge)
                    Text("${Format.fullDate(c.startsAt)} · ${Format.timeRange(c.startsAt, c.endsAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (list.isEmpty()) EmptyState(Icons.Rounded.Groups, "No bookings", "Nobody has booked this class yet.")
                LazyColumn {
                    items(list, key = { it.id }) { b ->
                        ListItem(
                            headlineContent = { Text(b.userName) },
                            supportingContent = { Text(b.userEmail) },
                            trailingContent = { Text(b.status.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
