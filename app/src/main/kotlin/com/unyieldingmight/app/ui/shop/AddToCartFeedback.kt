package com.unyieldingmight.app.ui.shop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * "Fly to cart" feedback: a bubble with the item's icon arcs from the tapped button to the cart icon,
 * then the cart badge pops. Positions are in root coordinates.
 */
@Stable
class FlyToCartState {
    data class Flight(val id: Long, val from: Offset?, val icon: ImageVector)

    /** Centre of the cart icon, reported by the top bar. */
    var target by mutableStateOf<Offset?>(null)
    internal val flights = mutableStateListOf<Flight>()

    /** Increments when a flight lands; the cart badge animates on change. */
    var landed by mutableIntStateOf(0)
        private set

    private var nextId = 0L

    /** [from] is the button's centre; null starts from the lower middle of the screen (e.g. from a bottom sheet). */
    fun launch(from: Offset?, icon: ImageVector) {
        flights += Flight(nextId++, from, icon)
    }

    internal fun land(flight: Flight) {
        flights.remove(flight)
        landed++
    }
}

@Composable
fun rememberFlyToCartState() = remember { FlyToCartState() }

/** Draws in-flight bubbles. Place it over the screen; it never consumes touches. */
@Composable
fun FlyToCartOverlay(state: FlyToCartState) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(Offset.Zero) }
    val bubble = 40.dp
    val half = with(LocalDensity.current) { (bubble / 2).toPx() }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                origin = it.positionInRoot()
                size = Offset(it.size.width.toFloat(), it.size.height.toFloat())
            }
    ) {
        val target = state.target ?: return@Box
        state.flights.forEach { flight ->
            key(flight.id) {
                val progress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    progress.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
                    state.land(flight)
                }
                val start = flight.from ?: (origin + Offset(size.x / 2, size.y * 0.75f))
                // Quadratic Bézier with the control point above both ends gives a natural throw arc.
                val control = Offset((start.x + target.x) / 2, minOf(start.y, target.y) - 160f)
                val t = progress.value
                val point = start * ((1 - t) * (1 - t)) + control * (2 * (1 - t) * t) + target * (t * t)
                val local = point - origin
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .offset { IntOffset((local.x - half).roundToInt(), (local.y - half).roundToInt()) }
                        .size(bubble)
                        .graphicsLayer {
                            val scale = 1f - 0.55f * t
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - 0.3f * t
                        },
                ) {
                    Icon(flight.icon, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(9.dp))
                }
            }
        }
    }
}

/** Scale for the cart badge: a quick pop each time an item lands. */
@Composable
fun cartBadgeScale(state: FlyToCartState): Float {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(state.landed) {
        if (state.landed == 0) return@LaunchedEffect
        scale.snapTo(1.45f)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }
    return scale.value
}
