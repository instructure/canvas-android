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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
fun AddFileSubmissionContent(uiState: AddSubmissionTypeUiState.File, modifier: Modifier = Modifier) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermission = Manifest.permission.CAMERA
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                uiState.onFileAdded(it)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showBottomSheet = false
                imageUri = takePictureWithFileProvider(context, launcher)
            }
        }
    )

    val onCameraClick: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            showBottomSheet = false
            imageUri = takePictureWithFileProvider(context, launcher)
        } else {
            permissionLauncher.launch(cameraPermission)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uiState.onFileAdded(it) }
    }

    if (showBottomSheet) {
        FileDropBottomSheet(
            onDismiss = { showBottomSheet = false }, callbacks = FileDropBottomSheetCallbacks(
                onChoosePhoto = {
                    showBottomSheet = false
                    filePickerLauncher.launch("image/*")
                },
                onTakePhoto = onCameraClick,
                onUploadFile = {
                    showBottomSheet = false
                    filePickerLauncher.launch("*/*")
                }
            )
        )
    }
    FileDrop(uiState.allowedTypes, fileItems = {
        uiState.files.forEach {
            FileDropItem(state = FileDropItemState.Success(it.name, onActionClick = it.onDeleteClicked))
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