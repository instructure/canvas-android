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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.attempts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AttemptSelectorUiState
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.cards.AttemptCard
import com.instructure.horizon.horizonui.organisms.cards.AttemptCardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptSelectorBottomSheet(
    uiState: AttemptSelectorUiState,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = uiState.onDismiss,
        dragHandle = null,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {
        Column {
            Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                IconButton(
                    iconRes = R.drawable.close,
                    contentDescription = stringResource(R.string.a11y_close),
                    color = IconButtonColor.Inverse,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(vertical = 8.dp),
                    elevation = HorizonElevation.level4,
                    onClick = uiState.onDismiss,
                    size = IconButtonSize.SMALL
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 40.dp)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.history),
                        contentDescription = null,
                        tint = HorizonColors.Icon.default()
                    )
                    HorizonSpace(SpaceSize.SPACE_8)
                    Text(
                        text = stringResource(R.string.attemptBottomSheet_header),
                        style = HorizonTypography.h3
                    )
                }
            }
        }
        HorizonSpace(SpaceSize.SPACE_16)
        if (uiState.attempts.isEmpty()) {
            Text(
                stringResource(R.string.attemptBottomSheet_noAttempts),
                style = HorizonTypography.p1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.attempts) { item ->
                    AttemptCard(state = item, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AttemptSelectorBottomSheetPreview() {
    Surface(color = Color.White) {
        AttemptSelectorBottomSheet(
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
            uiState = AttemptSelectorUiState(
                attempts = listOf(
                    AttemptCardState(
                        attemptNumber = 1,
                        attemptTitle = "Attempt 1",
                        score = "8/10",
                        date = "2024-01-01",
                    ),
                    AttemptCardState(
                        attemptNumber = 2,
                        attemptTitle = "Attempt 2",
                        date = "2024-01-02",
                    )
                ),
                onDismiss = {}
            )
        )
    }
}