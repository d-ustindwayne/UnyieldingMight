package com.unyieldingmight.app.ui.common

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_ZOOM = 5f

/**
 * Full-screen cropper: the photo can be dragged and pinch-zoomed behind a fixed frame of [aspectRatio]
 * (width / height). Only what's inside the frame is kept. The photo always covers the frame, so there are no gaps.
 */
@Composable
fun PhotoCropper(bitmap: Bitmap, aspectRatio: Float, onCancel: () -> Unit, onCropped: (Bitmap) -> Unit) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    var zoom by remember(bitmap) { mutableFloatStateOf(1f) }
    // Offset of the photo's centre from the frame's centre, in screen pixels.
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }

    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Column(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
            Text(
                "Drag and pinch to choose what shows",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
            )
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val density = LocalDensity.current
                val area = with(density) { Size(maxWidth.toPx(), maxHeight.toPx()) }
                val margin = with(density) { 16.dp.toPx() }
                val frame = remember(area, aspectRatio) { frameRect(area, aspectRatio, margin) }
                // Scale at which the photo exactly covers the frame; zoom multiplies it.
                val coverScale = max(frame.width / bitmap.width, frame.height / bitmap.height)

                fun clamp(candidate: Offset, z: Float): Offset {
                    val scale = coverScale * z
                    val maxX = (bitmap.width * scale - frame.width) / 2f
                    val maxY = (bitmap.height * scale - frame.height) / 2f
                    return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
                }

                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(bitmap, frame) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                val newZoom = (zoom * gestureZoom).coerceIn(1f, MAX_ZOOM)
                                // Scale the offset with the zoom so pinching zooms around the frame's centre.
                                offset = clamp((offset * (newZoom / zoom)) + pan, newZoom)
                                zoom = newZoom
                            }
                        },
                ) {
                    val scale = coverScale * zoom
                    val drawn = Size(bitmap.width * scale, bitmap.height * scale)
                    val topLeft = frame.center + offset - Offset(drawn.width / 2f, drawn.height / 2f)
                    drawImage(
                        image,
                        dstOffset = IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt()),
                        dstSize = IntSize(drawn.width.roundToInt(), drawn.height.roundToInt()),
                        filterQuality = FilterQuality.Medium,
                    )
                    // Dim everything outside the frame.
                    clipRect(frame.left, frame.top, frame.right, frame.bottom, ClipOp.Difference) {
                        drawRect(Color.Black.copy(alpha = 0.6f))
                    }
                    drawRect(Color.White, frame.topLeft, frame.size, style = Stroke(2.dp.toPx()))
                    // Rule-of-thirds guides.
                    for (i in 1..2) {
                        val x = frame.left + frame.width * i / 3f
                        val y = frame.top + frame.height * i / 3f
                        drawLine(Color.White.copy(alpha = 0.4f), Offset(x, frame.top), Offset(x, frame.bottom), 1.dp.toPx())
                        drawLine(Color.White.copy(alpha = 0.4f), Offset(frame.left, y), Offset(frame.right, y), 1.dp.toPx())
                    }
                }

                Row(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { zoom = 1f; offset = Offset.Zero }) { Text("Reset", color = Color.White) }
                    Box(Modifier.weight(1f))
                    TextButton(onClick = onCancel) { Text("Cancel", color = Color.White) }
                    Button(onClick = { onCropped(cropBitmap(bitmap, frame, coverScale * zoom, offset)) }) { Text("Use photo") }
                }
            }
        }
    }
}

/** The largest rectangle of [aspectRatio] that fits in [area] with [margin] around it, leaving room for the buttons. */
private fun frameRect(area: Size, aspectRatio: Float, margin: Float): Rect {
    val maxWidth = area.width - margin * 2
    val maxHeight = area.height * 0.75f - margin * 2
    var width = maxWidth
    var height = width / aspectRatio
    if (height > maxHeight) {
        height = maxHeight
        width = height * aspectRatio
    }
    val left = (area.width - width) / 2f
    val top = (area.height * 0.75f - height) / 2f
    return Rect(left, top, left + width, top + height)
}

/** Maps the frame back onto the original bitmap's pixels and cuts that part out. */
private fun cropBitmap(bitmap: Bitmap, frame: Rect, scale: Float, offset: Offset): Bitmap {
    val imageLeft = frame.center.x + offset.x - bitmap.width * scale / 2f
    val imageTop = frame.center.y + offset.y - bitmap.height * scale / 2f
    val x = ((frame.left - imageLeft) / scale).roundToInt().coerceIn(0, bitmap.width - 1)
    val y = ((frame.top - imageTop) / scale).roundToInt().coerceIn(0, bitmap.height - 1)
    val width = (frame.width / scale).roundToInt().coerceIn(1, bitmap.width - x)
    val height = (frame.height / scale).roundToInt().coerceIn(1, bitmap.height - y)
    return Bitmap.createBitmap(bitmap, x, y, min(width, bitmap.width - x), min(height, bitmap.height - y))
}
