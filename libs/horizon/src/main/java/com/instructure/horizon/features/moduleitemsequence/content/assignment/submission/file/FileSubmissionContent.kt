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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.file

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.instructure.horizon.R
import com.instructure.horizon.features.account.filepreview.FilePreview
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItem
import com.instructure.horizon.horizonui.molecules.filedrop.FileDropItemState

@UnstableApi
@Composable
fun FileSubmissionContent(
    uiState: FileSubmissionContentUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.check_circle_full), contentDescription = null, tint = HorizonColors.Icon.success())
            Spacer(Modifier.size(6.dp))
            Text(text = stringResource(R.string.assignmentDetails_fileSubmission), style = HorizonTypography.h3)
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Column {
            uiState.files.forEach { file ->
                val borderColor = if (file.selected) HorizonColors.Surface.institution() else HorizonColors.LineAndBorder.lineStroke()
                FileDropItem(
                    state = FileDropItemState.NoLongerEditable(
                        fileName = file.fileName,
                        onActionClick = file.onDownloadClick,
                        onClick = file.onClick
                    ), borderColor = borderColor
                )
            }
            HorizonSpace(SpaceSize.SPACE_8)
            if (uiState.filePreview != null) {
                FilePreview(filePreviewUiState = uiState.filePreview, modifier = Modifier.fillMaxWidth().then(
                    if (uiState.filePreview is FilePreviewUiState.Text) Modifier.fillMaxHeight() else Modifier.heightIn(0.dp, 400.dp)
                ))
            }
        }
    }
}