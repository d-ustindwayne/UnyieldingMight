package com.unyieldingmight.app.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/**
 * Protects an edit form from accidental back navigation. While [dirty], the system back gesture and the returned
 * function (wire it to the toolbar's back arrow or a dialog's dismiss) ask "Discard changes?" before calling [onLeave].
 */
@Composable
fun rememberLeaveGuard(dirty: Boolean, onLeave: () -> Unit): () -> Unit {
    var asking by remember { mutableStateOf(false) }
    val leave by rememberUpdatedState(onLeave)
    val isDirty by rememberUpdatedState(dirty)

    BackHandler(enabled = dirty) { asking = true }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            icon = { Icon(Icons.Rounded.EditNote, null) },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. If you leave now, they'll be lost.") },
            confirmButton = { TextButton(onClick = { asking = false; leave() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { asking = false }) { Text("Keep editing") } },
        )
    }
    return remember { { if (isDirty) asking = true else leave() } }
}
