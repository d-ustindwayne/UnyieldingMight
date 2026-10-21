package com.unyieldingmight.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.classes.ClassArt
import com.unyieldingmight.app.ui.classes.ClassCard
import com.unyieldingmight.app.ui.classes.IntensityPill
import com.unyieldingmight.app.ui.common.Centered
import com.unyieldingmight.app.ui.common.Format
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.StatTile
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.navigation.AdminRoute
import com.unyieldingmight.app.ui.navigation.BookingsRoute
import com.unyieldingmight.app.ui.navigation.CaloriesRoute
import com.unyieldingmight.app.ui.navigation.ClassDetailRoute
import com.unyieldingmight.app.ui.navigation.ClassesRoute
import com.unyieldingmight.app.ui.navigation.MembershipRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.app.ui.shell.navigateTopLevel
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.domain.Fitness
import com.unyieldingmight.shared.model.BookingDto
import com.unyieldingmight.shared.model.BookingStatus
import com.unyieldingmight.shared.model.RecommendationDto
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalTime

data class HomeData(val upcoming: List<BookingDto>, val recommendations: List<RecommendationDto>)

class HomeViewModel(container: AppContainer) : LoadViewModel<HomeData>(container) {
    override suspend fun load(): HomeData = coroutineScope {
        val bookings = async { container.api.myBookings() }
        val recs = async { runCatching { container.api.recommendedClasses() }.getOrDefault(emptyList()) }
        container.session.refreshUser()
        val now = System.currentTimeMillis()
        HomeData(
            upcoming = bookings.await().filter { it.status == BookingStatus.BOOKED && it.gymClass.endsAt > now }.sortedBy { it.gymClass.startsAt },
            recommendations = recs.await(),
        )
    }
}

@Composable
fun HomeScreen(user: UserDto) {
    val vm = containerViewModel { HomeViewModel(it) }
    val state by vm.state.collectAsState()
    val shell = LocalShell.current
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold("Home", topLevel = true) { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { data ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Centered(1040.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Greeting(user)
                        if (user.isAdmin) {
                            ActionCard(Icons.Rounded.AdminPanelSettings, "Admin dashboard", "Manage classes, trainers, the shop and members.") {
                                shell.nav.navigate(AdminRoute)
                            }
                        } else if (!user.isMember) {
                            ActionCard(Icons.Rounded.CardMembership, "Become a member", "Book every class for free. Plans from \$49 a month.") {
                                shell.nav.navigate(MembershipRoute)
                            }
                        }
                        user.stats?.let { stats ->
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = 3) {
                                val tile = Modifier.weight(1f).widthIn(min = 100.dp)
                                StatTile("Energy", "${stats.tdee}", tile, caption = "kcal / day", icon = Icons.Rounded.Bolt)
                                StatTile("BMI", "${stats.bmi}", tile, caption = Fitness.bmiCategory(stats.bmi), icon = Icons.Rounded.MonitorWeight)
                                StatTile("Booked", "${data.upcoming.size}", tile, caption = "upcoming", icon = Icons.Rounded.EventAvailable)
                            }
                        }

                        SectionHeader("Next up", overline = "Your schedule", action = {
                            TextButton(onClick = { shell.nav.navigate(BookingsRoute) }) { Text("All bookings") }
                        })
                        val next = data.upcoming.firstOrNull()
                        if (next == null) {
                            ActionCard(Icons.Rounded.EventAvailable, "Nothing booked yet", "Browse this week's classes and grab a spot.") {
                                shell.nav.navigateTopLevel(ClassesRoute)
                            }
                        } else {
                            ClassCard(next.gymClass, onClick = { shell.nav.navigate(ClassDetailRoute(next.gymClass.id)) }, showDate = true, isMember = user.isMember)
                        }

                        if (data.recommendations.isNotEmpty()) {
                            SectionHeader("Recommended for you", overline = "Picked from your profile")
                        }
                    }
                }
                if (data.recommendations.isNotEmpty()) {
                    LazyRow(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(data.recommendations, key = { it.gymClass.id }) { rec ->
                            RecommendationCard(rec) { shell.nav.navigate(ClassDetailRoute(rec.gymClass.id)) }
                        }
                    }
                }
                Centered(1040.dp) {
                    Column(Modifier.padding(16.dp)) {
                        ActionCard(Icons.Rounded.Bolt, "Calorie burner", "Estimate how many calories your workout burns.") {
                            shell.nav.navigateTopLevel(CaloriesRoute)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Greeting(user: UserDto) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..21 -> "Good evening"
        else -> "Welcome back"
    }
    Column {
        Text(
            when {
                user.isAdmin -> "STAFF"
                user.isMember -> "MEMBER #${user.membership?.id}"
                else -> "WELCOME"
            },
            style = Overline,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("$greeting, ${user.firstName}", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun ActionCard(icon: ImageVector, title: String, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun RecommendationCard(rec: RecommendationDto, onClick: () -> Unit) {
    val c = rec.gymClass
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.width(260.dp)) {
        Column {
            ClassArt(c.category, Modifier.fillMaxWidth().height(110.dp), iconSize = 40.dp)
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IntensityPill(c.intensity)
                    Text("${rec.score}% match", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(c.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${Format.relativeDay(c.startsAt)} · ${Format.time(c.startsAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                rec.reasons.firstOrNull()?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(it, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
