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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import com.instructure.instui.token.component.InstUIBaseButton
import com.instructure.instui.token.component.InstUIFormFieldLayout
import com.instructure.instui.token.component.InstUIFormFieldMessage
import com.instructure.instui.token.component.InstUIIcon
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.component.InstUITextInput
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.mic
import com.instructure.instui.token.icon.paperclip
import com.instructure.instui.token.icon.search
import com.instructure.instui.token.icon.send
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

/** Toolbar action item for a [ChatInput] ghost button. */
data class ChatToolbarAction(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
)

/**
 * InstUI ChatInput component. Rounded (radius `xxl` = 24 dp) chat composer per Figma
 * `27075-18130`.
 *
 * The outer container has a `xxl` border radius and a 1 dp border drawn from
 * [InstUITextInput] tokens. Inside the container there are two stacked rows:
 * 1. **Text row** — optional leading icon, an inline label (with optional required asterisk)
 *    or placeholder overlay, and an editable [BasicTextField]. The text cursor uses the icon
 *    "info" color per the Figma spec.
 * 2. **Toolbar row** — a left group of 32 dp bordered "ghost" icon buttons (each entry in
 *    [toolbarActions]) and a right 32 dp filled "send" button (primary background, inverse icon).
 *
 * An optional [hint] message is rendered below the container.
 *
 * Ghost-button styling uses [InstUIBaseButton.primaryGhostBorderColor] and
 * [InstUILayoutSizes.BorderRadius.Lg.lg] (12 dp). The send button uses
 * [InstUIBaseButton.primaryBackground] with an [IconColor.Inverse] icon.
 *
 * Usage:
 * ```
 * ChatInput(
 *     value = message,
 *     onValueChange = { message = it },
 *     label = "Message",
 *     placeholder = "Type something…",
 *     onSend = { sendMessage(message) },
 *     sendContentDescription = stringResource(R.string.send),
 *     toolbarActions = listOf(
 *         ChatToolbarAction(InstUIIcons.paperclip, "Attach file") { /* … */ },
 *         ChatToolbarAction(InstUIIcons.mic, "Voice") { /* … */ },
 *     ),
 *     hint = "Max 2 000 characters",
 * )
 * ```
 */
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendContentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    isRequired: Boolean = false,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    hint: String? = null,
    toolbarActions: List<ChatToolbarAction> = emptyList(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    // --- Resolved token values ---
    val backgroundColor = if (enabled) InstUITextInput.backgroundColor else InstUITextInput.backgroundDisabledColor
    val borderColor = if (enabled) InstUITextInput.borderColor else InstUITextInput.borderDisabledColor
    val textColor = if (enabled) InstUITextInput.textColor else InstUITextInput.textDisabledColor
    val caretColor = InstUIIcon.infoColor
    val disabledIconTint = InstUITextInput.textDisabledColor

    val textStyle = TextStyle(
        color = textColor,
        fontFamily = InstUITextInput.fontFamily,
        fontWeight = InstUITextInput.fontWeight,
        fontSize = InstUITextInput.fontSizeMd,
    )

    val containerShape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Xxl.xxl)
    val buttonShape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Lg.lg)

    val horizontalPadding = InstUITextInput.paddingHorizontalMd
    val spacingSm = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
    val spacingXs = InstUILayoutSizes.Spacing.SpaceXs.spaceXs
    val spacingMd = InstUILayoutSizes.Spacing.SpaceMd.spaceMd

    val toolbarButtonSize = InstUILayoutSizes.Size.Interactive.height_sm // 32 dp
    val ghostBorderColor = InstUIBaseButton.primaryGhostBorderColor
    val sendBackground = InstUIBaseButton.primaryBackground

    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier) {
        // Outer rounded container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(containerShape)
                .background(backgroundColor)
                .border(
                    width = InstUITextInput.borderWidth,
                    color = borderColor,
                    shape = containerShape,
                )
                .padding(horizontal = horizontalPadding, vertical = spacingSm),
        ) {
            // 1. Text row
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                textStyle = textStyle,
                singleLine = false,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                cursorBrush = SolidColor(caretColor),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Optional leading icon — only rendered when leadingIcon is provided
                        if (leadingIcon != null) {
                            if (enabled) {
                                Icon(
                                    imageVector = leadingIcon,
                                    size = IconSize.Small,
                                    color = IconColor.Muted,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = spacingSm),
                                )
                            } else {
                                Icon(
                                    imageVector = leadingIcon,
                                    tint = disabledIconTint,
                                    size = IconSize.Small,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = spacingSm),
                                )
                            }
                        }

                        // Text content area: inline label/placeholder overlay + field
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            // Inline label shown when value is empty and label is provided
                            if (label != null && value.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = label,
                                        style = InstUIText.content.copy(
                                            color = InstUIFormFieldLayout.textColor,
                                            fontSize = InstUITextInput.fontSizeMd,
                                        ),
                                    )
                                    if (isRequired) {
                                        Text(
                                            text = " *",
                                            style = InstUIText.content.copy(
                                                color = InstUIFormFieldLayout.asteriskColor,
                                                fontSize = InstUITextInput.fontSizeMd,
                                            ),
                                        )
                                    }
                                }
                            } else if (label == null && value.isEmpty() && placeholder != null) {
                                // Placeholder overlay when no label is set
                                Text(
                                    text = placeholder,
                                    style = InstUIText.content.copy(
                                        color = InstUITextInput.placeholderColor,
                                        fontSize = InstUITextInput.fontSizeMd,
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    }
                },
            )

            // 2. Toolbar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left group: ghost icon buttons — horizontally scrollable so overflow cannot
                // push the send button off-screen; weight(1f) lets the send button stay pinned.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                ) {
                    toolbarActions.forEach { action ->
                        Box(
                            modifier = Modifier
                                .size(toolbarButtonSize)
                                .clip(buttonShape)
                                .border(
                                    width = InstUITextInput.borderWidth,
                                    color = ghostBorderColor,
                                    shape = buttonShape,
                                )
                                .then(
                                    if (enabled) {
                                        Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = action.onClick,
                                        )
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (enabled) {
                                Icon(
                                    imageVector = action.icon,
                                    size = IconSize.Small,
                                    color = IconColor.Base,
                                    contentDescription = action.contentDescription,
                                )
                            } else {
                                Icon(
                                    imageVector = action.icon,
                                    tint = disabledIconTint,
                                    size = IconSize.Small,
                                    contentDescription = action.contentDescription,
                                )
                            }
                        }
                    }
                }

                // Right: filled send button
                Box(
                    modifier = Modifier
                        .size(toolbarButtonSize)
                        .clip(buttonShape)
                        .background(if (enabled) sendBackground else InstUIBaseButton.primaryDisabledBackgroundColor)
                        .then(
                            if (enabled) {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onSend,
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (enabled) {
                        Icon(
                            imageVector = InstUIIcons.send,
                            size = IconSize.Small,
                            color = IconColor.Inverse,
                            contentDescription = sendContentDescription,
                        )
                    } else {
                        Icon(
                            imageVector = InstUIIcons.send,
                            tint = InstUIBaseButton.primaryDisabledTextColor,
                            size = IconSize.Small,
                            contentDescription = sendContentDescription,
                        )
                    }
                }
            }
        }

        // Optional hint/status message below the container
        if (hint != null) {
            Text(
                text = hint,
                style = InstUIText.contentSmall.copy(
                    color = InstUIFormFieldMessage.hintTextColor,
                    fontWeight = InstUIFormFieldMessage.fontWeight,
                    fontSize = InstUIFormFieldMessage.fontSize,
                ),
                modifier = Modifier.padding(top = spacingXs),
            )
        }
    }
}

// region Previews

@Preview(name = "ChatInput Placeholder — Light", showBackground = true)
@Preview(name = "ChatInput Placeholder — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatInputPlaceholderPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Empty — placeholder visible (no label)
            ChatInput(
                value = "",
                onValueChange = {},
                onSend = {},
                sendContentDescription = "Send",
                placeholder = "Type a message…",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "ChatInput Filled — Light", showBackground = true)
@Preview(name = "ChatInput Filled — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatInputFilledPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // Filled text, label visible (inline), with a neutral leading icon
            ChatInput(
                value = "Hello, everyone!",
                onValueChange = {},
                onSend = {},
                sendContentDescription = "Send",
                label = "Message",
                isRequired = true,
                leadingIcon = InstUIIcons.search,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "ChatInput Toolbar — Light", showBackground = true)
@Preview(name = "ChatInput Toolbar — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatInputToolbarPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // With toolbar actions + send
            ChatInput(
                value = "",
                onValueChange = {},
                onSend = {},
                sendContentDescription = "Send",
                placeholder = "Type a message…",
                toolbarActions = listOf(
                    ChatToolbarAction(InstUIIcons.paperclip, "Attach file") {},
                    ChatToolbarAction(InstUIIcons.mic, "Voice message") {},
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "ChatInput Hint — Light", showBackground = true)
@Preview(name = "ChatInput Hint — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatInputHintPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            // With hint message below
            ChatInput(
                value = "Almost done!",
                onValueChange = {},
                onSend = {},
                sendContentDescription = "Send",
                label = "Reply",
                hint = "Max 2 000 characters",
                toolbarActions = listOf(
                    ChatToolbarAction(InstUIIcons.paperclip, "Attach file") {},
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "ChatInput Disabled — Light", showBackground = true)
@Preview(name = "ChatInput Disabled — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatInputDisabledPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp),
        ) {
            ChatInput(
                value = "Cannot edit this",
                onValueChange = {},
                onSend = {},
                sendContentDescription = "Send",
                label = "Message",
                enabled = false,
                toolbarActions = listOf(
                    ChatToolbarAction(InstUIIcons.paperclip, "Attach file") {},
                    ChatToolbarAction(InstUIIcons.mic, "Voice message") {},
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// endregion
