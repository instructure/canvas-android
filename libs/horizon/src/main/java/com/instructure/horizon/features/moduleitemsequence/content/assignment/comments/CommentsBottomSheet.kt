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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.comments

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.organisms.cards.CommentCard
import com.instructure.horizon.horizonui.organisms.cards.CommentCardState
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    uiState: CommentsUiState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null,
        modifier = modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {
        Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) {
            IconButton(
                iconRes = R.drawable.close,
                color = IconButtonColor.Inverse,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 8.dp),
                elevation = HorizonElevation.level4,
                onClick = onDismiss,
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
                    painterResource(R.drawable.chat),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default()
                )
                HorizonSpace(SpaceSize.SPACE_8)
                Text(
                    text = stringResource(R.string.commentsBottomSheet_header),
                    style = HorizonTypography.h3
                )
            }
        }
        HorizonSpace(SpaceSize.SPACE_24)
        if (uiState.loading) {
            Spinner(modifier = Modifier.fillMaxSize())
        } else {
            CommentsContent(uiState)
        }
    }
}

@Composable
private fun CommentsContent(uiState: CommentsUiState) {
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.comments.size, uiState.comment.text) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        uiState.comments.forEach { item ->
            CommentCard(item)
        }

        if (uiState.showPagingControls) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    iconRes = R.drawable.chevron_left,
                    color = IconButtonColor.White,
                    size = IconButtonSize.SMALL,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enabled = uiState.previousPageEnabled,
                    onClick = uiState.onPreviousPageClicked
                )
                IconButton(
                    iconRes = R.drawable.chevron_right,
                    color = IconButtonColor.White,
                    size = IconButtonSize.SMALL,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    enabled = uiState.nextPageEnabled,
                    onClick = uiState.onNextPageClicked
                )
            }
        }

        HorizonSpace(SpaceSize.SPACE_16)

        TextArea(
            state = TextAreaState(
                label = stringResource(R.string.commentsBottomSheet_addComment),
                required = InputLabelRequired.Regular,
                value = uiState.comment,
                onValueChange = uiState.onCommentChanged,
            ),
            minLines = 5
        )

        HorizonSpace(SpaceSize.SPACE_16)

        val alpha = if (uiState.comment.text.isNotBlank()) 1f else 0.5f
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color = HorizonColors.Surface.institution().copy(alpha = alpha), shape = HorizonCornerRadius.level6)
                    .animateContentSize()
            ) {
                if (uiState.postingComment) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level6)
                    ) {
                        Spinner(
                            size = SpinnerSize.EXTRA_SMALL,
                            color = HorizonColors.Surface.cardPrimary(),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                        )
                    }
                } else {
                    Button(
                        label = stringResource(R.string.commentsBottomSheet_post),
                        height = ButtonHeight.NORMAL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.Custom(
                            backgroundColor = Color.Transparent,
                            contentColor = HorizonColors.Text.surfaceColored()
                        ),
                        enabled = uiState.comment.text.isNotBlank(),
                        onClick = uiState.onPostClicked
                    )
                }
            }
        }

        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CommentsBottomSheetPreview() {
    ContextKeeper.appContext = LocalContext.current
    Surface(color = Color.White) {
        CommentsBottomSheet(
            modifier = Modifier.fillMaxSize(),
            uiState = CommentsUiState(
                comments = listOf(
                    CommentCardState(
                        title = "John Doe",
                        date = "2025-01-01",
                        subtitle = "Attempt 1",
                        commentText = "This is a sample comment text.",
                        read = true,
                        files = emptyList(),
                        fromCurrentUser = true
                    ),
                    CommentCardState(
                        title = "Jane Smith",
                        date = "2025-01-02",
                        subtitle = "Attempt 2",
                        commentText = "Another sample comment text.",
                        read = false,
                        files = emptyList(),
                        fromCurrentUser = false
                    )
                )
            ),
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
        )
    }
}