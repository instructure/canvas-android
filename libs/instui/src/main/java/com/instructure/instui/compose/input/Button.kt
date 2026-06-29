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

package com.instructure.instui.compose.input

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIBaseButton
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.primitives.InstUISizes
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Button as M3Button

/**
 * InstUI filled primary button component.
 *
 * Renders a filled primary button using InstUI design system tokens.
 * Width is controlled by the caller via [modifier]; use `Modifier.fillMaxWidth()`
 * for full-width or leave unset for wrap-content.
 *
 * When [loading] is true, a centered circular progress indicator replaces the
 * label and clicks are disabled.
 *
 * Usage:
 * ```
 * Button(text = "Submit", onClick = { submit() })
 * Button(text = "Submit", onClick = { submit() }, modifier = Modifier.fillMaxWidth())
 * Button(text = "Submit", onClick = { submit() }, enabled = false)
 * Button(text = "Submit", onClick = { submit() }, loading = true)
 * ```
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val contentColor = InstUIBaseButton.primaryColor

    M3Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(InstUIBaseButton.mediumHeight),
        shape = RoundedCornerShape(InstUIBaseButton.borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = InstUIBaseButton.primaryBackground,
            contentColor = contentColor,
            disabledContainerColor = InstUIBaseButton.primaryDisabledBackgroundColor,
            disabledContentColor = InstUIBaseButton.primaryDisabledTextColor,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                // InstUISizes.size20 = 20.dp (interactive xxs icon size)
                modifier = Modifier.size(InstUISizes.size20),
                // No stroke token available; keeping 2.dp as a thin-ring convention for
                // spinner-in-button. Matches InstUILayoutSizes.BorderWidth.Interactive.focus
                strokeWidth = InstUILayoutSizes.BorderWidth.Interactive.focus,
            )
        } else {
            Text(
                text = text,
                style = InstUIText.contentImportant,
                color = contentColor,
            )
        }
    }
}

@Preview(name = "Button — Light", showBackground = true)
@Preview(name = "Button — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ButtonPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Button(text = "Submit", onClick = {})
            Button(text = "Submit", onClick = {}, enabled = false)
            Button(text = "Submit", onClick = {}, loading = true)
        }
    }
}
