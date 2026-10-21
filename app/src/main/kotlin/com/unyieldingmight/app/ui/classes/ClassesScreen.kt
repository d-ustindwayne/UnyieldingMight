package com.unyieldingmight.app.ui.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.EmptyState
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.rememberIsMember
import com.unyieldingmight.app.ui.navigation.BookingsRoute
import com.unyieldingmight.app.ui.navigation.ClassDetailRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.Intensity
import java.time.LocalDate

class ClassesViewModel(container: AppContainer) : LoadViewModel<List<GymClassDto>>(container) {
    override suspend fun load() = container.api.classes()
}

@Composable
fun ClassesScreen() {
    val vm = containerViewModel { ClassesViewModel(it) }
    val state by vm.state.collectAsState()
    val shell = LocalShell.current
    val isMember = rememberIsMember()
    var query by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    var day by rememberSaveable { mutableStateOf<String?>(null) }
    var intensity by rememberSaveable { mutableStateOf<Intensity?>(null) }
    var category by rememberSaveable { mutableStateOf<ClassCategory?>(null) }

    // Refresh when coming back from a detail screen so capacity and booked state stay current.
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold(
        title = "Classes",
        topLevel = true,
        actions = {
            IconButton(onClick = { searching = !searching; if (!searching) query = "" }) { Icon(Icons.Rounded.Search, "Search classes") }
            IconButton(onClick = { shell.nav.navigate(BookingsRoute) }) { Icon(Icons.Rounded.EventAvailable, "My bookings") }
        },
    ) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { all ->
            val upcoming = all.filter { it.status == ClassStatus.SCHEDULED && it.endsAt > System.currentTimeMillis() }
            val days = upcoming.map { Format.date(it.startsAt) }.distinct().sorted()
            val filtered = upcoming.filter { c ->
                (day == null || Format.date(c.startsAt).toString() == day) &&
                    (intensity == null || c.intensity == intensity) &&
                    (category == null || c.category == category) &&
                    (query.isBlank() || listOfNotNull(c.name, c.description, c.trainer?.fullName).any { it.contains(query, ignoreCase = true) })
            }
            val grouped = filtered.groupBy { Format.date(it.startsAt) }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (searching) UmTextField(query, { query = it }, "Search by class, trainer or keyword", leadingIcon = Icons.Rounded.Search)
                        DayChips(days, day) { day = it }
                        FilterRow(intensity, { intensity = it }, category, { category = it })
                    }
                }
                if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Rounded.SearchOff, "No classes found", "Try a different day or clear your filters.")
                    }
                }
                grouped.forEach { (date, classes) ->
                    item(span = { GridItemSpan(maxLineSpan) }, key = "header-$date") {
                        Text(
                            Format.dayHeader(date).uppercase(),
                            style = Overline,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(classes, key = { it.id }) { c ->
                        ClassCard(c, onClick = { shell.nav.navigate(ClassDetailRoute(c.id)) }, isMember = isMember)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun DayChips(days: List<LocalDate>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All days") }) }
        items(days) { date ->
            FilterChip(
                selected = selected == date.toString(),
                onClick = { onSelect(if (selected == date.toString()) null else date.toString()) },
                label = { Text(Format.chipDay(date)) },
            )
        }
    }
}

@Composable
private fun FilterRow(
    intensity: Intensity?,
    onIntensity: (Intensity?) -> Unit,
    category: ClassCategory?,
    onCategory: (ClassCategory?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(Intensity.entries) { level ->
            FilterChip(
                selected = intensity == level,
                onClick = { onIntensity(if (intensity == level) null else level) },
                label = { Text(level.label()) },
            )
        }
        items(ClassCategory.entries) { c ->
            FilterChip(
                selected = category == c,
                onClick = { onCategory(if (category == c) null else c) },
                label = { Text(c.label) },
                leadingIcon = { Icon(c.icon, null) },
            )
        }
    }
}

