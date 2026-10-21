package com.unyieldingmight.app.ui.classes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SportsMma
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.R
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.Pill
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.app.ui.theme.Success
import com.unyieldingmight.app.ui.theme.Warning
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.ClassCategory
import com.unyieldingmight.shared.model.ClassStatus
import com.unyieldingmight.shared.model.GymClassDto
import com.unyieldingmight.shared.model.Intensity

val ClassCategory.icon: ImageVector
    get() = when (this) {
        ClassCategory.STRENGTH -> Icons.Rounded.FitnessCenter
        ClassCategory.CARDIO -> Icons.AutoMirrored.Rounded.DirectionsBike
        ClassCategory.COMBAT -> Icons.Rounded.SportsMma
        ClassCategory.MIND_BODY -> Icons.Rounded.SelfImprovement
        ClassCategory.DANCE -> Icons.Rounded.MusicNote
        ClassCategory.CONDITIONING -> Icons.Rounded.Bolt
    }

private val ClassCategory.colors: List<Color>
    get() = when (this) {
        ClassCategory.STRENGTH -> listOf(Color(0xFF3A1A08), Color(0xFFFF6B2C))
        ClassCategory.CARDIO -> listOf(Color(0xFF07283A), Color(0xFF2CA8FF))
        ClassCategory.COMBAT -> listOf(Color(0xFF2A0A0A), Color(0xFFE53935))
        ClassCategory.MIND_BODY -> listOf(Color(0xFF0B2A1E), Color(0xFF3CC48A))
        ClassCategory.DANCE -> listOf(Color(0xFF2A0B2A), Color(0xFFD94BD9))
        ClassCategory.CONDITIONING -> listOf(Color(0xFF2A2405), Color(0xFFF2C12E))
    }

val Intensity.color: Color
    get() = when (this) {
        Intensity.BEGINNER -> Success
        Intensity.INTERMEDIATE -> Warning
        Intensity.ATHLETE -> Color(0xFFE53935)
    }

fun Intensity.label() = name.lowercase().replaceFirstChar { it.uppercase() }

/** Category artwork: a gradient with the category icon (combat classes reuse the original boxing photo). */
@Composable
fun ClassArt(category: ClassCategory, modifier: Modifier = Modifier, iconSize: Dp = 56.dp) {
    Box(modifier.background(Brush.linearGradient(category.colors))) {
        if (category == ClassCategory.COMBAT) {
            // matchParentSize: the photo must not influence the card's measured height.
            Image(painterResource(R.drawable.boxfit), null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().alpha(0.55f))
        }
        Icon(
            category.icon, null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(iconSize),
        )
    }
}

@Composable
fun IntensityPill(intensity: Intensity) = Pill(intensity.label(), intensity.color)

@Composable
fun ClassStatusPill(gymClass: GymClassDto) {
    when {
        gymClass.status == ClassStatus.CANCELLED -> Pill("Cancelled", MaterialTheme.colorScheme.error)
        gymClass.status == ClassStatus.COMPLETED -> Pill("Completed", MaterialTheme.colorScheme.outline, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        gymClass.bookedByMe -> Pill("Booked", Success)
        gymClass.isFull -> Pill("Full", MaterialTheme.colorScheme.error)
        gymClass.spotsLeft <= 3 -> Pill("${gymClass.spotsLeft} left", Warning)
    }
}

@Composable
fun CapacityBar(gymClass: GymClassDto, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Groups, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${gymClass.bookedCount}/${gymClass.maxCapacity} booked",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { gymClass.bookedCount / gymClass.maxCapacity.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (gymClass.isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            drawStopIndicator = {},
        )
    }
}

/** Card used in lists and grids: artwork strip, name, time, trainer, intensity and capacity. */
@Composable
fun ClassCard(gymClass: GymClassDto, onClick: () -> Unit, modifier: Modifier = Modifier, showDate: Boolean = false, isMember: Boolean = false) {
    Surface(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
        // Height follows the text so large font sizes never clip; the artwork stretches to match.
        Row(Modifier.height(IntrinsicSize.Min)) {
            ClassArt(gymClass.category, Modifier.width(88.dp).fillMaxHeight(), iconSize = 32.dp)
            Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(gymClass.category.label.uppercase(), style = Overline, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    ClassStatusPill(gymClass)
                }
                Text(gymClass.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.AccessTime, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        (if (showDate) "${Format.relativeDay(gymClass.startsAt)} · " else "") + Format.timeRange(gymClass.startsAt, gymClass.endsAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Text(
                    listOfNotNull(gymClass.trainer?.fullName, if (isMember) "Included" else Money.format(gymClass.priceCents, "Free")).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntensityPill(gymClass.intensity)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("${gymClass.avgCalories} kcal", style = MaterialTheme.typography.labelSmall)
                    }
                }
                CapacityBar(gymClass, Modifier.padding(top = 2.dp))
            }
        }
    }
}
