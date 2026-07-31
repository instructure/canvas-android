/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */

package com.instructure.ngc.features.submission.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.FileUploadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller returned by [rememberMediaPicker]. Media is a single clip per submission, so a new
 * pick/capture/recording replaces any previously attached clip (delivered to the caller via the
 * `onClipReady` callback passed to [rememberMediaPicker]).
 *
 * Unlike the file picker, this controller renders no `Tray` of its own — the in-app audio and
 * video recorders are hosted inline by [MediaSubmissionContent] so the non-modal "additional info"
 * supporting pane stays visible over them. [chooseMediaFile] is exposed as a direct action for
 * that inline UI's secondary affordances; [requestAudioPermission]/[requestVideoPermission] let it
 * gate starting a recording on the `RECORD_AUDIO` / `CAMERA`+`RECORD_AUDIO` permissions
 * respectively — recording itself (both audio and video) happens entirely in-app, this controller
 * only owns permissions and the device-file picker.
 */
class MediaPickerController(
    val chooseMediaFile: () -> Unit,
    val requestAudioPermission: (onGranted: () -> Unit) -> Unit,
    val audioPermissionDenied: Boolean,
    val requestVideoPermission: (onGranted: () -> Unit) -> Unit,
    val videoPermissionDenied: Boolean,
)

/**
 * Pure permission-gating decision for [rememberMediaPicker]'s record actions, extracted out of the
 * `@Composable` closure so it can be unit tested without a Compose/Robolectric harness. Given the
 * current self-permission grant state, decides whether the caller can proceed immediately or must
 * launch an Android permission request for the returned permissions.
 */
internal sealed class MediaPermissionDecision {
    data object Proceed : MediaPermissionDecision()
    data class RequestPermissions(val permissions: List<String>) : MediaPermissionDecision()
}

/** Audio recording needs only `RECORD_AUDIO`. */
internal fun decideAudioPermissionRequest(isAudioGranted: Boolean): MediaPermissionDecision =
    if (isAudioGranted) {
        MediaPermissionDecision.Proceed
    } else {
        MediaPermissionDecision.RequestPermissions(listOf(Manifest.permission.RECORD_AUDIO))
    }

/**
 * Embedded video-with-audio needs both `CAMERA` and `RECORD_AUDIO` before the live preview/
 * recording can start, so both must be granted to proceed; otherwise both are (re-)requested
 * together so a grant only takes one prompt round-trip.
 */
internal fun decideVideoPermissionRequest(isCameraGranted: Boolean, isAudioGranted: Boolean): MediaPermissionDecision =
    if (isCameraGranted && isAudioGranted) {
        MediaPermissionDecision.Proceed
    } else {
        MediaPermissionDecision.RequestPermissions(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
    }

/**
 * Remembers a [MediaPickerController] that manages the media-source launchers (device media file,
 * `RECORD_AUDIO` permission, `CAMERA`+`RECORD_AUDIO` permissions) needed by the inline media
 * submission body. Neither audio nor video capture launches a system camera/recorder intent —
 * both are recorded in-app (see [AudioRecorderHolder] and [VideoRecorderHolder]); this controller
 * only gates the permissions they need.
 *
 * ```
 * val picker = rememberMediaPicker(onClipReady = vm::onMediaClipReady)
 * MediaSubmissionContent(..., onChooseMediaFile = picker.chooseMediaFile, ...)
 * ```
 */
@Composable
fun rememberMediaPicker(
    onClipReady: (FileSubmitObject) -> Unit,
): MediaPickerController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var audioPermissionDenied by rememberSaveable { mutableStateOf(false) }
    var videoPermissionDenied by rememberSaveable { mutableStateOf(false) }
    // Not saveable — a lambda can't be persisted across process death. Losing a pending grant
    // callback across a config change just means the user taps Record once more, same as any
    // other in-flight permission request would.
    var pendingAudioGrantCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingVideoGrantCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun handleUri(uri: Uri?) {
        uri ?: return
        scope.launch {
            val fso = withContext(Dispatchers.IO) { FileUploadUtils.getFile(context, uri) }
            if (fso != null) onClipReady(fso)
        }
    }

    val chooseMediaFileLauncher = rememberLauncherForActivityResult(
        contract = GetMediaContent()
    ) { uri: Uri? -> handleUri(uri) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioPermissionDenied = !isGranted
        if (isGranted) {
            pendingAudioGrantCallback?.invoke()
        }
        pendingAudioGrantCallback = null
    }

    // Embedded video-with-audio needs both permissions before the live preview/recording can
    // start, unlike the old system-camera-intent path which only needed CAMERA (the intent's own
    // app owned RECORD_AUDIO). Requested together so a grant only takes one prompt round-trip.
    val videoPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.CAMERA] == true && grants[Manifest.permission.RECORD_AUDIO] == true
        videoPermissionDenied = !granted
        if (granted) {
            pendingVideoGrantCallback?.invoke()
        }
        pendingVideoGrantCallback = null
    }

    // Recreated each recomposition (cheap — just wraps the launchers/lambdas above, which
    // themselves close over the mutable state) so the permission-denied flags are always current.
    return MediaPickerController(
        chooseMediaFile = { chooseMediaFileLauncher.launch("*/*") },
        requestAudioPermission = { onGranted ->
            audioPermissionDenied = false
            val audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            when (val decision = decideAudioPermissionRequest(audioGranted)) {
                MediaPermissionDecision.Proceed -> onGranted()
                is MediaPermissionDecision.RequestPermissions -> {
                    pendingAudioGrantCallback = onGranted
                    audioPermissionLauncher.launch(decision.permissions.first())
                }
            }
        },
        audioPermissionDenied = audioPermissionDenied,
        requestVideoPermission = { onGranted ->
            videoPermissionDenied = false
            val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            when (val decision = decideVideoPermissionRequest(cameraGranted, audioGranted)) {
                MediaPermissionDecision.Proceed -> onGranted()
                is MediaPermissionDecision.RequestPermissions -> {
                    pendingVideoGrantCallback = onGranted
                    videoPermissionsLauncher.launch(decision.permissions.toTypedArray())
                }
            }
        },
        videoPermissionDenied = videoPermissionDenied,
    )
}

// GetContent() only accepts a single mime type, which can't express "audio OR video". Setting
// EXTRA_MIME_TYPES on top of the wildcard type additionally filters the system picker to media on
// devices that honor it. This is advisory only (some pickers ignore it) — callers must still
// validate the resolved contentType defensively (see NGCSubmissionViewModel.onMediaClipReady).
internal class GetMediaContent : ActivityResultContracts.GetContent() {
    override fun createIntent(context: Context, input: String): Intent =
        super.createIntent(context, input).putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*", "video/*"))
}
