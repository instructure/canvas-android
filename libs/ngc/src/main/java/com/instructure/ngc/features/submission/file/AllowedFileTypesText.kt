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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R

/**
 * Renders a single legend-style helper line listing the allowed file extensions,
 * e.g. "Allowed: PDF, DOCX, PNG". Renders nothing when [allowedExtensions] is empty
 * (meaning the assignment accepts any file type).
 */
@Composable
fun AllowedFileTypesText(allowedExtensions: List<String>) {
    if (allowedExtensions.isEmpty()) return
    Text(
        text = stringResource(
            R.string.ngc_submission_allowedFileTypes,
            allowedExtensions.joinToString(separator = ", ") { it.uppercase() },
        ),
        style = InstUIText.legend,
        color = InstUISemanticColors.Text.muted(),
    )
}
