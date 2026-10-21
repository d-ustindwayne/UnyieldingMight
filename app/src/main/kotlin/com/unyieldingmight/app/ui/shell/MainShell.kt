package com.unyieldingmight.app.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unyieldingmight.app.data.Cart
import com.unyieldingmight.app.ui.common.LocalContainer
import com.unyieldingmight.app.ui.common.UmLogo
import com.unyieldingmight.app.ui.navigation.AboutRoute
import com.unyieldingmight.app.ui.navigation.AdminRoute
import com.unyieldingmight.app.ui.navigation.BookingsRoute
import com.unyieldingmight.app.ui.navigation.MembershipRoute
import com.unyieldingmight.app.ui.navigation.OrdersRoute
import com.unyieldingmight.app.ui.navigation.ProfileRoute
import com.unyieldingmight.app.ui.navigation.TopLevel
import com.unyieldingmight.app.ui.theme.Overline
import com.unyieldingmight.shared.model.UserDto
import kotlinx.coroutines.launch

/** What screens need from the shell: navigation and (on phones/medium) a way to open the drawer. */
class ShellActions(
    val nav: NavHostController,
    val openDrawer: (() -> Unit)?,
)

val LocalShell = staticCompositionLocalOf<ShellActions> { error("ShellActions not provided") }

@Composable
fun MainShell(windowSize: WindowSizeClass, user: UserDto) {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val cartCount = Cart.count(LocalContainer.current.cart.entries.collectAsState().value)

    val navigateTop: (TopLevel) -> Unit = { tab -> nav.navigateTopLevel(tab.route) }
    val navigateDrawer: (Any) -> Unit = { route ->
        scope.launch { drawerState.close() }
        nav.navigate(route) { launchSingleTop = true }
    }

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
            // Tablets / unfolded foldables: everything visible in a permanent drawer.
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(Modifier.width(300.dp)) {
                        DrawerContent(user, destination, includeTabs = true, onTab = navigateTop, onNavigate = { nav.navigate(it) { launchSingleTop = true } })
                    }
                },
            ) {
                CompositionLocalProvider(LocalShell provides ShellActions(nav, openDrawer = null)) {
                    MainNavHost(nav, user)
                }
            }
        }
        WindowWidthSizeClass.Medium -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { ModalDrawerSheet { DrawerContent(user, destination, includeTabs = false, onTab = {}, onNavigate = navigateDrawer) } },
            ) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail(
                        header = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Rounded.Menu, "Open menu") }
                        },
                    ) {
                        Spacer(Modifier.weight(1f))
                        TopLevel.entries.forEach { tab ->
                            val selected = destination.isOn(tab)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTop(tab) },
                                icon = { TabIcon(tab, selected, if (tab == TopLevel.SHOP) cartCount else 0) },
                                label = { Text(tab.label) },
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    Box(Modifier.weight(1f)) {
                        CompositionLocalProvider(LocalShell provides ShellActions(nav, openDrawer = null)) {
                            MainNavHost(nav, user)
                        }
                    }
                }
            }
        }
        else -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { ModalDrawerSheet { DrawerContent(user, destination, includeTabs = false, onTab = {}, onNavigate = navigateDrawer, includeSettings = true) } },
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0),
                    bottomBar = {
                        val onTopLevel = TopLevel.entries.any { destination.isOn(it) }
                        if (onTopLevel) {
                            NavigationBar {
                                TopLevel.bottomBar.forEach { tab ->
                                    val selected = destination.isOn(tab)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTop(tab) },
                                        icon = { TabIcon(tab, selected, if (tab == TopLevel.SHOP) cartCount else 0) },
                                        label = { Text(tab.label, maxLines = 1) },
                                    )
                                }
                            }
                        }
                    },
                ) { padding ->
                    Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
                        CompositionLocalProvider(LocalShell provides ShellActions(nav, openDrawer = { scope.launch { drawerState.open() } })) {
                            MainNavHost(nav, user)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: TopLevel, selected: Boolean, badge: Int) {
    BadgedBox(badge = { if (badge > 0) Badge { Text("$badge") } }) {
        Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = tab.label)
    }
}

private fun NavDestination?.isOn(tab: TopLevel): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.route::class) } == true

fun NavHostController.navigateTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun DrawerContent(
    user: UserDto,
    destination: NavDestination?,
    includeTabs: Boolean,
    onTab: (TopLevel) -> Unit,
    onNavigate: (Any) -> Unit,
    includeSettings: Boolean = false,
) {
    val container = LocalContainer.current
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 16.dp)) {
        UmLogo(Modifier.padding(start = 8.dp).size(width = 72.dp, height = 40.dp))
        Spacer(Modifier.height(12.dp))
        ProfileHeader(user, onClick = { onNavigate(ProfileRoute) })
        Spacer(Modifier.height(12.dp))

        if (includeTabs) {
            TopLevel.entries.forEach { tab ->
                val selected = destination.isOn(tab)
                DrawerItem(tab.label, if (selected) tab.selectedIcon else tab.icon, selected) { onTab(tab) }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        Text("YOU", style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
        DrawerItem("My profile", Icons.Rounded.Person, destination.isRoute(ProfileRoute)) { onNavigate(ProfileRoute) }
        DrawerItem("Membership", Icons.Rounded.CardMembership, destination.isRoute(MembershipRoute)) { onNavigate(MembershipRoute) }
        DrawerItem("My bookings", Icons.Rounded.EventAvailable, destination.isRoute(BookingsRoute)) { onNavigate(BookingsRoute) }
        DrawerItem("Orders", Icons.Rounded.Receipt, destination.isRoute(OrdersRoute)) { onNavigate(OrdersRoute) }
        if (includeSettings) {
            DrawerItem(TopLevel.SETTINGS.label, TopLevel.SETTINGS.icon, destination.isOn(TopLevel.SETTINGS)) { onNavigate(TopLevel.SETTINGS.route) }
        }
        if (user.isAdmin) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("STAFF", style = Overline, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp))
            DrawerItem("Admin dashboard", Icons.Rounded.AdminPanelSettings, destination.isRoute(AdminRoute)) { onNavigate(AdminRoute) }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        DrawerItem("About", Icons.Rounded.Info, destination.isRoute(AboutRoute)) { onNavigate(AboutRoute) }
        DrawerItem("Log out", Icons.AutoMirrored.Rounded.Logout, false) {
            scope.launch {
                container.cart.clear()
                container.session.signOut()
            }
        }
    }
}

private fun NavDestination?.isRoute(route: Any) = this?.hasRoute(route::class) == true

@Composable
private fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    )
}

@Composable
private fun ProfileHeader(user: UserDto, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(user, 44)
            Column(Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        user.isAdmin -> "ADMIN"
                        user.isMember -> "MEMBER"
                        else -> "GUEST · NOT A MEMBER"
                    },
                    style = Overline,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun Avatar(user: UserDto, sizeDp: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(sizeDp.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "${user.firstName.firstOrNull() ?: ' '}${user.lastName.firstOrNull() ?: ' '}".uppercase(),
                style = if (sizeDp >= 64) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
