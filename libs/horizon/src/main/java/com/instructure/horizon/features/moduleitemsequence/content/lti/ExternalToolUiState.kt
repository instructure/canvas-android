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
package com.instructure.horizon.features.moduleitemsequence.content.lti

data class ExternalToolUiState(
    val previewUrl: String = "",
    val urlToOpen: String = "",
    val previewState: PreviewState = PreviewState.LOADING,
    val onOpenExternallyClicked: () -> Unit = {},
    val onPreviewError: () -> Unit = {},
    val onPageFinished: () -> Unit = {},
    val openExternallyLoading: Boolean = false,
    val authenticatedUrl: String? = null
)

enum class PreviewState {
    LOADING,
    ERROR,
    SUCCESS
}