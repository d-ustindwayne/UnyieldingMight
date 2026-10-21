package com.unyieldingmight.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.data.UnitSystem
import com.unyieldingmight.shared.domain.Units
import com.unyieldingmight.shared.model.ActivityLevel
import com.unyieldingmight.shared.model.AddressDto
import com.unyieldingmight.shared.model.Gender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(optionLabel(option)) }, onClick = { onSelected(option); expanded = false })
            }
        }
    }
}

/** Read-only field that opens a date picker. [value] is an ISO date or blank. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1920..LocalDate.now().year,
    defaultDate: LocalDate = LocalDate.now().minusYears(25),
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val display = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMMM yyyy")) }.getOrDefault("")
    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = { IconButton(onClick = { open = true }) { Icon(Icons.Rounded.CalendarMonth, "Pick date") } },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    )
    if (open) {
        val initial = runCatching { LocalDate.parse(value) }.getOrNull() ?: defaultDate
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = yearRange,
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onValueChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
}

@Composable
fun <T> SegmentedChoice(options: List<T>, selected: T, onSelected: (T) -> Unit, label: (T) -> String, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label(option), maxLines = 1) }
        }
    }
}

fun Gender.label() = name.lowercase().replaceFirstChar { it.uppercase() }

val Countries = listOf("New Zealand", "Australia")

class AddressInput(initial: AddressDto? = null) {
    var street by mutableStateOf(initial?.street.orEmpty())
    var city by mutableStateOf(initial?.city.orEmpty())
    var region by mutableStateOf(initial?.region.orEmpty())
    var postcode by mutableStateOf(initial?.postcode.orEmpty())
    var country by mutableStateOf(initial?.country?.takeIf { it.isNotBlank() } ?: Countries.first())
    var showErrors by mutableStateOf(false)

    fun errors(): Map<String, String?> = mapOf(
        "street" to com.unyieldingmight.shared.domain.Validation.required(street, "Street"),
        "city" to com.unyieldingmight.shared.domain.Validation.required(city, "City", 50),
        "region" to com.unyieldingmight.shared.domain.Validation.required(region, "Region", 50),
        "postcode" to com.unyieldingmight.shared.domain.Validation.postcode(postcode),
    )

    fun isValid() = errors().values.all { it == null }

    fun toDto() = AddressDto(street.trim(), city.trim(), region.trim(), postcode.trim(), country)
}

@Composable
fun AddressFields(input: AddressInput) {
    val errors = if (input.showErrors) input.errors() else emptyMap()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        UmTextField(input.street, { input.street = it }, "Street", error = errors["street"])
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UmTextField(input.city, { input.city = it }, "City / Town", Modifier.weight(1f), error = errors["city"])
            UmTextField(input.region, { input.region = it }, "Region", Modifier.weight(1f), error = errors["region"])
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            UmTextField(input.postcode, { input.postcode = it }, "Postcode", Modifier.weight(1f), error = errors["postcode"], keyboardType = KeyboardType.Number)
            DropdownField("Country", Countries, input.country, { input.country = it }, { it }, Modifier.weight(1f))
        }
    }
}

/** Height & weight entry in the user's preferred units, always converted to cm/kg. */
class BodyInput(heightCm: Double? = null, weightKg: Double? = null, units: UnitSystem = UnitSystem.METRIC) {
    var units by mutableStateOf(units)
    var heightCm by mutableStateOf(heightCm?.let { "%.1f".format(it).removeSuffix(".0") } ?: "")
    var feet by mutableStateOf(heightCm?.let { (Units.cmToInches(it) / 12).toInt().toString() } ?: "")
    var inches by mutableStateOf(heightCm?.let { (Units.cmToInches(it) % 12).toInt().toString() } ?: "")
    var weight by mutableStateOf(weightKg?.let { (if (units == UnitSystem.METRIC) it else Units.kgToLb(it)).let { w -> "%.1f".format(w).removeSuffix(".0") } } ?: "")
    var showErrors by mutableStateOf(false)

    fun heightInCm(): Double? = when (units) {
        UnitSystem.METRIC -> heightCm.toDoubleOrNull()
        UnitSystem.IMPERIAL -> feet.toIntOrNull()?.let { f -> Units.feetInchesToCm(f, inches.toDoubleOrNull() ?: 0.0) }
    }

    fun weightInKg(): Double? = weight.toDoubleOrNull()?.let { if (units == UnitSystem.METRIC) it else Units.lbToKg(it) }

    fun heightError() = com.unyieldingmight.shared.domain.Validation.heightCm(heightInCm())
    fun weightError() = com.unyieldingmight.shared.domain.Validation.weightKg(weightInKg())
    fun isValid() = heightError() == null && weightError() == null

    fun switchUnits(to: UnitSystem) {
        if (to == units) return
        val cm = heightInCm()
        val kg = weightInKg()
        units = to
        cm?.let {
            heightCm = "%.1f".format(it).removeSuffix(".0")
            feet = (Units.cmToInches(it) / 12).toInt().toString()
            inches = (Units.cmToInches(it) % 12).toInt().toString()
        }
        kg?.let { weight = "%.1f".format(if (to == UnitSystem.METRIC) it else Units.kgToLb(it)).removeSuffix(".0") }
    }
}

@Composable
fun BodyFields(input: BodyInput) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SegmentedChoice(UnitSystem.entries, input.units, input::switchUnits, { if (it == UnitSystem.METRIC) "cm · kg" else "ft · lb" })
        if (input.units == UnitSystem.METRIC) {
            UmTextField(
                input.heightCm, { input.heightCm = it }, "Height", keyboardType = KeyboardType.Decimal, suffix = "cm",
                error = if (input.showErrors) input.heightError() else null,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                UmTextField(input.feet, { input.feet = it }, "Height", Modifier.weight(1f), keyboardType = KeyboardType.Number, suffix = "ft",
                    error = if (input.showErrors) input.heightError() else null)
                UmTextField(input.inches, { input.inches = it }, "", Modifier.weight(1f), keyboardType = KeyboardType.Number, suffix = "in")
            }
        }
        UmTextField(
            input.weight, { input.weight = it }, "Weight", keyboardType = KeyboardType.Decimal,
            suffix = if (input.units == UnitSystem.METRIC) "kg" else "lb",
            error = if (input.showErrors) input.weightError() else null,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
        )
    }
}

@Composable
fun ActivityLevelPicker(selected: ActivityLevel, onSelected: (ActivityLevel) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ActivityLevel.entries.forEach { level ->
            val isSelected = level == selected
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth().selectable(isSelected, role = Role.RadioButton) { onSelected(level) },
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                    Column {
                        Text(level.label, style = MaterialTheme.typography.titleSmall)
                        Text(level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Numeric-only filter for text fields. */
fun String.digitsOnly(max: Int = Int.MAX_VALUE) = filter { it.isDigit() }.take(max)
