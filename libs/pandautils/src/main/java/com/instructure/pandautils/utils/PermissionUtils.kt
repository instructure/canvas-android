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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.webkit.PermissionRequest
import androidx.core.app.ActivityCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

object PermissionUtils {

    const val PERMISSION_REQUEST_CODE = 78
    const val WRITE_FILE_PERMISSION_REQUEST_CODE = 98
    const val READ_FILE_PERMISSION_REQUEST_CODE = 108

    const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

    const val CAMERA = Manifest.permission.CAMERA
    const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

    /**
     * Checks to see if we have the necessary permissions.
     * @param activity A context in the form of an activity
     * @param permissions A string of permissions (we have hard coded values in [PermissionUtils])
     * @return a boolean telling if the user has the necessary permissions
     */
    fun hasPermissions(activity: Activity, vararg permissions: String): Boolean {
        val validPermissions = filterStoragePermission(permissions)
        return validPermissions
                .map { activity.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
                .all { it }
    }

    fun filterStoragePermission(permissions: Array<out String>): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.filter { it != WRITE_EXTERNAL_STORAGE }
        } else {
            permissions.toList()
        }
    }

    // Helper to make an array
    @Suppress("UNCHECKED_CAST")
    fun makeArray(vararg items: String): Array<String> {
        return filterStoragePermission(items as Array<String>).toTypedArray()
    }

    /**
     * Whether or not all of the permissions were granted.
     * @param grantResults the array returned from [Activity.onRequestPermissionsResult]
     * @return the result telling if all permissions were granted
     */
    fun allPermissionsGrantedResultSummary(grantResults: IntArray): Boolean = grantResults.indices.none { grantResults[it] == PackageManager.PERMISSION_DENIED }

    fun permissionGranted(permissions: Array<String>, grantResults: IntArray, permission: String): Boolean {
        var permissionsGranted = false

        if (permissions.size == grantResults.size && !TextUtils.isEmpty(permission)) {
            permissions.indices
                    .filter { permissions[it] == permission && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                    .forEach { permissionsGranted = true }
        }

        return permissionsGranted
    }
}

/**
 * Extension method on [Context] to check for permissions
 */
fun Context.hasPermissions(vararg permissions: String): Boolean {
    val validPermissions = PermissionUtils.filterStoragePermission(permissions)
    return validPermissions
            .map { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .all { it }
    return true
}

fun Activity.needsPermissions(successCallback: () -> Unit, failureCallback: () -> Unit, vararg permissions: String): Boolean {
    if (PermissionUtils.hasPermissions(this, *permissions)) {
        return false
    }

    this.requestPermissions(setOf(*permissions)) { results ->
        if (results.isNotEmpty() && results.all { it.value }) {
            successCallback()
        } else {
            failureCallback()
        }
    }
    return true
}

/**
 * Attempts to request web permissions by mapping them to the relevant system permissions.
 *
 * Note that only the [PermissionRequest.RESOURCE_VIDEO_CAPTURE] and [PermissionRequest.RESOURCE_AUDIO_CAPTURE] web
 * permissions are supported at this time.
 */
fun Activity.requestWebPermissions(request: PermissionRequest) {
    val permissionsMap = mapOf(
        PermissionRequest.RESOURCE_VIDEO_CAPTURE to setOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
        PermissionRequest.RESOURCE_AUDIO_CAPTURE to setOf(Manifest.permission.RECORD_AUDIO)
    ).withDefault { emptySet() }

    val mappedPermissions = request.resources.flatMap { res -> permissionsMap.getValue(res) }.toSet()

    requestPermissions(mappedPermissions) { results ->
        if ((request.resources.isNotEmpty() && results.isEmpty()) || results.all { !it.value }) {
            // If permissions list is empty (i.e. canceled) or none are granted, deny the request
            request.deny()
        } else {
            val granted = request.resources.filter { res -> permissionsMap.getValue(res).all { results[it] == true } }
            request.grant(granted.toTypedArray())
        }
    }
}

/**
 * Attempts to request the specified permissions and invokes [onComplete] with the results.
 *
 * For this function to work correctly the Activity must implement [ActivityCompat.OnRequestPermissionsResultCallback]
 * and delegate it to an instance of [PermissionReceiver].
 */
fun Activity.requestPermissions(permissions: Set<String>, onComplete: (permissions: Map<String, Boolean>) -> Unit) {
    val validPermissions = PermissionUtils.filterStoragePermission(permissions.toTypedArray())
    PermissionRequester(validPermissions.toSet()).request(this, onComplete)
}

/**
 * Helper for requesting permissions
 */
class PermissionRequester(private val permissionStrings: Set<String>) {

    private lateinit var permissions: MutableMap<String, Boolean>
    private lateinit var missingPermissions: Map<String, Boolean>
    private lateinit var onComplete: (permissions: Map<String, Boolean>) -> Unit

    fun request(activity: Activity, onComplete: (permissions: Map<String, Boolean>) -> Unit) {
        this.onComplete = onComplete
        activity as? ActivityCompat.OnRequestPermissionsResultCallback
                ?: throw IllegalArgumentException("Provided activity must implement PermissionReceiver")
        permissions = permissionStrings.associate { it to activity.hasPermissions(it) }.toMutableMap()
        missingPermissions = permissions.filter { it.value == false }
        if (missingPermissions.isEmpty()) {
            onComplete(permissions)
        } else {
            EventBus.getDefault().register(this)
            ActivityCompat.requestPermissions(activity, missingPermissions.keys.toTypedArray(), REQUEST_CODE)
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onPermissionsResult(results: PermissionResult) {
        EventBus.getDefault().unregister(this)
        results.permissions.forEachIndexed { index, permission ->
            permissions[permission] = results.grantResults[index] == PackageManager.PERMISSION_GRANTED
        }
        onComplete(permissions)
    }

    companion object {
        const val REQUEST_CODE = 4434
    }

    data class PermissionResult(val requestCode: Int, val permissions: Array<out String>, val grantResults: IntArray)

}

/**
 * Helper for receiving permissions, to be used with [PermissionRequester] and [Activity.requestPermissions]
 */
class PermissionReceiver : ActivityCompat.OnRequestPermissionsResultCallback {

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // When nesting fragments, the request code from a permission request gets mangled (https://stackoverflow.com/questions/36170324/receive-incorrect-resultcode-in-activitys-onrequestpermissionsresult-when-reque/36186666)
        // using a bitwise '&'' with 0xff gets us the code we want
        if (requestCode != PermissionRequester.REQUEST_CODE && (requestCode and 0xff) != PermissionUtils.PERMISSION_REQUEST_CODE) return
        EventBus.getDefault().post(PermissionRequester.PermissionResult(requestCode, permissions, grantResults))
    }

}

