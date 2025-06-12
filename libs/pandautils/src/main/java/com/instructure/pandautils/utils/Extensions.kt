/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.work.Data
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.ln
import kotlin.math.pow
import androidx.core.net.toUri
import java.io.File

fun Any.toJson(): String {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJson(): T {
    return Gson().fromJson<T>(this, T::class.java)
}

fun JsonObject.getObjectOrNull(memberName: String): JsonObject? {
    val jsonObj = this.get(memberName) ?: return null
    return if (jsonObj.isJsonObject) {
        jsonObj as JsonObject
    } else {
        null
    }
}

fun JsonObject.getArrayOrNull(memberName: String): JsonArray? {
    val jsonArray = this.get(memberName) ?: return null
    return if (jsonArray.isJsonArray) {
        jsonArray as JsonArray
    } else {
        null
    }
}

fun Long.humanReadableByteCount(): String {
    val unit = 1024
    if (this < unit) return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = "KMGTPE"[exp - 1].toString()
    return String.format(Locale.getDefault(), "%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Long?.orDefault(default: Long = 0): Long {
    return this ?: default
}

fun Int?.orDefault(default: Int = 0): Int {
    return this ?: default
}

fun Boolean?.orDefault(default: Boolean = false): Boolean {
    return this ?: default
}

fun Double?.orDefault(default: Double = 0.0): Double {
    return this ?: default
}

fun Data.newBuilder(): Data.Builder {
    return Data.Builder()
        .putAll(this)
}

suspend fun retry(retryCount: Int = 5, initialDelay: Long = 100, factor: Float = 2f, maxDelay: Long = 1000, block: suspend () -> Unit) {
    var currentDelay = initialDelay
    repeat(retryCount.coerceAtLeast(1)) {
        try {
            block()
            return
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
}

suspend fun <T> poll(
    pollInterval: Long = 1000,
    maxAttempts: Int = 10,
    block: suspend () -> T?,
    validate: suspend (T) -> Boolean
): T? {
    var attempts = 0
    while (attempts < maxAttempts || maxAttempts == -1) {
        val result = block()
        result?.let {
            if (validate(it)) {
                return result
            }
        }
        attempts++
        delay(pollInterval)
    }
    return null
}

fun Activity.launchCustomTab(url: String, @ColorInt color: Int) {
    val uri = url.toUri()
        .buildUpon()
        .appendQueryParameter("display", "borderless")
        .appendQueryParameter("platform", "android")
        .build()

    val colorSchemeParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(color)
        .setNavigationBarColor(window.navigationBarColor)
        .build()

    var intent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(colorSchemeParams)
        .setShowTitle(true)
        .build()
        .intent

    intent.data = uri

    // Exclude Instructure apps from chooser options
    intent = intent.asChooserExcludingInstructure()

    startActivity(intent)
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

fun String.SHA256(): String {
    return MessageDigest.getInstance("SHA-256").digest(toByteArray()).joinToString("") {
        "%02x".format(it)
    }
}

fun Context.openFile(
    filePathToOpen: String,
    mimeType: String,
    chooserTitle: String
) {
    val file = File(filePathToOpen)
    val uri = FileProvider.getUriForFile(this, applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file)

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivity(Intent.createChooser(intent, chooserTitle))
}