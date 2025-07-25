/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.inbox.attachment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropBottomSheet
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropBottomSheetCallbacks
import com.instructure.pandautils.utils.Const
import java.io.File

@Composable
fun HorizonInboxAttachmentPicker(
    showBottomSheet: Boolean,
    onDismissBottomSheet: () -> Unit,
    state: HorizonInboxAttachmentPickerUiState,
    onFilesChanged: (List<HorizonInboxAttachment>) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    LaunchedEffect(state.files) {
        onFilesChanged(state.files)
    }

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var mediaUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            mediaUri?.let {
                state.onFileSelected(it)
            }
        }
    }

    val photoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onDismissBottomSheet()
                mediaUri = takePictureWithFileProvider(context, photoLauncher)
            }
        }
    )

    val onCameraPhotoClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            onDismissBottomSheet()
            mediaUri = takePictureWithFileProvider(context, photoLauncher)
        } else {
            photoPermissionLauncher.launch(cameraPermission)
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            mediaUri?.let {
                state.onFileSelected(it)
            }
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onDismissBottomSheet()
                mediaUri = takeVideoWithFileProvider(context, videoLauncher)
            }
        }
    )

    val onCameraVideoClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            onDismissBottomSheet()
            mediaUri = takeVideoWithFileProvider(context, videoLauncher)
        } else {
            videoPermissionLauncher.launch(cameraPermission)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { state.onFileSelected(it) }
    }

    val onGalleryClick: () -> Unit = {
        onDismissBottomSheet()
        filePickerLauncher.launch("image/*")
    }

    if (showBottomSheet) {
        FileDropBottomSheet(
            onDismiss = onDismissBottomSheet,
            callbacks = FileDropBottomSheetCallbacks(
                onChoosePhoto = onGalleryClick,
                onTakePhoto = onCameraPhotoClick,
                onTakeVideo = onCameraVideoClick,
                onUploadFile = {
                    onDismissBottomSheet()
                    filePickerLauncher.launch("*/*")
                }
            ),
            sheetState = sheetState
        )
    }
}

private fun takePictureWithFileProvider(
    context: Context,
    launcher: ActivityResultLauncher<Uri>
): Uri {
    val fileName = "pic_${System.currentTimeMillis()}.jpg"
    val file = File(context.externalCacheDir, fileName)
    val authority = context.packageName + Const.FILE_PROVIDER_AUTHORITY
    val uri = FileProvider.getUriForFile(context, authority, file)
    launcher.launch(uri)
    return uri
}

private fun takeVideoWithFileProvider(
    context: Context,
    launcher: ActivityResultLauncher<Uri>
): Uri {
    val fileName = "vid_${System.currentTimeMillis()}.mp4"
    val file = File(context.externalCacheDir, fileName)
    val authority = context.packageName + Const.FILE_PROVIDER_AUTHORITY
    val uri = FileProvider.getUriForFile(context, authority, file)
    launcher.launch(uri)
    return uri
}

@Composable
@Preview
private fun HorizonInboxAttachmentPickerPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = HorizonInboxAttachmentPickerUiState(
        files = emptyList(),
        onFileSelected = {}
    )
    HorizonInboxAttachmentPicker(
        showBottomSheet = true,
        onDismissBottomSheet = {},
        state = state,
        onFilesChanged = {},
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    )
}