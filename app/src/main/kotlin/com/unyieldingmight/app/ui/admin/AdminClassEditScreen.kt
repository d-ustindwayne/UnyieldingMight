package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.classes.IntensityPill
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.DateField
import com.unyieldingmight.app.ui.common.DropdownField
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.PrimaryButton
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.common.digitsOnly
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassUpsertRequest
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.Intensity
import com.unyieldingmight.shared.model.TrainerDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class ClassEditData(val trainers: List<TrainerDto>, val existing: GymClassDto?)

class AdminClassEditViewModel(container: AppContainer, private val id: Int) : LoadViewModel<ClassEditData>(container) {
    val saving = MutableStateFlow(false)

    override suspend fun load() = coroutineScope {
        val trainers = async { container.api.trainers() }
        val existing = if (id > 0) async { container.api.gymClass(id) } else null
        ClassEditData(trainers.await(), existing?.await())
    }

    init { refresh() }

    fun save(request: ClassUpsertRequest, onSaved: () -> Unit) = act(saving, {
        if (id > 0) container.api.updateClass(id, request) else container.api.createClass(request)
    }) {
        container.messenger.success(if (id > 0) "Class updated" else "Class created", "${it.name} on ${Format.fullDate(it.startsAt)} is on the schedule.")
        onSaved()
    }
}

private val durations = listOf(30, 45, 60, 75, 90, 120, 150, 180)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassEditScreen(id: Int) {
    val vm = containerViewModel(key = "edit-class-$id") { AdminClassEditViewModel(it, id) }
    val state by vm.state.collectAsState()
    val saving by vm.saving.collectAsState()
    val shell = LocalShell.current

    ScreenScaffold(if (id > 0) "Edit class" else "New class") { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { data ->
            val existing = data.existing
            val zone = ZoneId.systemDefault()
            var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
            var description by rememberSaveable { mutableStateOf(existing?.description.orEmpty()) }
            var category by rememberSaveable { mutableStateOf(existing?.category ?: ClassCategory.STRENGTH) }
            var trainerId by rememberSaveable { mutableStateOf(existing?.trainer?.id) }
            var date by rememberSaveable { mutableStateOf(existing?.let { Format.date(it.startsAt).toString() } ?: LocalDate.now().plusDays(1).toString()) }
            var time by rememberSaveable { mutableStateOf(existing?.let { java.time.Instant.ofEpochMilli(it.startsAt).atZone(zone).toLocalTime().toString() } ?: "09:00") }
            var duration by rememberSaveable { mutableStateOf(existing?.durationMinutes?.toInt() ?: 60) }
            var capacity by rememberSaveable { mutableStateOf(existing?.maxCapacity?.toString() ?: "15") }
            var calories by rememberSaveable { mutableStateOf(existing?.avgCalories?.toString() ?: "400") }
            var price by rememberSaveable { mutableStateOf(existing?.let { "%.2f".format(it.priceCents / 100.0) } ?: "15.00") }
            var pickingTime by rememberSaveable { mutableStateOf(false) }
            var showErrors by rememberSaveable { mutableStateOf(false) }

            val startTime = runCatching { LocalTime.parse(time) }.getOrNull()
            val start = runCatching { LocalDate.parse(date).atTime(startTime).atZone(zone).toInstant().toEpochMilli() }.getOrNull()
            val errors = mapOf(
                "name" to if (name.isBlank()) "Enter a class name" else null,
                "description" to if (description.isBlank()) "Describe the class" else null,
                "date" to when {
                    start == null -> "Choose a date"
                    start <= System.currentTimeMillis() -> "The class must start in the future"
                    else -> null
                },
                "capacity" to if ((capacity.toIntOrNull() ?: 0) !in 1..500) "Between 1 and 500" else null,
                "calories" to if ((calories.toIntOrNull() ?: -1) !in 0..3000) "Between 0 and 3000" else null,
                "price" to if ((price.toDoubleOrNull() ?: -1.0) !in 0.0..1000.0) "Between $0 and $1000" else null,
            )
            val shown = if (showErrors) errors else emptyMap()

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Centered(720.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Details")
                        UmTextField(name, { name = it }, "Class name", error = shown["name"])
                        UmTextField(description, { description = it }, "Description", error = shown["description"], singleLine = false, minLines = 3)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DropdownField("Category", ClassCategory.entries, category, { category = it }, { it.label }, Modifier.weight(1f))
                            DropdownField(
                                "Trainer", listOf<TrainerDto?>(null) + data.trainers, data.trainers.firstOrNull { it.id == trainerId },
                                { trainerId = it?.id }, { it?.fullName ?: "Unassigned" }, Modifier.weight(1f),
                            )
                        }
                        SectionHeader("Schedule", modifier = Modifier.padding(top = 8.dp))
                        DateField(
                            "Date", date, { date = it }, error = shown["date"],
                            yearRange = LocalDate.now().year..LocalDate.now().year + 2, defaultDate = LocalDate.now().plusDays(1),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = startTime?.let { Format.time(LocalDate.now().atTime(it).atZone(zone).toInstant().toEpochMilli()) } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Start time") },
                                trailingIcon = { IconButton(onClick = { pickingTime = true }) { Icon(Icons.Rounded.Schedule, "Pick time") } },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.weight(1f),
                            )
                            DropdownField("Duration", durations, duration, { duration = it }, { Format.durationMinutes(it.toLong()) }, Modifier.weight(1f))
                        }
                        SectionHeader("Capacity & pricing", modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            UmTextField(capacity, { capacity = it.digitsOnly(3) }, "Max capacity", Modifier.weight(1f), error = shown["capacity"], keyboardType = KeyboardType.Number)
                            UmTextField(calories, { calories = it.digitsOnly(4) }, "Avg calories", Modifier.weight(1f), error = shown["calories"], keyboardType = KeyboardType.Number, suffix = "kcal")
                        }
                        calories.toIntOrNull()?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Intensity: ", style = MaterialTheme.typography.bodySmall)
                                IntensityPill(Intensity.fromCalories(it))
                            }
                        }
                        UmTextField(
                            price, { price = it.filter { c -> c.isDigit() || c == '.' }.take(7) }, "Non-member price", error = shown["price"],
                            keyboardType = KeyboardType.Decimal, supporting = "Members always book for free. Use 0 for a free class.",
                        )
                        PrimaryButton(
                            if (id > 0) "Save changes" else "Create class",
                            onClick = {
                                showErrors = true
                                if (errors.values.any { it != null } || start == null) return@PrimaryButton
                                vm.save(
                                    ClassUpsertRequest(
                                        name = name.trim(),
                                        description = description.trim(),
                                        category = category,
                                        trainerId = trainerId,
                                        startsAt = start,
                                        endsAt = start + duration * 60_000L,
                                        maxCapacity = capacity.toInt(),
                                        avgCalories = calories.toInt(),
                                        priceCents = Math.round(price.toDouble() * 100).toInt(),
                                    )
                                ) { shell.nav.navigateUp() }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            loading = saving,
                        )
                    }
                }
            }

            if (pickingTime) {
                val picker = rememberTimePickerState(startTime?.hour ?: 9, startTime?.minute ?: 0)
                AlertDialog(
                    onDismissRequest = { pickingTime = false },
                    confirmButton = {
                        TextButton(onClick = { time = LocalTime.of(picker.hour, picker.minute).toString(); pickingTime = false }) { Text("OK") }
                    },
                    dismissButton = { TextButton(onClick = { pickingTime = false }) { Text("Cancel") } },
                    text = { TimePicker(picker) },
                )
            }
        }
    }
}
