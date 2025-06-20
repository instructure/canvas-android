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

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.organisms.cards.CommentCardState

data class CommentsUiState(
    val loading: Boolean = false,
    val comments: List<CommentCardState> = emptyList(),
    val comment: TextFieldValue = TextFieldValue(""),
    val onCommentChanged: (TextFieldValue) -> Unit = {},
    val onPostClicked: () -> Unit = {},
    val showPagingControls: Boolean = false,
    val previousPageEnabled: Boolean = false,
    val nextPageEnabled: Boolean = false,
    val onPreviousPageClicked: () -> Unit = {},
    val onNextPageClicked: () -> Unit = {},
    val postingComment: Boolean = false,
    val filePathToOpen: String? = null,
    val mimeTypeToOpen: String? = null,
    val onFileOpened: () -> Unit = {}
)