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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIFormFieldLayout
import com.instructure.instui.token.component.InstUIFormFieldMessage
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextArea
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.alertCircle
import com.instructure.instui.token.icon.circleCheck
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Validation state for a [TextArea] field.
 */
sealed class TextAreaValidation {
    /** No validation indicator. */
    data object None : TextAreaValidation()

    /** Field is in an error state; [message] is shown below the field. */
    data class Error(val message: String) : TextAreaValidation()

    /** Field is in a success state; [message] is shown below the field. */
    data class Success(val message: String) : TextAreaValidation()
}

/**
 * InstUI TextArea component. Multiline form text field per Figma `27357-3739`.
 *
 * Uses [InstUITextArea], [InstUIFormFieldLayout], and [InstUIFormFieldMessage] tokens for
 * styling. Text grows vertically from [minLines] up to [maxLines] (unconstrained by default).
 *
 * Supports an optional above-field label (fixed) or inline label inside the field,
 * an optional trailing icon, placeholder text, and three validation states
 * (none / error / success) with a helper message shown below the field.
 *
 * Usage:
 * ```
 * TextArea(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "Notes",
 *     isRequired = true,
 *     placeholder = "Enter your notes here…",
 *     minLines = 4,
 * )
 * ```
 */
@Composable
fun TextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    isRequired: Boolean = false,
    inlineLabel: Boolean = false,
    placeholder: String? = null,
    trailingIcon: ImageVector? = null,
    minLines: Int = 3,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    validation: TextAreaValidation = TextAreaValidation.None,
    hint: String? = null,
) {
    val backgroundColor = when {
        !enabled -> InstUITextArea.backgroundDisabledColor
        readOnly -> InstUITextArea.backgroundReadonlyColor
        else -> InstUITextArea.backgroundColor
    }
    val borderColor = when {
        validation is TextAreaValidation.Error -> InstUITextArea.errorBorderColor
        validation is TextAreaValidation.Success -> InstUITextArea.successBorderColor
        !enabled -> InstUITextArea.borderDisabledColor
        readOnly -> InstUITextArea.borderReadonlyColor
        else -> InstUITextArea.borderColor
    }
    val textColor = when {
        !enabled -> InstUITextArea.textDisabledColor
        readOnly -> InstUITextArea.textReadonlyColor
        else -> InstUITextArea.textColor
    }
    val disabledIconTint = InstUITextArea.textDisabledColor

    val textStyle = TextStyle(
        color = textColor,
        fontFamily = InstUITextArea.fontFamily,
        fontWeight = InstUITextArea.fontWeight,
        fontSize = InstUITextArea.fontSizeMd,
    )

    val interactionSource = remember { MutableInteractionSource() }
    val fieldShape = RoundedCornerShape(InstUITextArea.borderRadius)
    val contentPadding = InstUITextArea.padding
    val iconGap = InstUILayoutSizes.Spacing.Gap.inputElements
    val effectiveMaxLines = maxLines.coerceAtLeast(minLines)

    Column {
        // Fixed above-field label (when label is set and inlineLabel is false)
        if (label != null && !inlineLabel) {
            TextAreaLabelRow(label = label, isRequired = isRequired)
            Spacer(modifier = Modifier.height(InstUILayoutSizes.Spacing.SpaceXs.spaceXs))
        }

        // Input area: border + background + text content + optional trailing icon
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .clip(fieldShape)
                .background(backgroundColor)
                .border(
                    width = InstUITextArea.borderWidth,
                    color = borderColor,
                    shape = fieldShape,
                ),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            singleLine = false,
            minLines = minLines,
            maxLines = effectiveMaxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(textColor),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                // Text field + optional trailing icon in a top-aligned row.
                // The placeholder/inline-label is overlaid on the input area via Box so
                // that the empty field height equals minLines (not minLines + label height).
                Row(
                    modifier = Modifier.padding(contentPadding),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        // Overlay: placeholder or inline label, drawn behind the cursor
                        // when the field is empty. Both the overlay and innerTextField()
                        // occupy the same space; the overlay is only visible when empty.
                        if (value.isEmpty()) {
                            if (label != null && inlineLabel) {
                                // Inline label acts as the placeholder
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = label,
                                        style = InstUIText.content.copy(
                                            color = InstUIFormFieldLayout.textColor,
                                            fontSize = InstUITextArea.fontSizeMd,
                                        ),
                                    )
                                    if (isRequired) {
                                        Text(
                                            text = " *",
                                            style = InstUIText.content.copy(
                                                color = InstUIFormFieldLayout.asteriskColor,
                                                fontSize = InstUITextArea.fontSizeMd,
                                            ),
                                        )
                                    }
                                }
                            } else if (placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = InstUIText.content.copy(
                                        color = InstUITextArea.placeholderColor,
                                        fontSize = InstUITextArea.fontSizeMd,
                                    ),
                                )
                            }
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        if (enabled) {
                            Icon(
                                imageVector = trailingIcon,
                                size = IconSize.Small,
                                color = IconColor.Muted,
                                modifier = Modifier.padding(start = iconGap),
                            )
                        } else {
                            Icon(
                                imageVector = trailingIcon,
                                tint = disabledIconTint,
                                size = IconSize.Small,
                                modifier = Modifier.padding(start = iconGap),
                            )
                        }
                    }
                }
            },
        )

        // Message row: hint or validation message with icon
        val messageText: String? = when (validation) {
            is TextAreaValidation.Error -> validation.message.takeIf { it.isNotEmpty() }
            is TextAreaValidation.Success -> validation.message.takeIf { it.isNotEmpty() }
            TextAreaValidation.None -> hint
        }

        if (messageText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
            ) {
                when (validation) {
                    is TextAreaValidation.Error -> {
                        Icon(
                            imageVector = InstUIIcons.alertCircle,
                            size = IconSize.Small,
                            color = IconColor.Error,
                        )
                        Text(
                            text = messageText,
                            style = InstUIText.contentSmall.copy(
                                color = InstUIFormFieldMessage.errorTextColor,
                                fontWeight = InstUIFormFieldMessage.fontWeight,
                                fontSize = InstUIFormFieldMessage.fontSize,
                            ),
                        )
                    }
                    is TextAreaValidation.Success -> {
                        Icon(
                            imageVector = InstUIIcons.circleCheck,
                            size = IconSize.Small,
                            color = IconColor.Success,
                        )
                        Text(
                            text = messageText,
                            style = InstUIText.contentSmall.copy(
                                color = InstUIFormFieldMessage.successTextColor,
                                fontWeight = InstUIFormFieldMessage.fontWeight,
                                fontSize = InstUIFormFieldMessage.fontSize,
                            ),
                        )
                    }
                    TextAreaValidation.None -> {
                        Text(
                            text = messageText,
                            style = InstUIText.contentSmall.copy(
                                color = InstUIFormFieldMessage.hintTextColor,
                                fontWeight = InstUIFormFieldMessage.fontWeight,
                                fontSize = InstUIFormFieldMessage.fontSize,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextAreaLabelRow(label: String, isRequired: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = InstUIText.content.copy(
                color = InstUIFormFieldLayout.textColor,
                fontWeight = InstUIFormFieldLayout.fontWeight,
                fontSize = InstUIFormFieldLayout.fontSize,
            ),
        )
        if (isRequired) {
            Text(
                text = " *",
                style = InstUIText.content.copy(
                    color = InstUIFormFieldLayout.asteriskColor,
                    fontWeight = InstUIFormFieldLayout.fontWeight,
                    fontSize = InstUIFormFieldLayout.fontSize,
                ),
            )
        }
    }
}

// region Previews

@Preview(name = "TextArea — Light", showBackground = true)
@Preview(name = "TextArea — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextAreaPlaceholderPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Empty with placeholder
            TextArea(
                value = "",
                onValueChange = {},
                placeholder = "Enter your message here…",
                modifier = Modifier.fillMaxWidth(),
            )
            // Filled with long text
            TextArea(
                value = "This is a longer piece of text that spans multiple lines in the text area. " +
                    "The field grows to accommodate the content while respecting the minLines minimum height.",
                onValueChange = {},
                placeholder = "Enter your message here…",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextArea Fixed Label — Light", showBackground = true)
@Preview(name = "TextArea Fixed Label — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextAreaFixedLabelPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Fixed label (above), required, empty
            TextArea(
                value = "",
                onValueChange = {},
                label = "Assignment Notes",
                isRequired = true,
                placeholder = "Add any notes for your teacher…",
                modifier = Modifier.fillMaxWidth(),
            )
            // Fixed label (above), filled
            TextArea(
                value = "Great work on the last assignment! Here are some thoughts on how to improve for next time.",
                onValueChange = {},
                label = "Feedback",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextArea Inline Label — Light", showBackground = true)
@Preview(name = "TextArea Inline Label — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextAreaInlineLabelPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Inline label (empty — label visible)
            TextArea(
                value = "",
                onValueChange = {},
                label = "Description",
                isRequired = true,
                inlineLabel = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // Inline label (filled — label hidden, content shown)
            TextArea(
                value = "Canvas is an LMS that helps educators and students connect.",
                onValueChange = {},
                label = "Description",
                inlineLabel = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextArea States — Light", showBackground = true)
@Preview(name = "TextArea States — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextAreaStatesPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Disabled
            TextArea(
                value = "This field cannot be edited.",
                onValueChange = {},
                label = "Disabled field",
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            // Read-only
            TextArea(
                value = "Read-only content that cannot be modified by the user.",
                onValueChange = {},
                label = "Read-only field",
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // Error state
            TextArea(
                value = "Too short.",
                onValueChange = {},
                label = "Response",
                validation = TextAreaValidation.Error("Please write at least 50 characters."),
                modifier = Modifier.fillMaxWidth(),
            )
            // Hint text
            TextArea(
                value = "",
                onValueChange = {},
                label = "Bio",
                placeholder = "Tell us about yourself…",
                hint = "Maximum 500 characters",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// endregion
