package com.instructure.pandautils.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.instructure.pandautils.R
import java.io.File

/** Whether or not this Uri is exposed */
fun Uri.isExposed() = "file" == scheme && !path!!.startsWith("/system/")

/** Wraps this file in a safe-to-expose Uri using FileProvider */
fun File.provided(context: Context): Uri = FileProvider.getUriForFile(context, context.packageName + Const.FILE_PROVIDER_AUTHORITY, this)

/** Wraps this Uri in a safe-to-expose Uri (if necessary) using FileProvider */
fun Uri.provided(context: Context): Uri = takeUnless { it.isExposed() } ?: File(path!!).provided(context)

/**
 * Launches an intent to view the contents of this Uri in another app.
 * @param context A valid Android Context
 * @param contentType The MIME type of the content
 * @param onNoApps Called when no apps can handle the intent. Default behavior shows a toast.
 */
fun Uri.viewExternally(context: Context, contentType: String, onNoApps: () -> Unit = { context.toast(R.string.noApps)} ) {
    val uri = provided(context)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setDataAndType(uri, contentType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val appCount = context.packageManager.queryIntentActivities(intent, 0).size
    if (appCount > 0) context.startActivity(intent) else onNoApps()
}