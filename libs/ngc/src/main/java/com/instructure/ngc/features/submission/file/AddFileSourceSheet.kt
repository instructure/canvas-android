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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R

/**
 * Bottom tray presenting four file-source options for a file-upload submission.
 *
 * Delegates to [FileSourceTray] which is backed by InstUI [com.instructure.instui.compose.container.Tray].
 * Kept as a thin wrapper so existing call-sites in [NGCSubmissionScreen] do not need to change.
 *
 * @see rememberFilePicker for the recommended higher-level API that bundles launchers with this sheet.
 */
@Composable
fun AddFileSourceSheet(
    onDismiss: () -> Unit,
    onDeviceFiles: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit,
    onChooseFromGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FileSourceTray(
        onDismiss = onDismiss,
        onDeviceFiles = onDeviceFiles,
        onTakePhoto = onTakePhoto,
        onRecordVideo = onRecordVideo,
        onChooseFromGallery = onChooseFromGallery,
        modifier = modifier,
    )
}

// region Previews

@Preview(name = "AddFileSourceSheet — Light", showBackground = true)
@Composable
private fun AddFileSourceSheetPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            FileSourceTray(
                onDismiss = {},
                onDeviceFiles = {},
                onTakePhoto = {},
                onRecordVideo = {},
                onChooseFromGallery = {},
            )
        }
    }
}

@Preview(
    name = "AddFileSourceSheet — Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AddFileSourceSheetDarkPreview() {
    InstUITheme {
        Column(modifier = Modifier.background(InstUISemanticColors.Background.base())) {
            FileSourceTray(
                onDismiss = {},
                onDeviceFiles = {},
                onTakePhoto = {},
                onRecordVideo = {},
                onChooseFromGallery = {},
            )
        }
    }
}

// endregion
