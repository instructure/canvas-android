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
package com.instructure.horizon.features.moduleitemsequence.content.page

import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.horizonui.platform.LoadingState

data class PageDetailsUiState(
    val loadingState: LoadingState = LoadingState(),
    val pageHtmlContent: String? = null,
    val ltiButtonPressed: ((String) -> Unit)? = null,
    val urlToOpen: String? = null,
    val onUrlOpened: () -> Unit = {},
    val notes: List<Note> = emptyList(),
    val courseId: Long = -1L,
    val pageId: Long = -1L,
    val pageUrl: String = ""
)