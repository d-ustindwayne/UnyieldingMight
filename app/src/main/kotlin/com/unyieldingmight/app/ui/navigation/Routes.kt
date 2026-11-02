package com.unyieldingmight.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Group
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

/** [id] of -1 creates a new class. */
@Serializable data class AdminClassEditRoute(val id: Int = -1)
/** [id] of -1 creates a new shop item. */
@Serializable data class AdminShopItemEditRoute(val id: Int = -1)
@Serializable data object AdminTrainersRoute
@Serializable data object AdminUsersRoute
/** [id] of -1 creates an account for a member at the front desk. */
@Serializable data class AdminUserEditRoute(val id: Int = -1)
@Serializable data object AdminNewsletterRoute

/**
 * Top-level tabs. Customers get the six from the original plan: home, classes, shop, calorie burner, news, settings.
 * Admins only manage the gym, so Home becomes their dashboard and Members replaces the calorie burner.
 */
enum class TopLevel(val label: String, val route: Any, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Home", HomeRoute, Icons.Outlined.Home, Icons.Rounded.Home),
    CLASSES("Classes", ClassesRoute, Icons.Outlined.FitnessCenter, Icons.Rounded.FitnessCenter),
    SHOP("Shop", ShopRoute, Icons.Outlined.ShoppingBag, Icons.Rounded.ShoppingBag),
    CALORIES("Calories", CaloriesRoute, Icons.Outlined.LocalFireDepartment, Icons.Rounded.LocalFireDepartment),
    MEMBERS("Members", AdminUsersRoute, Icons.Outlined.Group, Icons.Rounded.Group),
    NEWS("News", NewsRoute, Icons.AutoMirrored.Outlined.Article, Icons.AutoMirrored.Rounded.Article),
    SETTINGS("Settings", SettingsRoute, Icons.Outlined.Settings, Icons.Rounded.Settings);

    fun label(isAdmin: Boolean) = if (this == HOME && isAdmin) "Dashboard" else label

    companion object {
        fun forUser(isAdmin: Boolean): List<TopLevel> =
            if (isAdmin) listOf(HOME, CLASSES, SHOP, MEMBERS, NEWS, SETTINGS) else listOf(HOME, CLASSES, SHOP, CALORIES, NEWS, SETTINGS)

        /** Settings moves into the drawer on phones, where a 6-item bottom bar would be too cramped. */
        fun bottomBar(isAdmin: Boolean) = forUser(isAdmin).filter { it != SETTINGS }
    }
}
