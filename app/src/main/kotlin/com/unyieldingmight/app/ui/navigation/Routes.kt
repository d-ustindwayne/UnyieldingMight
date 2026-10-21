package com.unyieldingmight.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Type-safe navigation destinations.

@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object ForgotPasswordRoute

@Serializable data object HomeRoute
@Serializable data object ClassesRoute
@Serializable data object ShopRoute
@Serializable data object CaloriesRoute
@Serializable data object NewsRoute
@Serializable data object SettingsRoute

@Serializable data class ClassDetailRoute(val id: Int)
@Serializable data object BookingsRoute
@Serializable data object CartRoute
@Serializable data object OrdersRoute
@Serializable data object ProfileRoute
@Serializable data object MembershipRoute
@Serializable data object AboutRoute

@Serializable data object AdminRoute
@Serializable data object AdminClassesRoute
/** [id] of -1 creates a new class. */
@Serializable data class AdminClassEditRoute(val id: Int = -1)
@Serializable data object AdminTrainersRoute
@Serializable data object AdminShopRoute
@Serializable data object AdminUsersRoute
@Serializable data object AdminNewsletterRoute

/** The six top-level tabs from the original plan: home, shop, classes, calorie burner, news, settings. */
enum class TopLevel(val label: String, val route: Any, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Home", HomeRoute, Icons.Outlined.Home, Icons.Rounded.Home),
    CLASSES("Classes", ClassesRoute, Icons.Outlined.FitnessCenter, Icons.Rounded.FitnessCenter),
    SHOP("Shop", ShopRoute, Icons.Outlined.ShoppingBag, Icons.Rounded.ShoppingBag),
    CALORIES("Calories", CaloriesRoute, Icons.Outlined.LocalFireDepartment, Icons.Rounded.LocalFireDepartment),
    NEWS("News", NewsRoute, Icons.AutoMirrored.Outlined.Article, Icons.AutoMirrored.Rounded.Article),
    SETTINGS("Settings", SettingsRoute, Icons.Outlined.Settings, Icons.Rounded.Settings);

    companion object {
        /** Settings moves into the drawer on phones, where a 6-item bottom bar would be too cramped. */
        val bottomBar = entries.filter { it != SETTINGS }
    }
}
