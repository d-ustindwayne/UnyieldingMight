package com.unyieldingmight.app.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Photo chosen from the gallery, cropped to [aspectRatio] (width / height) and uploaded to the server, which stores
 * it and returns its URL. The current photo can be re-cropped with the Crop button. [preview] draws the current photo
 * (or the placeholder art when [url] is null).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoField(url: String?, onChange: (String?) -> Unit, aspectRatio: Float, preview: @Composable (url: String?) -> Unit) {
    val container = LocalContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var cropping by remember { mutableStateOf<Bitmap?>(null) }

    fun run(label: String, block: suspend () -> Unit) {
        scope.launch {
            busy = label
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                container.messenger.error(e, "Couldn't use that photo")
            } finally {
                busy = null
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) run("Opening…") { cropping = withContext(Dispatchers.IO) { decodeUpright(context, uri) } }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        preview(url)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = busy == null,
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, null)
                Text(busy?.let { "  $it" } ?: if (url == null) "  Upload photo" else "  Change photo")
            }
            if (url != null) {
                TextButton(onClick = { run("Opening…") { cropping = loadForCrop(context, url) } }, enabled = busy == null) {
                    Icon(Icons.Rounded.Crop, null)
                    Text(" Crop")
                }
                TextButton(onClick = { onChange(null) }, enabled = busy == null) {
                    Icon(Icons.Rounded.Delete, null)
                    Text(" Remove")
                }
            }
        }
        Text(
            "JPEG, PNG or WebP. You'll choose which part to show before it uploads.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    cropping?.let { bitmap ->
        PhotoCropper(
            bitmap, aspectRatio,
            onCancel = { cropping = null },
            onCropped = { cropped ->
                cropping = null
                run("Uploading…") {
                    val jpeg = withContext(Dispatchers.Default) { encodeForUpload(cropped) }
                    onChange(container.api.uploadImage(jpeg).url)
                }
            },
        )
    }
}

/** Big enough to crop from without losing detail, small enough to keep in memory comfortably. */
private const val MAX_SOURCE_SIDE = 2400

/** Longest side of what's uploaded. */
private const val MAX_UPLOAD_SIDE = 1600

/** Decodes the picked photo the right way up, scaled down to at most [MAX_SOURCE_SIDE] px. */
private fun decodeUpright(context: Context, uri: Uri): Bitmap =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val scale = MAX_SOURCE_SIDE.toFloat() / max(info.size.width, info.size.height)
            if (scale < 1f) decoder.setTargetSize((info.size.width * scale).roundToInt(), (info.size.height * scale).roundToInt())
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SOURCE_SIDE) sample *= 2
        context.contentResolver.openInputStream(uri)!!.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("That file isn't a photo we can read")
    }

/** Loads the photo currently in use (bundled, uploaded or https) so it can be cropped again. */
private suspend fun loadForCrop(context: Context, url: String): Bitmap {
    val request = ImageRequest.Builder(context)
        .data(resolveImageUrl(url))
        .size(MAX_SOURCE_SIDE)
        .allowHardware(false)
        .build()
    val result = context.imageLoader.execute(request) as? SuccessResult ?: error("Couldn't load the current photo")
    return result.image.toBitmap()
}

/** Scales to at most [MAX_UPLOAD_SIDE] px and encodes as JPEG, flattening any transparency onto white. */
private fun encodeForUpload(source: Bitmap): ByteArray {
    val scale = MAX_UPLOAD_SIDE.toFloat() / max(source.width, source.height)
    val sized = if (scale < 1f) {
        Bitmap.createScaledBitmap(source, (source.width * scale).roundToInt(), (source.height * scale).roundToInt(), true)
    } else source
    // JPEG has no transparency; flatten transparent PNGs onto white (like the product shots) instead of black.
    val opaque = if (sized.hasAlpha()) {
        Bitmap.createBitmap(sized.width, sized.height, Bitmap.Config.ARGB_8888).also {
            Canvas(it).apply { drawColor(Color.WHITE); drawBitmap(sized, 0f, 0f, null) }
        }
    } else sized
    return ByteArrayOutputStream().use { out ->
        opaque.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.toByteArray()
    }
}
