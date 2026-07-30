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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIFormFieldLayout
import com.instructure.instui.token.component.InstUIFormFieldMessage
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.alertCircle
import com.instructure.instui.token.icon.circleArrowUp
import com.instructure.instui.token.icon.circleCheck
import com.instructure.instui.token.icon.paperclip
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Validation state for a [TextInput] field.
 */
sealed class TextInputValidation {
    /** No validation indicator. */
    data object None : TextInputValidation()

    /** Field is in an error state; [message] is shown below the field. */
    data class Error(val message: String) : TextInputValidation()

    /** Field is in a success state; [message] is shown below the field. */
    data class Success(val message: String) : TextInputValidation()
}

/**
 * Token-backed height/font/padding dimensions for each [TextInput] size.
 */
enum class TextInputSize(
    val height: @Composable () -> Dp,
    val fontSize: @Composable () -> androidx.compose.ui.unit.TextUnit,
    val horizontalPadding: @Composable () -> Dp,
) {
    Small(
        height = { InstUITextInput.heightSm },
        fontSize = { InstUITextInput.fontSizeSm },
        horizontalPadding = { InstUITextInput.paddingHorizontalSm },
    ),
    Medium(
        height = { InstUITextInput.heightMd },
        fontSize = { InstUITextInput.fontSizeMd },
        horizontalPadding = { InstUITextInput.paddingHorizontalMd },
    ),
    Large(
        height = { InstUITextInput.heightLg },
        fontSize = { InstUITextInput.fontSizeLg },
        horizontalPadding = { InstUITextInput.paddingHorizontalLg },
    ),
}

/**
 * InstUI TextInput component. Uses [InstUITextInput], [InstUIFormFieldLayout], and
 * [InstUIFormFieldMessage] tokens for styling.
 *
 * Supports optional above-field label (fixed) or inline label inside the field,
 * leading and trailing icons, and three validation states (none / error / success)
 * that change the border color and show a helper message with an icon below the field.
 *
 * [actionsContent] renders an extra row (e.g. attach/send icons) below the text field, still inside
 * the same bordered box — used by the message-compose variant (see the comments input row).
 *
 * Usage:
 * ```
 * TextInput(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "Email",
 *     isRequired = true,
 *     validation = TextInputValidation.Error("Invalid address"),
 * )
 * ```
 */
@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    size: TextInputSize = TextInputSize.Medium,
    placeholder: String? = null,
    textAlign: TextAlign = TextAlign.Start,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: String? = null,
    isRequired: Boolean = false,
    inlineLabel: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    validation: TextInputValidation = TextInputValidation.None,
    hint: String? = null,
    actionsContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val height = size.height()
    val fontSize = size.fontSize()
    val horizontalPadding = size.horizontalPadding()
    val iconGap = InstUILayoutSizes.Spacing.SpaceSm.spaceSm

    val backgroundColor = when {
        !enabled -> InstUITextInput.backgroundDisabledColor
        readOnly -> InstUITextInput.backgroundReadonlyColor
        else -> InstUITextInput.backgroundColor
    }
    val borderColor = when {
        validation is TextInputValidation.Error -> InstUITextInput.errorBorderColor
        validation is TextInputValidation.Success -> InstUITextInput.successBorderColor
        !enabled -> InstUITextInput.borderDisabledColor
        readOnly -> InstUITextInput.borderReadonlyColor
        else -> InstUITextInput.borderColor
    }
    val textColor = when {
        !enabled -> InstUITextInput.textDisabledColor
        readOnly -> InstUITextInput.textReadonlyColor
        else -> InstUITextInput.textColor
    }
    val disabledIconTint = InstUITextInput.textDisabledColor

    val textStyle = TextStyle(
        color = textColor,
        fontFamily = InstUITextInput.fontFamily,
        fontWeight = InstUITextInput.fontWeight,
        fontSize = fontSize,
        textAlign = textAlign,
    )

    val interactionSource = remember { MutableInteractionSource() }
    val fieldShape = RoundedCornerShape(InstUITextInput.borderRadius)

    Column {
        // Fixed above-field label (when label is set and inlineLabel is false)
        if (label != null && !inlineLabel) {
            LabelRow(label = label, isRequired = isRequired)
            Spacer(modifier = Modifier.padding(bottom = InstUILayoutSizes.Spacing.SpaceXs.spaceXs))
        }

        // Field box: border + background, wraps the BasicTextField and the optional actions row
        // below it, so both share the same bordered container.
        Column(
            modifier = modifier
                .heightIn(min = height)
                .clip(fieldShape)
                .background(backgroundColor)
                .border(
                    width = InstUITextInput.borderWidth,
                    color = borderColor,
                    shape = fieldShape,
                ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                cursorBrush = SolidColor(textColor),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .defaultMinSize(minHeight = height)
                            .padding(horizontal = horizontalPadding),
                    ) {
                        // Leading icon
                        if (leadingIcon != null) {
                            if (enabled) {
                                Icon(
                                    imageVector = leadingIcon,
                                    size = IconSize.Small,
                                    color = IconColor.Muted,
                                    modifier = Modifier.padding(end = iconGap),
                                )
                            } else {
                                Icon(
                                    imageVector = leadingIcon,
                                    tint = disabledIconTint,
                                    size = IconSize.Small,
                                    modifier = Modifier.padding(end = iconGap),
                                )
                            }
                        }

                        // Text content area (inline label + placeholder + field)
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = when (textAlign) {
                                TextAlign.End -> Alignment.CenterEnd
                                TextAlign.Center -> Alignment.Center
                                else -> Alignment.CenterStart
                            },
                        ) {
                            // Inline label shown when value is empty and label is set
                            if (label != null && inlineLabel && value.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = label,
                                        style = InstUIText.content.copy(
                                            color = InstUIFormFieldLayout.textColor,
                                            fontSize = fontSize,
                                        ),
                                    )
                                    if (isRequired) {
                                        Text(
                                            text = " *",
                                            style = InstUIText.content.copy(
                                                color = InstUIFormFieldLayout.asteriskColor,
                                                fontSize = fontSize,
                                            ),
                                        )
                                    }
                                }
                            } else if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = InstUIText.content.copy(
                                        color = InstUITextInput.placeholderColor,
                                        fontSize = fontSize,
                                        textAlign = textAlign,
                                    ),
                                )
                            }
                            innerTextField()
                        }

                        // Trailing icon
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

            // Actions row: extra content (e.g. attach/send icons) below the field, inside the border
            if (actionsContent != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                            end = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                            bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                        ),
                    content = actionsContent,
                )
            }
        }

        // Message row: hint or validation message with icon
        val messageText: String? = when (validation) {
            is TextInputValidation.Error -> validation.message.takeIf { it.isNotEmpty() }
            is TextInputValidation.Success -> validation.message.takeIf { it.isNotEmpty() }
            TextInputValidation.None -> hint
        }

        if (messageText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
            ) {
                when (validation) {
                    is TextInputValidation.Error -> {
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
                    is TextInputValidation.Success -> {
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
                    TextInputValidation.None -> {
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
private fun LabelRow(label: String, isRequired: Boolean) {
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

@Preview(name = "TextInput — Light", showBackground = true)
@Preview(name = "TextInput — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputPlaceholderPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Placeholder (empty)
            TextInput(
                value = "",
                onValueChange = {},
                placeholder = "Enter your name",
                modifier = Modifier.fillMaxWidth(),
            )
            // Filled value
            TextInput(
                value = "Ada Lovelace",
                onValueChange = {},
                placeholder = "Enter your name",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextInput Fixed Label — Light", showBackground = true)
@Preview(name = "TextInput Fixed Label — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputFixedLabelPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Fixed label (above), required
            TextInput(
                value = "",
                onValueChange = {},
                label = "Email address",
                isRequired = true,
                placeholder = "you@example.com",
                modifier = Modifier.fillMaxWidth(),
            )
            // Fixed label (above), filled
            TextInput(
                value = "ada@canvas.instructure.com",
                onValueChange = {},
                label = "Email address",
                placeholder = "you@example.com",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextInput Inline Label — Light", showBackground = true)
@Preview(name = "TextInput Inline Label — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputInlineLabelPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Inline label (empty — label visible)
            TextInput(
                value = "",
                onValueChange = {},
                label = "Search courses",
                isRequired = true,
                inlineLabel = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // Inline label (filled — label hidden)
            TextInput(
                value = "Biology 101",
                onValueChange = {},
                label = "Search courses",
                inlineLabel = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextInput Validation — Light", showBackground = true)
@Preview(name = "TextInput Validation — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputValidationPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Error state
            TextInput(
                value = "bad-email",
                onValueChange = {},
                label = "Email",
                validation = TextInputValidation.Error("Please enter a valid email address"),
                modifier = Modifier.fillMaxWidth(),
            )
            // Success state
            TextInput(
                value = "ada@canvas.instructure.com",
                onValueChange = {},
                label = "Email",
                validation = TextInputValidation.Success("Looks good!"),
                modifier = Modifier.fillMaxWidth(),
            )
            // Hint text (no validation)
            TextInput(
                value = "",
                onValueChange = {},
                label = "Username",
                placeholder = "at least 3 characters",
                hint = "Cannot contain spaces or special characters",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextInput States — Light", showBackground = true)
@Preview(name = "TextInput States — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputStatesPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Disabled
            TextInput(
                value = "Cannot edit this",
                onValueChange = {},
                label = "Disabled field",
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            // Read-only
            TextInput(
                value = "Read-only value",
                onValueChange = {},
                label = "Read-only field",
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // Small size, numeric, right-aligned (grades call-site shape)
            TextInput(
                value = "",
                onValueChange = {},
                size = TextInputSize.Small,
                placeholder = "-",
                textAlign = TextAlign.End,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(96.dp),
            )
        }
    }
}

@Preview(name = "TextInput Icons — Light", showBackground = true)
@Preview(name = "TextInput Icons — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputIconsPreview() {
    InstUITheme {
        var value by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Leading icon
            TextInput(
                value = value,
                onValueChange = { value = it },
                label = "With leading icon",
                placeholder = "Search…",
                leadingIcon = InstUIIcons.alertCircle,
                modifier = Modifier.fillMaxWidth(),
            )
            // Trailing icon
            TextInput(
                value = value,
                onValueChange = { value = it },
                label = "With trailing icon",
                placeholder = "Enter value",
                trailingIcon = InstUIIcons.circleCheck,
                modifier = Modifier.fillMaxWidth(),
            )
            // Both icons
            TextInput(
                value = value,
                onValueChange = { value = it },
                label = "Leading + trailing",
                placeholder = "Both icons",
                leadingIcon = InstUIIcons.alertCircle,
                trailingIcon = InstUIIcons.circleCheck,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "TextInput Actions Row — Light", showBackground = true)
@Preview(name = "TextInput Actions Row — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TextInputActionsRowPreview() {
    InstUITheme {
        var value by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Message-compose variant: attach + send icons live inside the field, below the text
            TextInput(
                value = value,
                onValueChange = { value = it },
                placeholder = "Comment",
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
                actionsContent = {
                    Icon(imageVector = InstUIIcons.paperclip, size = IconSize.Medium, color = IconColor.Base)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = InstUIIcons.circleArrowUp,
                        size = IconSize.Large,
                        color = if (value.isNotBlank()) IconColor.CourseColor else IconColor.Muted,
                    )
                },
            )
        }
    }
}

// endregion
