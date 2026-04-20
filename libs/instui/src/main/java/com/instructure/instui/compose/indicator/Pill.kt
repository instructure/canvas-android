/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui.compose.indicator

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUIPill
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Assignment
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Pill status variant determining text and border colors.
 */
enum class PillVariant {
    Default,
    Info,
    Error,
    Success,
    Warning,
}

/**
 * InstUI status pill / badge.
 *
 * Displays a short label with colored border indicating status.
 *
 * Usage:
 * ```
 * Pill(text = "Missing", variant = PillVariant.Error)
 * Pill(text = "Submitted", variant = PillVariant.Success)
 * ```
 */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.Default,
    icon: (@Composable () -> Unit)? = null,
) {
    val textColor = when (variant) {
        PillVariant.Default -> InstUIPill.baseTextColor
        PillVariant.Info -> InstUIPill.infoTextColor
        PillVariant.Error -> InstUIPill.errorTextColor
        PillVariant.Success -> InstUIPill.successTextColor
        PillVariant.Warning -> InstUIPill.warningTextColor
    }

    val borderColor = when (variant) {
        PillVariant.Default -> InstUIPill.baseBorderColor
        PillVariant.Info -> InstUIPill.infoBorderColor
        PillVariant.Error -> InstUIPill.errorBorderColor
        PillVariant.Success -> InstUIPill.successBorderColor
        PillVariant.Warning -> InstUIPill.warningBorderColor
    }

    val shape = RoundedCornerShape(InstUIPill.borderRadius)
    val textStyle = TextStyle(
        fontFamily = InstUIPill.fontFamily,
        fontWeight = InstUIPill.textFontWeight,
        fontSize = InstUIPill.textFontSize,
    )

    Box(
        modifier = modifier
            .height(InstUIPill.height)
            .widthIn(max = InstUIPill.maxWidth)
            .border(
                width = InstUIPill.borderWidth,
                color = borderColor,
                shape = shape,
            )
            .background(
                color = InstUIPill.backgroundColor,
                shape = shape,
            )
            .padding(horizontal = InstUIPill.paddingHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                maxLines = 1,
            )
        }
    }
}

@Preview(name = "Pill Variants — Light", showBackground = true)
@Preview(name = "Pill Variants — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PillVariantsPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(text = "Default", variant = PillVariant.Default)
            Pill(text = "Info", variant = PillVariant.Info)
            Pill(text = "Missing", variant = PillVariant.Error)
            Pill(text = "Submitted", variant = PillVariant.Success)
            Pill(text = "Late", variant = PillVariant.Warning)
        }
    }
}

@Preview(name = "Pill with Icon — Light", showBackground = true)
@Preview(name = "Pill with Icon — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PillWithIconPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(
                text = "Missing",
                variant = PillVariant.Error,
                icon = {
                    Icon(
                        InstUIIcons.Line.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = InstUISemanticColors.Icon.error(),
                    )
                },
            )
        }
    }
}