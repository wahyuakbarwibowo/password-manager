package com.aminmart.passwordmanager.ui.components

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.widget.Toast

private const val SENSITIVE_CLEAR_DELAY_MS = 60_000L

private val mainHandler = Handler(Looper.getMainLooper())
private var pendingClear: Runnable? = null

/**
 * Copy text to the clipboard. Sensitive values are hidden from the clipboard
 * preview / keyboard suggestions on Android 13+ and cleared again after
 * [SENSITIVE_CLEAR_DELAY_MS] so a copied password does not linger.
 */
fun copyToClipboard(context: Context, text: String, label: String, isSensitive: Boolean = false) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    if (isSensitive) {
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    clipboard.setPrimaryClip(clip)
    // Android 13+ shows its own clipboard confirmation overlay
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    pendingClear?.let(mainHandler::removeCallbacks)
    if (!isSensitive) return
    pendingClear = Runnable {
        // ponytail: Android 10+ hides clipboard contents from background apps, so we
        // can't always confirm the clip is still ours; when unreadable, clear anyway.
        val current = runCatching { clipboard.primaryClip?.getItemAt(0)?.text?.toString() }.getOrNull()
        if (current != null && current != text) return@Runnable
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip()
        } else {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }.also { mainHandler.postDelayed(it, SENSITIVE_CLEAR_DELAY_MS) }
}
