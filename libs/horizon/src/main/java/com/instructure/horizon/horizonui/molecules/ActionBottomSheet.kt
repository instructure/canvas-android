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

package com.instructure.horizon.horizonui.molecules

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
import kotlinx.coroutines.launch

@Composable
fun ActionBottomSheet(
    title: String,
    actions: List<BottomSheetActionState>,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
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
                    text = title,
                    style = HorizonTypography.h3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 48.dp)
                        .align(Alignment.Center)
                )
                IconButton(
                    iconRes = R.drawable.close, color = IconButtonColor.Inverse,
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
                actions.forEachIndexed { index, bottomSheetActionState ->
                    val clipModifier = when (index) {
                        0 -> Modifier.clip(shape = HorizonCornerRadius.level3Top)
                        actions.lastIndex -> Modifier.clip(shape = HorizonCornerRadius.level3Bottom)
                        else -> Modifier
                    }
                    BottomSheetAction(state = bottomSheetActionState, modifier = clipModifier)
                    if (index != actions.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = HorizonColors.LineAndBorder.lineStroke())
                    }
                }
            }
        }
    }
}

data class BottomSheetActionState(
    val label: String,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit = {})

@Composable
private fun BottomSheetAction(state: BottomSheetActionState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable { state.onClick() }
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = state.label,
            style = HorizonTypography.p1,
            textAlign = TextAlign.Start
        )
        Icon(painter = painterResource(id = state.iconRes), contentDescription = null)
    }
}

@Composable
@Preview
fun ActionBottomSheetPreview() {
    ContextKeeper.appContext = LocalContext.current
    ActionBottomSheet(
        title = "Title",
        actions = listOf(
            BottomSheetActionState("Label 1", R.drawable.check_circle_full),
            BottomSheetActionState("Label 2", R.drawable.check_circle_full),
            BottomSheetActionState("Label 3", R.drawable.check_circle_full)
        ),
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    )
}