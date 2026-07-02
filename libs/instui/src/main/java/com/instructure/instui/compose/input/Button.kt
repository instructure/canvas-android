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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIBaseButton
import com.instructure.instui.token.component.InstUISpinner
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.material3.Button as M3Button

/**
 * Color set for a [Button] variant, resolved from InstUI tokens.
 */
data class ButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
)

/**
 * InstUI button variant. Each variant resolves its colors from [InstUIBaseButton] tokens.
 */
enum class ButtonVariant(val colors: @Composable () -> ButtonColors) {
    Primary(colors = {
        ButtonColors(
            container = InstUIBaseButton.primaryBackground,
            content = InstUIBaseButton.primaryColor,
            disabledContainer = InstUIBaseButton.primaryDisabledBackgroundColor,
            disabledContent = InstUIBaseButton.primaryDisabledTextColor,
        )
    }),
    Secondary(colors = {
        ButtonColors(
            container = InstUIBaseButton.secondaryBackground,
            content = InstUIBaseButton.secondaryColor,
            disabledContainer = InstUIBaseButton.secondaryDisabledBackgroundColor,
            disabledContent = InstUIBaseButton.secondaryDisabledTextColor,
        )
    }),
    Success(colors = {
        ButtonColors(
            container = InstUIBaseButton.successBackground,
            content = InstUIBaseButton.successColor,
            disabledContainer = InstUIBaseButton.successDisabledBackgroundColor,
            disabledContent = InstUIBaseButton.successDisabledTextColor,
        )
    }),
}

/**
 * InstUI filled button.
 *
 * Usage:
 * ```
 * Button(text = "Submit assignment", onClick = { ... })
 * Button(text = "Resubmit", onClick = { ... }, variant = ButtonVariant.Secondary)
 * Button(text = "Submitting", onClick = { ... }, loading = true)
 * ```
 */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: ImageVector? = null,
    elevation: Dp = 0.dp,
) {
    val colors = variant.colors()
    M3Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = InstUIBaseButton.mediumHeight),
        shape = RoundedCornerShape(InstUIBaseButton.borderRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.container,
            contentColor = colors.content,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = elevation,
            focusedElevation = elevation,
            hoveredElevation = elevation,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(InstUIBaseButton.gapButtonContentMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (loading) {
                Spinner(size = IconSize.Small.dp, strokeWidth = InstUISpinner.strokeWidthXs, color = colors.content)
            } else if (icon != null) {
                Icon(imageVector = icon, tint = colors.content, size = IconSize.Small)
            }
            Text(
                text = text,
                style = InstUIText.contentImportant,
                color = if (enabled) colors.content else colors.disabledContent,
            )
        }
    }
}

@Preview(name = "Button — Light", showBackground = true)
@Preview(name = "Button — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ButtonPreview() {
    InstUITheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Button(text = "Submit", onClick = {})
            Button(text = "Disabled", onClick = {}, enabled = false)
            Button(text = "Loading", onClick = {}, loading = true)
        }
    }
}
