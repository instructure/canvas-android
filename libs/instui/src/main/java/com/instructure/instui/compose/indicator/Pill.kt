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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUIPill
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Pill colors resolved from tokens for a given variant.
 */
data class PillColors(
    val textColor: Color,
    val borderColor: Color,
)

/**
 * Pill status variant determining text and border colors.
 * Each variant carries its own composable color resolver.
 */
enum class PillVariant(val colors: @Composable () -> PillColors) {
    Default(colors = {
        PillColors(
            textColor = InstUIPill.baseTextColor,
            borderColor = InstUIPill.baseBorderColor,
        )
    }),
    Info(colors = {
        PillColors(
            textColor = InstUIPill.infoTextColor,
            borderColor = InstUIPill.infoBorderColor,
        )
    }),
    Error(colors = {
        PillColors(
            textColor = InstUIPill.errorTextColor,
            borderColor = InstUIPill.errorBorderColor,
        )
    }),
    Success(colors = {
        PillColors(
            textColor = InstUIPill.successTextColor,
            borderColor = InstUIPill.successBorderColor,
        )
    }),
    Warning(colors = {
        PillColors(
            textColor = InstUIPill.warningTextColor,
            borderColor = InstUIPill.warningBorderColor,
        )
    }),
}

private val PillTextStyle = TextStyle(
    fontFamily = InstUIPill.fontFamily,
    fontWeight = InstUIPill.textFontWeight,
    fontSize = InstUIPill.textFontSize,
)

/**
 * InstUI status pill / badge.
 *
 * Displays a short label with colored border indicating status.
 *
 * Usage:
 * ```
 * Pill(text = "Missing", variant = PillVariant.Error)
 * Pill(text = "Missing", variant = PillVariant.Error, icon = InstUIIcons.Line.Warning)
 * ```
 */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.Default,
    icon: ImageVector? = null,
) {
    val colors = variant.colors()
    val shape = RoundedCornerShape(InstUIPill.borderRadius)

    Box(
        modifier = modifier
            .height(InstUIPill.height)
            .widthIn(max = InstUIPill.maxWidth)
            .border(
                width = InstUIPill.borderWidth,
                color = colors.borderColor,
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
                Icon(
                    imageVector = icon,
                    tint = colors.textColor,
                    size = IconSize.Small,
                )
            }
            Text(
                text = text,
                style = PillTextStyle,
                color = colors.textColor,
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
                icon = InstUIIcons.Line.Warning,
            )
        }
    }
}