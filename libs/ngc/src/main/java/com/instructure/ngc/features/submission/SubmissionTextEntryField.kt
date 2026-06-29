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

package com.instructure.ngc.features.submission

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.ngc.R
import com.instructure.pandautils.compose.composables.rce.ComposeRCE
import com.instructure.pandautils.compose.composables.rce.RceControlsPosition
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isHtmlBlank

/**
 * A bordered rich-content-editor field for text entry submissions.
 *
 * Wraps [ComposeRCE] inside a rounded border using InstUI TextInput tokens.
 * When [html] is blank and the editor is not focused, a placeholder overlay is shown
 * (mirroring the Horizon AddTextSubmissionContent pattern).
 *
 * [contentResetKey] is bumped by the ViewModel on external resets only (initial load, delete
 * draft, submit success). The editor is re-seeded from [html] on each key change, but NOT on
 * every keystroke — this prevents the caret from jumping while the user types.
 */
@Composable
fun SubmissionTextEntryField(
    html: String,
    contentResetKey: Int,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    // Seed the editor only when contentResetKey changes (external reset), not on every keystroke.
    // Keying on contentResetKey rather than html means the RCE is not re-applied per keystroke,
    // so the caret position is preserved while the user types.
    val seedHtml = remember(contentResetKey) { html }
    var currentText by remember(contentResetKey) { mutableStateOf(html) }
    // The RCE leaves residual markup (e.g. "<br>", "<p></p>") for a visually-empty field;
    // use the shared isHtmlBlank helper so the placeholder check stays consistent with the VM.
    val isEmpty = isHtmlBlank(currentText)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = InstUITextInput.borderWidth,
                color = InstUITextInput.borderColor,
                shape = RoundedCornerShape(InstUITextInput.borderRadius),
            ),
        contentAlignment = Alignment.TopStart,
    ) {
        ComposeRCE(
            html = seedHtml,
            // Give the editor a fixed height so it fills the bordered area instead of
            // collapsing to a single content line.
            fixedHeightInDp = TEXT_ENTRY_HEIGHT_DP,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                    end = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                    top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                    bottom = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                ),
            onTextChangeListener = {
                currentText = it
                onTextChanged(it)
            },
            onRceFocused = { isFocused = true },
            rceControlsPosition = RceControlsPosition.BOTTOM,
            rceDialogThemeColor = ThemePrefs.brandColor,
            rceDialogButtonColor = ThemePrefs.brandColor,
        )

        if (isEmpty && !isFocused) {
            Text(
                text = stringResource(R.string.ngc_submission_textEntryPlaceholder),
                style = InstUIText.content,
                color = InstUITextInput.placeholderColor,
                modifier = Modifier.padding(
                    horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                    vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                ),
            )
        }
    }
}

private const val TEXT_ENTRY_HEIGHT_DP = 200
