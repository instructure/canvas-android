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

package com.instructure.instui.compose.text

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.component.InstUIHeading
import com.instructure.instui.component.InstUIText as InstUITextTokens
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.semantic.InstUISemanticColors

/**
 * InstUI text component.
 *
 * Renders text using InstUI design system typography and colors.
 * Uses [InstUITextTokens.content] as the default style.
 *
 * Usage:
 * ```
 * Text(text = "Hello world")
 * Text(text = "Important", style = InstUITextTokens.contentImportant)
 * Text(text = "Small print", style = InstUITextTokens.contentSmall)
 * ```
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = InstUITextTokens.content,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}

/**
 * InstUI text component with [AnnotatedString] support.
 */
@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = InstUITextTokens.content,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}

/**
 * Heading level for [Heading] composable.
 */
enum class HeadingLevel {
    H1, H2, H3, H4, H5, H6
}

/**
 * InstUI heading component.
 *
 * Renders headings using Inclusive Sans font family with appropriate
 * size and weight per level.
 *
 * Usage:
 * ```
 * Heading(text = "Page Title", level = HeadingLevel.H1)
 * Heading(text = "Section", level = HeadingLevel.H3)
 * ```
 */
@Composable
fun Heading(
    text: String,
    level: HeadingLevel,
    modifier: Modifier = Modifier,
    color: Color = InstUISemanticColors.Text.base(),
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val style = when (level) {
        HeadingLevel.H1 -> InstUIHeading.titlePageMobile
        HeadingLevel.H2 -> InstUIHeading.titleSection
        HeadingLevel.H3 -> InstUIHeading.titleCardRegular
        HeadingLevel.H4 -> InstUIHeading.titleCardMini
        HeadingLevel.H5 -> InstUIHeading.label
        HeadingLevel.H6 -> InstUIHeading.label
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
    )
}

@Preview(name = "Text Styles — Light", showBackground = true)
@Preview(name = "Text Styles — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextStylesPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            Text(text = "Description Page", style = InstUITextTokens.descriptionPage)
            Spacer(Modifier.height(8.dp))
            Text(text = "Content (default)", style = InstUITextTokens.content)
            Spacer(Modifier.height(8.dp))
            Text(text = "Content Important", style = InstUITextTokens.contentImportant)
            Spacer(Modifier.height(8.dp))
            Text(text = "Content Small", style = InstUITextTokens.contentSmall)
            Spacer(Modifier.height(8.dp))
            Text(text = "Legend", style = InstUITextTokens.legend)
        }
    }
}

@Preview(name = "Text Colors — Light", showBackground = true)
@Preview(name = "Text Colors — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextColorsPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            Text(text = "Base color", color = InstUITextTokens.baseColor)
            Spacer(Modifier.height(8.dp))
            Text(text = "Muted color", color = InstUITextTokens.mutedColor)
            Spacer(Modifier.height(8.dp))
            Text(text = "Error color", color = InstUITextTokens.errorColor)
            Spacer(Modifier.height(8.dp))
            Text(text = "Success color", color = InstUITextTokens.successColor)
            Spacer(Modifier.height(8.dp))
            Text(text = "Warning color", color = InstUITextTokens.warningColor)
        }
    }
}

@Preview(name = "Headings — Light", showBackground = true)
@Preview(name = "Headings — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HeadingsPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            Heading(text = "Heading H1", level = HeadingLevel.H1)
            Spacer(Modifier.height(8.dp))
            Heading(text = "Heading H2", level = HeadingLevel.H2)
            Spacer(Modifier.height(8.dp))
            Heading(text = "Heading H3", level = HeadingLevel.H3)
            Spacer(Modifier.height(8.dp))
            Heading(text = "Heading H4", level = HeadingLevel.H4)
            Spacer(Modifier.height(8.dp))
            Heading(text = "Heading H5", level = HeadingLevel.H5)
            Spacer(Modifier.height(8.dp))
            Heading(text = "Heading H6", level = HeadingLevel.H6)
        }
    }
}