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

package com.instructure.ngc.features.submission.file

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.camera
import com.instructure.instui.token.icon.folder
import com.instructure.instui.token.icon.image
import com.instructure.instui.token.icon.video
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller returned by [rememberFilePicker]. Call [launch] to open the source sheet;
 * results are delivered via [onFilesPicked].
 */
class FilePickerController(
    internal val launch: () -> Unit,
)

/**
 * Remembers a [FilePickerController] that manages all four file-source launchers
 * (device files, camera photo, camera video, gallery) and presents a Tray source
 * sheet for the user to choose from.
 *
 * Embed the [FilePickerTray] composable at the call-site to host the sheet UI:
 * ```
 * val picker = rememberFilePicker { files -> vm.onFilesPicked(files) }
 * picker.FilePickerTray()
 * AddFileButton(onClick = picker::launch)
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFilePicker(
    onFilesPicked: (List<FileSubmitObject>) -> Unit,
): FilePickerController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    fun handleUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        scope.launch {
            val fsos = withContext(Dispatchers.IO) {
                uris.mapNotNull { FileUploadUtils.getFile(context, it) }
            }
            if (fsos.isNotEmpty()) onFilesPicked(fsos)
        }
    }

    val deviceFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> handleUris(uris) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> handleUris(uris) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = TakePictureWithWriteGrant()
    ) { success ->
        if (success) pendingCameraUri?.let { handleUris(listOf(it)) }
    }

    val photoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showSourceSheet = false
            pendingCameraUri = launchCameraPhoto(context, photoLauncher)
        }
    }

    val onTakePhoto: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showSourceSheet = false
            pendingCameraUri = launchCameraPhoto(context, photoLauncher)
        } else {
            photoPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = CaptureVideoWithWriteGrant()
    ) { success ->
        if (success) pendingCameraUri?.let { handleUris(listOf(it)) }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showSourceSheet = false
            pendingCameraUri = launchCameraVideo(context, videoLauncher)
        }
    }

    val onRecordVideo: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showSourceSheet = false
            pendingCameraUri = launchCameraVideo(context, videoLauncher)
        } else {
            videoPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val controller = remember { FilePickerController(launch = { showSourceSheet = true }) }

    if (showSourceSheet) {
        FileSourceTray(
            onDismiss = { showSourceSheet = false },
            onDeviceFiles = {
                showSourceSheet = false
                deviceFileLauncher.launch("*/*")
            },
            onTakePhoto = onTakePhoto,
            onRecordVideo = onRecordVideo,
            onChooseFromGallery = {
                showSourceSheet = false
                galleryLauncher.launch("image/*")
            },
        )
    }

    return controller
}

/**
 * Bottom tray presenting the four file-source options using InstUI [Tray].
 *
 * Split out so it can be previewed independently.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileSourceTray(
    onDismiss: () -> Unit,
    onDeviceFiles: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit,
    onChooseFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tray(onDismiss = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                    vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                ),
        ) {
            Heading(
                text = stringResource(R.string.ngc_submission_addFileSheetTitle),
                level = HeadingLevel.H3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            )
            Separator()
            FileSourceRow(
                icon = InstUIIcons.folder,
                label = stringResource(R.string.ngc_submission_sourceDeviceFiles),
                contentDescription = stringResource(R.string.a11y_submission_sourceDeviceFiles),
                onClick = onDeviceFiles,
            )
            Separator()
            FileSourceRow(
                icon = InstUIIcons.camera,
                label = stringResource(R.string.ngc_submission_sourceTakePhoto),
                contentDescription = stringResource(R.string.a11y_submission_sourceTakePhoto),
                onClick = onTakePhoto,
            )
            Separator()
            FileSourceRow(
                icon = InstUIIcons.video,
                label = stringResource(R.string.ngc_submission_sourceRecordVideo),
                contentDescription = stringResource(R.string.a11y_submission_sourceRecordVideo),
                onClick = onRecordVideo,
            )
            Separator()
            FileSourceRow(
                icon = InstUIIcons.image,
                label = stringResource(R.string.ngc_submission_sourceChooseFromGallery),
                contentDescription = stringResource(R.string.a11y_submission_sourceChooseFromGallery),
                onClick = onChooseFromGallery,
            )
        }
    }
}

@Composable
private fun FileSourceRow(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0))
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick)
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            color = IconColor.Base,
            size = IconSize.Large,
        )
        Text(
            text = label,
            style = InstUIText.content,
            color = InstUISemanticColors.Text.base(),
        )
    }
}

// The default capture contracts rely on the OS's implicit URI write-grant for EXTRA_OUTPUT, which is
// removed in Android 18+. Granting FLAG_GRANT_WRITE_URI_PERMISSION on the intent sets it explicitly.
internal class TakePictureWithWriteGrant : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent =
        super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

internal class CaptureVideoWithWriteGrant : ActivityResultContracts.CaptureVideo() {
    override fun createIntent(context: Context, input: Uri): Intent =
        super.createIntent(context, input).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

internal fun launchCameraPhoto(
    context: Context,
    launcher: ActivityResultLauncher<Uri>,
): Uri? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir ?: return null
    val file = File(cacheDir, "pic_${System.currentTimeMillis()}.jpg")
    val authority = context.packageName + Const.FILE_PROVIDER_AUTHORITY
    val uri = FileProvider.getUriForFile(context, authority, file)
    launcher.launch(uri)
    return uri
}

internal fun launchCameraVideo(
    context: Context,
    launcher: ActivityResultLauncher<Uri>,
): Uri? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir ?: return null
    val file = File(cacheDir, "vid_${System.currentTimeMillis()}.mp4")
    val authority = context.packageName + Const.FILE_PROVIDER_AUTHORITY
    val uri = FileProvider.getUriForFile(context, authority, file)
    launcher.launch(uri)
    return uri
}
