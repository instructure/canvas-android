/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandautils.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.instructure.pandautils.R
import java.io.File

object Utils {
    /**
     * Check if the device has a camera. If it doesn't, return false
     */
    fun hasCameraAvailable(activity: Activity?): Boolean {
        val pm = activity?.packageManager ?: return false
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    fun getAttachmentsDirectory(context: Context): File {
        return context.externalCacheDir?.let { File(it, "attachments") } ?: context.filesDir
    }

    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    val referer: Map<String, String>
        get() = mapOf("Referer" to domain)

    @Suppress("DEPRECATION")
    fun generateUserAgent(context: Context, userAgentString: String): String {
        return try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "$userAgentString/${info.versionName} (${info.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            userAgentString
        }
    }

    fun goToAppStore(appType: AppType, context: Context?) {
        try {
            val goToMarket = Intent(Intent.ACTION_VIEW)
            goToMarket.data = Uri.parse("market://details?id=${appType.packageName}")
            context?.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            // The device might not have the play store installed, open it in a webview
            val goToMarket = Intent(Intent.ACTION_VIEW)
            goToMarket.data = Uri.parse("https://play.google.com/store/apps/details?id=${appType.packageName}")
            context?.startActivity(goToMarket)
        }
    }

    fun copyToClipboard(context: Context, stringToCopy: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.link), stringToCopy))

        // Let the user know
        Toast.makeText(context, context.getString(R.string.linkCopied), Toast.LENGTH_SHORT).show()
    }
}
