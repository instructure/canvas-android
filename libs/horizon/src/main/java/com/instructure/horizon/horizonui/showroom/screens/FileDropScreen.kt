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

package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.molecules.filedrop.FileDrop
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropBottomSheet
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState

@Composable
fun FileDropScreen() {
    var showBottomSheet by remember { mutableStateOf(false) }
    if (showBottomSheet) {
        FileDropBottomSheet(onDismiss = { showBottomSheet = false })
    }
    FileDrop(listOf("pdf", "jpg"), fileItems = {
        FileDropItem(state = FileDropItemState.InProgress("In progress file"))
        FileDropItem(state = FileDropItemState.Success("Success file"))
        FileDropItem(state = FileDropItemState.NoLongerEditable("No longer editable file"))
        FileDropItem(state = FileDropItemState.Error("Error text"))
    }, onUploadClick = {
        showBottomSheet = true
    })
}

@Composable
@Preview
fun FileDropScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDropScreen()
}