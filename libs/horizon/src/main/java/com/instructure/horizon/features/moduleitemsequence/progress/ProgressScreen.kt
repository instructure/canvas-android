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
package com.instructure.horizon.features.moduleitemsequence.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCard
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardState
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.compose.composables.FullScreenDialog

@Composable
fun ProgressScreen(uiState: ProgressScreenUiState, modifier: Modifier = Modifier) {
    FullScreenDialog(onDismissRequest = uiState.onCloseClick) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = HorizonColors.Surface.pagePrimary(), shape = HorizonCornerRadius.level5)
                .padding(start = 24.dp, end = 24.dp)
        ) {
            val currentPage = uiState.pages[uiState.currentPosition]
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 24.dp, bottom = 16.dp)) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.list_alt), contentDescription = null, tint = HorizonColors.Icon.default())
                        HorizonSpace(SpaceSize.SPACE_8)
                        Text(text = stringResource(R.string.moduleProgressScreen_title), style = HorizonTypography.h3)
                    }
                }
                item {
                    Text(
                        text = currentPage.moduleName,
                        style = HorizonTypography.labelLargeBold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                items(currentPage.items.size) { index ->
                    when (val item = currentPage.items[index]) {
                        is ProgressPageItem.ModuleItem -> {
                            val selected = uiState.selectedModuleItemId == item.moduleItemId
                            ModuleItemCard(state = item.moduleItemCardState.copy(selected = selected))
                        }

                        is ProgressPageItem.SubHeader -> {
                            Text(
                                text = item.name,
                                style = HorizonTypography.p2,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            IconButton(
                iconRes = R.drawable.close,
                color = IconButtonColor.INVERSE,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp),
                elevation = HorizonElevation.level4,
                onClick = uiState.onCloseClick,
                size = IconButtonSize.SMALL
            )
            IconButton(
                iconRes = R.drawable.chevron_left,
                color = IconButtonColor.INVERSE,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp),
                elevation = HorizonElevation.level4,
                onClick = uiState.onPreviousClick
            )
            IconButton(
                iconRes = R.drawable.chevron_right,
                color = IconButtonColor.INVERSE,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp),
                elevation = HorizonElevation.level4,
                onClick = uiState.onNextClick
            )
        }
    }
}

@Composable
@Preview
private fun PreviewProgressScreen() {
    ContextKeeper.appContext = LocalContext.current
    ProgressScreen(
        uiState = ProgressScreenUiState(
            visible = true,
            loading = false,
            pages = listOf(
                ProgressPageUiState(
                    moduleName = "Module 1 longer name",
                    moduleId = 1L,
                    items = listOf(
                        ProgressPageItem.SubHeader("Subheader 1"),
                        ProgressPageItem.ModuleItem(1,
                            ModuleItemCardState(
                                "Assignment",
                                learningObjectType = LearningObjectType.ASSIGNMENT,
                                learningObjectStatus = LearningObjectStatus.REQUIRED
                            )
                        ),
                        ProgressPageItem.ModuleItem(2,
                            ModuleItemCardState(
                                "Page",
                                learningObjectType = LearningObjectType.PAGE,
                                learningObjectStatus = LearningObjectStatus.OPTIONAL
                            )
                        ),
                    )
                )
            ),
            currentPosition = 0,
            onCloseClick = {},
            onPreviousClick = {},
            onNextClick = {}
        )
    )
}