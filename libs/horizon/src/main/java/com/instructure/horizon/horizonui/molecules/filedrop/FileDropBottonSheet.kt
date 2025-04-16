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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import kotlinx.coroutines.launch

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
    val localCoroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .background(color = HorizonColors.Surface.pagePrimary(), shape = HorizonCornerRadius.level5)
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 36.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.fileDropSheet_uploadFile),
                    style = HorizonTypography.h3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .align(Alignment.Center)
                )
                IconButton(
                    iconRes = R.drawable.close, color = IconButtonColor.INVERSE,
                    onClick = {
                        localCoroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    elevation = HorizonElevation.level4,
                )
            }
            HorizonSpace(SpaceSize.SPACE_24)
            Column(modifier = Modifier.background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)) {
                FileUploadRow(
                    label = stringResource(R.string.fileDropSheet_choosePhotoOrVideo),
                    iconRes = R.drawable.image,
                    onClick = callbacks.onChoosePhoto,
                    modifier = Modifier.clip(shape = HorizonCornerRadius.level3Top)
                )
                HorizontalDivider(thickness = 1.dp, color = HorizonColors.LineAndBorder.lineStroke())
                FileUploadRow(
                    label = stringResource(R.string.fileDropSheet_takePhotoOrVideo),
                    iconRes = R.drawable.camera,
                    onClick = callbacks.onTakePhoto
                )
                HorizontalDivider(thickness = 1.dp, color = HorizonColors.LineAndBorder.lineStroke())
                FileUploadRow(
                    label = stringResource(R.string.fileDropSheet_uploadFile),
                    iconRes = R.drawable.folder,
                    onClick = callbacks.onUploadFile,
                    modifier = Modifier.clip(shape = HorizonCornerRadius.level3Bottom)
                )
            }
        }
    }
}

@Composable
private fun FileUploadRow(label: String, @DrawableRes iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = HorizonTypography.p1,
            textAlign = TextAlign.Start
        )
        Icon(painter = painterResource(id = iconRes), contentDescription = null)
    }
}

@Composable
@Preview
fun FileDropBottomSheetPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDropBottomSheet(sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded))
}