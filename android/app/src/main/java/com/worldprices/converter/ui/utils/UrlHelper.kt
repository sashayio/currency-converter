package com.worldprices.converter.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object UrlHelper {
    /**
     * Safely opens an external URL in the user's preferred web browser or app.
     * Uses FLAG_ACTIVITY_NEW_TASK so it can be called from any context (including localizedContext / ContextWrapper).
     */
    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                    null
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Toast.makeText(context, url, Toast.LENGTH_LONG).show()
            }
        }
    }
}
