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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIIcon
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.search
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Size variants for a [SearchInput] field.
 *
 * Each size carries token-backed height, font size, horizontal padding, and icon size.
 */
enum class SearchInputSize(
    val height: @Composable () -> Dp,
    val fontSize: @Composable () -> TextUnit,
    val horizontalPadding: @Composable () -> Dp,
    val leadingIconSize: IconSize,
) {
    /** Compact 32 dp pill — leading icon 16 dp (IconSize.Small). */
    Sm(
        height = { InstUITextInput.heightSm },
        fontSize = { InstUITextInput.fontSizeSm },
        horizontalPadding = { InstUITextInput.paddingHorizontalSm },
        leadingIconSize = IconSize.Small,
    ),

    /** Standard 40 dp pill — leading icon 20 dp (IconSize.Medium). */
    Md(
        height = { InstUITextInput.heightMd },
        fontSize = { InstUITextInput.fontSizeMd },
        horizontalPadding = { InstUITextInput.paddingHorizontalMd },
        leadingIconSize = IconSize.Medium,
    ),

    /** Large 48 dp pill — leading icon 24 dp (IconSize.Large). */
    Lg(
        height = { InstUITextInput.heightLg },
        fontSize = { InstUITextInput.fontSizeLg },
        horizontalPadding = { InstUITextInput.paddingHorizontalLg },
        leadingIconSize = IconSize.Large,
    ),
}

/**
 * InstUI SearchInput component. Pill-shaped (full border-radius) search field per Figma
 * `27143-103981`.
 *
 * Features a leading magnifier icon ([InstUIIcons.search]), optional placeholder text, and an
 * optional trailing icon (e.g. [InstUIIcons.x] to clear the query) with a click handler.
 * Three sizes are available via [SearchInputSize]: [SearchInputSize.Sm] (compact 32 dp),
 * [SearchInputSize.Md] (standard 40 dp), and [SearchInputSize.Lg] (large 48 dp). The text
 * cursor uses the icon "info" color per the Figma spec.
 *
 * Colors are drawn exclusively from [InstUITextInput] tokens; the pill shape is produced by
 * [InstUILayoutSizes.BorderRadius.Full.full].
 *
 * Usage:
 * ```
 * SearchInput(
 *     value = query,
 *     onValueChange = { query = it },
 *     placeholder = "Search courses…",
 *     trailingIcon = if (query.isNotEmpty()) InstUIIcons.x else null,
 *     onTrailingIconClick = { query = "" },
 * )
 * ```
 */
@Composable
fun SearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    size: SearchInputSize = SearchInputSize.Sm,
    placeholder: String? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val height = size.height()
    val fontSize = size.fontSize()
    val horizontalPadding = size.horizontalPadding()
    val iconGap = InstUILayoutSizes.Spacing.SpaceSm.spaceSm

    val backgroundColor = when {
        !enabled -> InstUITextInput.backgroundDisabledColor
        else -> InstUITextInput.backgroundColor
    }
    val borderColor = when {
        !enabled -> InstUITextInput.borderDisabledColor
        else -> InstUITextInput.borderColor
    }
    val textColor = when {
        !enabled -> InstUITextInput.textDisabledColor
        else -> InstUITextInput.textColor
    }
    val caretColor = InstUIIcon.infoColor
    val disabledIconTint = InstUITextInput.textDisabledColor

    val textStyle = TextStyle(
        color = textColor,
        fontFamily = InstUITextInput.fontFamily,
        fontWeight = InstUITextInput.fontWeight,
        fontSize = fontSize,
    )

    val interactionSource = remember { MutableInteractionSource() }
    val pillShape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Full.full)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(pillShape)
            .background(backgroundColor)
            .border(
                width = InstUITextInput.borderWidth,
                color = borderColor,
                shape = pillShape,
            ),
        enabled = enabled,
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(caretColor),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .defaultMinSize(minHeight = height)
                    .padding(horizontal = horizontalPadding),
            ) {
                // Leading magnifier icon
                if (enabled) {
                    Icon(
                        imageVector = InstUIIcons.search,
                        size = size.leadingIconSize,
                        color = IconColor.Muted,
                        modifier = Modifier.padding(end = iconGap),
                    )
                } else {
                    Icon(
                        imageVector = InstUIIcons.search,
                        tint = disabledIconTint,
                        size = size.leadingIconSize,
                        modifier = Modifier.padding(end = iconGap),
                    )
                }

                // Text content: placeholder overlay + innerTextField in the same Box
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = InstUIText.content.copy(
                                color = InstUITextInput.placeholderColor,
                                fontSize = fontSize,
                            ),
                        )
                    }
                    innerTextField()
                }

                // Optional trailing icon (e.g. clear button)
                if (trailingIcon != null) {
                    val trailingModifier = Modifier
                        .padding(start = iconGap)
                        .let { base ->
                            if (enabled && onTrailingIconClick != null) {
                                base.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onTrailingIconClick,
                                )
                            } else {
                                base
                            }
                        }

                    if (enabled) {
                        Icon(
                            imageVector = trailingIcon,
                            size = size.leadingIconSize,
                            color = IconColor.Muted,
                            modifier = trailingModifier,
                        )
                    } else {
                        Icon(
                            imageVector = trailingIcon,
                            tint = disabledIconTint,
                            size = size.leadingIconSize,
                            modifier = trailingModifier,
                        )
                    }
                }
            }
        },
    )
}

// region Previews

@Preview(name = "SearchInput Sm — Light", showBackground = true)
@Preview(name = "SearchInput Sm — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchInputSmPreview() {
    InstUITheme {
        var query by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Sm — placeholder
            SearchInput(
                value = "",
                onValueChange = {},
                size = SearchInputSize.Sm,
                placeholder = "Search…",
                modifier = Modifier.fillMaxWidth(),
            )
            // Sm — filled with trailing clear icon
            SearchInput(
                value = "Biology 101",
                onValueChange = { query = it },
                size = SearchInputSize.Sm,
                placeholder = "Search…",
                trailingIcon = InstUIIcons.x,
                onTrailingIconClick = { query = "" },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "SearchInput Md — Light", showBackground = true)
@Preview(name = "SearchInput Md — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchInputMdPreview() {
    InstUITheme {
        var query by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Md — placeholder
            SearchInput(
                value = "",
                onValueChange = {},
                size = SearchInputSize.Md,
                placeholder = "Search courses…",
                modifier = Modifier.fillMaxWidth(),
            )
            // Md — filled with trailing clear icon
            SearchInput(
                value = "Calculus II",
                onValueChange = { query = it },
                size = SearchInputSize.Md,
                placeholder = "Search courses…",
                trailingIcon = InstUIIcons.x,
                onTrailingIconClick = { query = "" },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "SearchInput Lg — Light", showBackground = true)
@Preview(name = "SearchInput Lg — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchInputLgPreview() {
    InstUITheme {
        var query by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Lg — placeholder
            SearchInput(
                value = "",
                onValueChange = {},
                size = SearchInputSize.Lg,
                placeholder = "Search everything…",
                modifier = Modifier.fillMaxWidth(),
            )
            // Lg — filled with trailing clear icon
            SearchInput(
                value = "Introduction to Art History",
                onValueChange = { query = it },
                size = SearchInputSize.Lg,
                placeholder = "Search everything…",
                trailingIcon = InstUIIcons.x,
                onTrailingIconClick = { query = "" },
                modifier = Modifier.fillMaxWidth(),
            )
            // Lg — disabled
            SearchInput(
                value = "Disabled state",
                onValueChange = {},
                size = SearchInputSize.Lg,
                placeholder = "Search everything…",
                trailingIcon = InstUIIcons.x,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// endregion
