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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.horizonui.molecules.filedrop

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.ActionBottomSheet
import com.instructure.horizon.horizonui.molecules.BottomSheetActionState

data class FileDropBottomSheetCallbacks(
    val onChoosePhoto: () -> Unit = {},
    val onTakePhoto: () -> Unit = {},
    val onUploadFile: () -> Unit = {}
)

@Composable
fun FileDropBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    callbacks: FileDropBottomSheetCallbacks = FileDropBottomSheetCallbacks(),
    onDismiss: () -> Unit = {}
) {
    ActionBottomSheet(
        title = stringResource(R.string.fileDropSheet_uploadFile), actions = listOf(
            BottomSheetActionState(
                label = stringResource(R.string.fileDropSheet_choosePhotoOrVideo),
                iconRes = R.drawable.image,
                onClick = callbacks.onChoosePhoto
            ),
            BottomSheetActionState(
                label = stringResource(R.string.fileDropSheet_takePhotoOrVideo),
                iconRes = R.drawable.camera,
                onClick = callbacks.onTakePhoto
            ),
            BottomSheetActionState(
                label = stringResource(R.string.fileDropSheet_uploadFile),
                iconRes = R.drawable.folder,
                onClick = callbacks.onUploadFile
            )
        ), sheetState = sheetState, onDismiss = onDismiss, modifier = modifier
    )
}

@Composable
@Preview
fun FileDropBottomSheetPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDropBottomSheet(sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded))
}