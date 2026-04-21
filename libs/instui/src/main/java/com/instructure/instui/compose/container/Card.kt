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

package com.instructure.instui.compose.container

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.semantic.InstUIElevation
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Maps InstUI elevation levels to Compose elevation values.
 *
 * InstUI defines dual CSS-style shadows per level; Compose uses a single
 * Dp elevation. The mapping uses the primary shadow's Y offset as the
 * closest approximation.
 */
enum class Elevation(val dp: Dp) {
    None(0.dp),
    Level1(InstUIElevation.Level1.shadow1Y),
    Level2(InstUIElevation.Level2.shadow1Y),
    Level3(InstUIElevation.Level3.shadow1Y),
    Level4(InstUIElevation.Level4.shadow1Y),
}

/**
 * InstUI card container.
 *
 * A surface with rounded corners, optional border, and elevation.
 *
 * Usage:
 * ```
 * Card {
 *     Text(text = "Card content")
 * }
 * Card(elevation = Elevation.Level2) {
 *     Text(text = "Elevated card")
 * }
 * Card(borderColor = InstUISemanticColors.Stroke.info()) {
 *     Text(text = "With border")
 * }
 * ```
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    backgroundColor: Color = InstUISemanticColors.Background.container(),
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = InstUILayoutSizes.BorderWidth.Sm.sm,
    cornerRadius: Dp = InstUISharedTokens.BorderRadius.Card.md,
    elevation: Elevation = Elevation.None,
    contentPadding: Dp = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = if (borderColor != Color.Transparent) {
        BorderStroke(borderWidth, borderColor)
    } else {
        null
    }

    androidx.compose.material3.Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        border = border,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview(name = "Card — Light", showBackground = true)
@Preview(name = "Card — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.page())
                .padding(16.dp)
        ) {
            Card {
                Heading(text = "Card Title", level = HeadingLevel.H4)
                Text(text = "Card body content goes here.")
            }
        }
    }
}

@Preview(name = "Card Elevated — Light", showBackground = true)
@Preview(name = "Card Elevated — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardElevatedPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.page())
                .padding(16.dp)
        ) {
            Card(elevation = Elevation.Level1) {
                Text(text = "Elevated card (Level 1)")
            }
            Column(Modifier.padding(top = 12.dp)) {
                Card(elevation = Elevation.Level3) {
                    Text(text = "Elevated card (Level 3)")
                }
            }
        }
    }
}

@Preview(name = "Card with Border — Light", showBackground = true)
@Preview(name = "Card with Border — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CardWithBorderPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.page())
                .padding(16.dp)
        ) {
            Card(borderColor = InstUISemanticColors.Stroke.base()) {
                Text(text = "Card with visible border")
            }
        }
    }
}