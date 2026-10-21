package com.unyieldingmight.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.unyieldingmight.app.AppContainer
import com.unyieldingmight.app.ui.common.LoadContent
import com.unyieldingmight.app.ui.common.LoadViewModel
import com.unyieldingmight.app.ui.common.SectionHeader
import com.unyieldingmight.app.ui.common.StatTile
import com.unyieldingmight.app.ui.common.containerViewModel
import com.unyieldingmight.app.ui.navigation.AdminClassesRoute
import com.unyieldingmight.app.ui.navigation.AdminNewsletterRoute
import com.unyieldingmight.app.ui.navigation.AdminShopRoute
import com.unyieldingmight.app.ui.navigation.AdminTrainersRoute
import com.unyieldingmight.app.ui.navigation.AdminUsersRoute
import com.unyieldingmight.app.ui.shell.LocalShell
import com.unyieldingmight.app.ui.shell.ScreenScaffold
import com.unyieldingmight.shared.domain.Money
import com.unyieldingmight.shared.model.AdminStatsDto

class AdminDashboardViewModel(container: AppContainer) : LoadViewModel<AdminStatsDto>(container) {
    override suspend fun load() = container.api.adminStats()
}

private data class AdminSection(val title: String, val text: String, val icon: ImageVector, val route: Any)

private val sections = listOf(
    AdminSection("Classes", "Schedule, edit, cancel and see who's booked", Icons.Rounded.CalendarMonth, AdminClassesRoute),
    AdminSection("Trainers", "Coach profiles shown on classes", Icons.Rounded.Person, AdminTrainersRoute),
    AdminSection("Shop", "Products, prices and stock", Icons.Rounded.Storefront, AdminShopRoute),
    AdminSection("Members & roles", "Search accounts and manage admin access", Icons.Rounded.Group, AdminUsersRoute),
    AdminSection("Newsletter", "Email every opted-in member", Icons.Rounded.Mail, AdminNewsletterRoute),
)

@Composable
fun AdminDashboardScreen() {
    val vm = containerViewModel { AdminDashboardViewModel(it) }
    val state by vm.state.collectAsState()
    val shell = LocalShell.current
    LaunchedEffect(Unit) { vm.refresh() }

    ScreenScaffold("Admin dashboard") { padding ->
        LoadContent(state, vm::refresh, Modifier.padding(padding)) { stats ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("At a glance", overline = "Live stats") }
                item { StatTile("Members", "${stats.activeMembers}", caption = "active", icon = Icons.Rounded.CardMembership) }
                item { StatTile("Accounts", "${stats.totalUsers}", caption = "registered", icon = Icons.Rounded.Group) }
                item { StatTile("Classes", "${stats.upcomingClasses}", caption = "upcoming", icon = Icons.Rounded.FitnessCenter) }
                item { StatTile("Bookings", "${stats.bookingsThisWeek}", caption = "last 7 days", icon = Icons.Rounded.EventAvailable) }
                item { StatTile("Revenue", Money.format(stats.revenueCents), caption = "last 30 days", icon = Icons.Rounded.Payments) }
                item { StatTile("Low stock", "${stats.lowStockItems}", caption = "items ≤ 5 left", icon = Icons.Rounded.Inventory2) }
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader("Manage", modifier = Modifier.padding(top = 8.dp)) }
                items(sections, span = { GridItemSpan(if (maxLineSpan >= 4) 2 else maxLineSpan) }) { section ->
                    Surface(onClick = { shell.nav.navigate(section.route) }, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(section.icon, null, Modifier.padding(10.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(section.title, style = MaterialTheme.typography.titleMedium)
                                Text(section.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                        }
                    }
                }
            }
        }
    }
}
