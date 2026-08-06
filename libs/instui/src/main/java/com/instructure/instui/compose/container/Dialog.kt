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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import androidx.compose.ui.window.Dialog as ComposeDialog

// Figma: NATIVE.Android.Dialog (IDP-817, node 31654:3402). The card is a fixed 360dp in the design with
// min/max constraints on the component; on a phone it fills the available width between those bounds.
private val DialogMinWidth = 320.dp
private val DialogMaxWidth = 684.dp

// Gap between the title and its description. The design uses a bare 10dp here rather than a spacing
// token — kept as-is so the component matches the spec exactly.
private val DialogHeaderGap = 10.dp

// The bottom action row is a fixed 48dp band in the design, with the actions right-aligned and an
// optional leading icon pinned to the opposite edge.
private val DialogActionRowHeight = 48.dp
private val DialogActionSpacing = 8.dp

// The design caps the action row at three actions (Tertiary / Secondary / Primary).
private const val MaxDialogActions = 3

/**
 * A single button in an InstUI [Dialog]'s bottom action row.
 *
 * Actions render right-aligned in list order, so the primary action goes last — matching the design's
 * Tertiary / Secondary / Primary ordering.
 *
 * @param contentColor overrides the default action colour; use [InstUISemanticColors.Text.error] for
 * destructive actions.
 */
data class DialogAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val contentColor: Color? = null,
)

/**
 * InstUI dialog.
 *
 * A modal [Card] with a title, an optional description, an optional [content] slot for a form control
 * (the design shows a text input there), and a bottom action row of up to [MaxDialogActions] actions.
 *
 * Usage:
 * ```
 * Dialog(
 *     title = "A Short Title Is Best",
 *     description = "A description should be a short, complete sentence.",
 *     onDismissRequest = { showDialog = false },
 *     actions = listOf(
 *         DialogAction(label = "Cancel", onClick = { showDialog = false }),
 *         DialogAction(label = "Delete", onClick = ::delete, contentColor = InstUISemanticColors.Text.error()),
 *     ),
 * )
 * ```
 *
 * @param leadingIcon optional slot rendered at the start of the action row. The design reserves a 24dp
 * icon here; it has no glyph of its own because the InstUI icon system does not exist yet, so callers
 * supply one.
 */
@Composable
fun Dialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    actions: List<DialogAction> = emptyList(),
    properties: DialogProperties = DialogProperties(),
    leadingIcon: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    require(actions.size <= MaxDialogActions) {
        "InstUI Dialog supports at most $MaxDialogActions actions, got ${actions.size}"
    }

    ComposeDialog(
        onDismissRequest = onDismissRequest,
        // The design owns the width (min 320 / max 684), so opt out of the platform's own sizing.
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            securePolicy = properties.securePolicy,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = modifier
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
                .fillMaxWidth()
                .widthIn(min = DialogMinWidth, max = DialogMaxWidth),
            elevation = Elevation.Level1,
            contentPadding = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXl.spaceXl),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                            end = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                            top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                        ),
                    verticalArrangement = Arrangement.spacedBy(DialogHeaderGap),
                ) {
                    Text(
                        text = title,
                        style = InstUIHeading.titleCardLarge,
                        color = InstUISemanticColors.Text.base(),
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = InstUIText.content,
                            color = InstUISemanticColors.Text.muted(),
                        )
                    }
                }

                content?.invoke()

                if (actions.isNotEmpty() || leadingIcon != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DialogActionRowHeight),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                            contentAlignment = Alignment.Center,
                        ) {
                            leadingIcon?.invoke()
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DialogActionSpacing),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            actions.forEach { action ->
                                TextButton(
                                    text = action.label,
                                    onClick = action.onClick,
                                    enabled = action.enabled,
                                    contentColor = action.contentColor
                                        ?: InstUISemanticColors.Text.Interactive.Action.Tertiary.base(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Dialog — Light")
@Preview(name = "Dialog — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DialogPreview() {
    InstUITheme {
        Dialog(
            title = "Same in material",
            description = "Now you can design for Android as well!",
            onDismissRequest = {},
            actions = listOf(
                DialogAction(label = "Tertiary", onClick = {}),
                DialogAction(label = "Secondary", onClick = {}),
                DialogAction(label = "Primary", onClick = {}),
            ),
        )
    }
}

@Preview(name = "Dialog Destructive — Light")
@Preview(name = "Dialog Destructive — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DialogDestructivePreview() {
    InstUITheme {
        Dialog(
            title = "Delete comment?",
            description = "This comment will be permanently removed.",
            onDismissRequest = {},
            actions = listOf(
                DialogAction(label = "Cancel", onClick = {}),
                DialogAction(
                    label = "Delete",
                    onClick = {},
                    contentColor = InstUISemanticColors.Text.error(),
                ),
            ),
        )
    }
}
