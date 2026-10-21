package com.unyieldingmight.app.ui.calories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.unyieldingmight.app.data.UnitSystem
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.DropdownField
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.StatTile
import com.unyieldingmight.app.ui.common.UmTextField
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.domain.CalorieBurner
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.domain.Units
import com.unyieldingmight.shared.model.UserDto
import kotlin.math.roundToInt

/** Holds today's tally across tab switches; it's a quick estimate, not persisted. */
class CaloriesViewModel : ViewModel() {
    data class Entry(val activity: CalorieBurner.Activity, val minutes: Int, val kcal: Int)

    val tally = mutableStateListOf<Entry>()
}

@Composable
fun CaloriesScreen(user: UserDto) {
    val vm = containerViewModel { CaloriesViewModel() }
    val container = LocalContainer.current
    val units by container.preferences.units.collectAsState()
    val metric = units == UnitSystem.METRIC

    var activityId by rememberSaveable { mutableStateOf(CalorieBurner.activities.first { it.id == "hiit" }.id) }
    var minutes by rememberSaveable { mutableStateOf(45f) }
    val defaultWeight = user.stats?.weightKg ?: 70.0
    var weightText by rememberSaveable(units) {
        mutableStateOf((if (metric) defaultWeight else Units.kgToLb(defaultWeight)).roundToInt().toString())
    }

    val activity = CalorieBurner.byId(activityId)!!
    val weightKg = weightText.toDoubleOrNull()?.let { if (metric) it else Units.lbToKg(it) } ?: 0.0
    val burned = CalorieBurner.caloriesBurned(activity.met, weightKg, minutes.toDouble())

    ScreenScaffold("Calorie burner", topLevel = true) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= 760.dp
            val calculator: @Composable () -> Unit = {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Workout burn", overline = "Calculator")
                        DropdownField(
                            "Activity",
                            CalorieBurner.activities.sortedBy { it.group },
                            activity,
                            { activityId = it.id },
                            { "${it.name} · ${it.group}" },
                        )
                        Text("Duration: ${minutes.roundToInt()} min", style = MaterialTheme.typography.labelLarge)
                        Slider(value = minutes, onValueChange = { minutes = it }, valueRange = 5f..180f, steps = 34)
                        UmTextField(
                            weightText, { weightText = it.filter { c -> c.isDigit() || c == '.' }.take(5) }, "Your weight",
                            keyboardType = KeyboardType.Decimal, suffix = if (metric) "kg" else "lb",
                            supporting = if (user.stats != null) "From your profile" else null,
                        )
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.Bottom) {
                            Column(Modifier.weight(1f)) {
                                Text("ESTIMATED BURN", style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$burned kcal", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                                Text("MET ${activity.met} · ${(burned / minutes).let { "%.1f".format(it) }} kcal/min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { vm.tally += CaloriesViewModel.Entry(activity, minutes.roundToInt(), burned) }, enabled = burned > 0) {
                                Icon(Icons.Rounded.Add, null)
                                Text("Add to today")
                            }
                        }
                        Equivalents(burned)
                    }
                }
            }
            val daily: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    user.stats?.let { stats ->
                        SectionHeader("Your daily energy", overline = "Based on your profile")
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatTile("BMR", "${stats.bmr}", Modifier.weight(1f), caption = "kcal at rest")
                            StatTile("TDEE", "${stats.tdee}", Modifier.weight(1f), caption = "kcal / day · ${stats.activityLevel.label}")
                        }
                        val goals = Fitness.calorieGoals(stats.tdee)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatTile("Lose", "${goals.cut}", Modifier.weight(1f), caption = "≈0.5 kg/week")
                            StatTile("Maintain", "${goals.maintain}", Modifier.weight(1f))
                            StatTile("Gain", "${goals.bulk}", Modifier.weight(1f), caption = "lean bulk")
                        }
                    }
                    TodayTally(vm)
                    Text(
                        "Estimates use MET values from the Compendium of Physical Activities and the Mifflin-St Jeor equation. Real burn varies with fitness and intensity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (wide) {
                Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f)) { calculator() }
                    Column(Modifier.weight(1f)) { daily() }
                }
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Centered {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            calculator()
                            daily()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Equivalents(kcal: Int) {
    if (kcal <= 0) return
    val foods = listOf("banana" to 105, "slice of pizza" to 285, "chocolate bar" to 230, "flat white" to 120)
    Text(
        "That's about " + foods.joinToString(", ") { (food, cal) -> "${"%.1f".format(kcal / cal.toDouble())} × $food" } + ".",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TodayTally(vm: CaloriesViewModel) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today's workouts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${vm.tally.sumOf { it.kcal }} kcal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (vm.tally.isEmpty()) {
                Text("Add workouts from the calculator to total up your day.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            vm.tally.forEachIndexed { index, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${entry.activity.name} · ${entry.minutes} min", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${entry.kcal} kcal", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { vm.tally.removeAt(index) }) { Icon(Icons.Rounded.Close, "Remove") }
                }
            }
        }
    }
}
