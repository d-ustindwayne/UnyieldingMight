package com.unyieldingmight.app.ui.shell

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.unyieldingmight.app.ui.admin.AdminClassEditScreen
import com.unyieldingmight.app.ui.admin.AdminClassesScreen
import com.unyieldingmight.app.ui.admin.AdminDashboardScreen
import com.unyieldingmight.app.ui.admin.AdminNewsletterScreen
import com.unyieldingmight.app.ui.admin.AdminShopScreen
import com.unyieldingmight.app.ui.admin.AdminTrainersScreen
import com.unyieldingmight.app.ui.admin.AdminUsersScreen
import com.unyieldingmight.app.ui.calories.CaloriesScreen
import com.unyieldingmight.app.ui.classes.BookingsScreen
import com.unyieldingmight.app.ui.classes.ClassDetailScreen
import com.unyieldingmight.app.ui.classes.ClassesScreen
import com.unyieldingmight.app.ui.home.HomeScreen
import com.unyieldingmight.app.ui.navigation.*
import com.unyieldingmight.app.ui.news.NewsScreen
import com.unyieldingmight.app.ui.settings.AboutScreen
import com.unyieldingmight.app.ui.settings.MembershipScreen
import com.unyieldingmight.app.ui.settings.ProfileScreen
import com.unyieldingmight.app.ui.settings.SettingsScreen
import com.unyieldingmight.app.ui.shop.CartScreen
import com.unyieldingmight.app.ui.shop.OrdersScreen
import com.unyieldingmight.app.ui.shop.ShopScreen
import com.unyieldingmight.shared.model.UserDto

@Composable
fun MainNavHost(nav: NavHostController, user: UserDto) {
    // Read the user through State so the graph builder stays the same lambda when the profile changes;
    // otherwise every profile update would rebuild the graph and reset the back stack.
    val currentUser by rememberUpdatedState(user)
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) {
        composable<HomeRoute> { HomeScreen(currentUser) }
        composable<ClassesRoute> { ClassesScreen() }
        composable<ShopRoute> { ShopScreen() }
        composable<CaloriesRoute> { CaloriesScreen(currentUser) }
        composable<NewsRoute> { NewsScreen() }
        composable<SettingsRoute> { SettingsScreen(currentUser) }

        composable<ClassDetailRoute> { ClassDetailScreen(it.toRoute<ClassDetailRoute>().id, currentUser) }
        composable<BookingsRoute> { BookingsScreen() }
        composable<CartRoute> { CartScreen() }
        composable<OrdersRoute> { OrdersScreen() }
        composable<ProfileRoute> { ProfileScreen(currentUser) }
        composable<MembershipRoute> { MembershipScreen(currentUser) }
        composable<AboutRoute> { AboutScreen() }

        // Admin routes are always registered (the server enforces access); the drawer only shows them to admins.
        composable<AdminRoute> { AdminDashboardScreen() }
        composable<AdminClassesRoute> { AdminClassesScreen() }
        composable<AdminClassEditRoute> { AdminClassEditScreen(it.toRoute<AdminClassEditRoute>().id) }
        composable<AdminTrainersRoute> { AdminTrainersScreen() }
        composable<AdminShopRoute> { AdminShopScreen() }
        composable<AdminUsersRoute> { AdminUsersScreen(currentUser) }
        composable<AdminNewsletterRoute> { AdminNewsletterScreen() }
    }
}
