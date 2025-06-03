/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AddSubmissionFileUiState
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AddSubmissionTypeUiState
import com.instructure.horizon.horizonui.molecules.filedrop.FileDrop
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropBottomSheet
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropBottomSheetCallbacks
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState
import com.instructure.pandautils.utils.Const
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileSubmissionContent(uiState: AddSubmissionTypeUiState.File, submissionInProgress: Boolean, modifier: Modifier = Modifier) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var mediaUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            mediaUri?.let {
                uiState.onFileAdded(it)
            }
        }
    }

    val photoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showBottomSheet = false
                mediaUri = takePictureWithFileProvider(context, photoLauncher)
            }
        }
    )

    val onCameraPhotoClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            showBottomSheet = false
            mediaUri = takePictureWithFileProvider(context, photoLauncher)
        } else {
            photoPermissionLauncher.launch(cameraPermission)
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            mediaUri?.let {
                uiState.onFileAdded(it)
            }
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showBottomSheet = false
                mediaUri = takeVideoWithFileProvider(context, videoLauncher)
            }
        }
    )

    val onCameraVideoClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            showBottomSheet = false
            mediaUri = takeVideoWithFileProvider(context, videoLauncher)
        } else {
            videoPermissionLauncher.launch(cameraPermission)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uiState.onFileAdded(it) }
    }

    val onGalleryClick: () -> Unit = {
        showBottomSheet = false
        filePickerLauncher.launch("image/*")
    }

    if (showBottomSheet) {
        FileDropBottomSheet(
            onDismiss = { showBottomSheet = false }, callbacks = FileDropBottomSheetCallbacks(
                onChoosePhoto = if (uiState.galleryPickerAllowed) onGalleryClick else null,
                onTakePhoto = if (uiState.cameraAllowed) onCameraPhotoClick else null,
                onTakeVideo = if (uiState.cameraAllowed) onCameraVideoClick else null,
                onUploadFile = {
                    showBottomSheet = false
                    filePickerLauncher.launch("*/*")
                }
            )
        )
    }
    FileDrop(uiState.allowedTypes, fileItems = {
        uiState.files.forEach {
            val state = if (submissionInProgress) {
                FileDropItemState.InProgress(it.name)
            } else {
                FileDropItemState.Success(it.name, onActionClick = it.onDeleteClicked)
            }
            FileDropItem(state = state)
        }
    }, onUploadClick = {
        showBottomSheet = true
    }, modifier = modifier)
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
private fun AddFileSubmissionContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    AddFileSubmissionContent(
        uiState = AddSubmissionTypeUiState.File(
            cameraAllowed = true,
            galleryPickerAllowed = true,
            files = listOf(
                AddSubmissionFileUiState(name = "file1.jpg", onDeleteClicked = {}),
                AddSubmissionFileUiState(name = "file2.mp4", onDeleteClicked = {})
            ),
            onFileAdded = {}
        ),
        submissionInProgress = false
    )
}