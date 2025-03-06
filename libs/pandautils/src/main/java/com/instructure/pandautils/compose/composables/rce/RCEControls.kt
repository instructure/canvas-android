/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.pandautils.compose.composables.rce

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R

@Composable
fun RCEControls(
    rceState: RCEState,
    onActionClick: (RCEAction) -> Unit,
    onColorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RCETextControls(rceState, onActionClick = onActionClick)
        AnimatedVisibility(
            visible = rceState.colorPicker
        ) {
            RCEColorControls(onColorClick = onColorClick)
        }
        Divider()
    }
}

@Composable
fun RCETextControls(
    rceState: RCEState,
    modifier: Modifier = Modifier,
    onActionClick: (RCEAction) -> Unit
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        ActionButton(
            iconRes = R.drawable.ic_rce_undo,
            contentDescription = R.string.rce_contentDescription_undo,
            active = false,
            selectable = false
        ) {
            onActionClick(RCEAction.UNDO)
        }
        ActionButton(
            iconRes = R.drawable.ic_rce_redo,
            contentDescription = R.string.rce_contentDescription_redo,
            active = false,
            selectable = false
        ) {
            onActionClick(RCEAction.REDO)
        }
        ActionButton(
            iconRes = R.drawable.ic_rce_format_bold,
            contentDescription = R.string.rce_contentDescription_bold,
            active = rceState.bold,
            selectable = true
        ) {
            onActionClick(RCEAction.BOLD)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_format_italic,
            contentDescription = R.string.rce_contentDescription_italic,
            active = rceState.italic,
            selectable = true
        ) {
            onActionClick(RCEAction.ITALIC)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_format_underlined,
            contentDescription = R.string.rce_contentDescription_underline,
            active = rceState.underline,
            selectable = true
        ) {
            onActionClick(RCEAction.UNDERLINE)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_format_color_text,
            contentDescription = R.string.rce_contentDescription_format_text_color,
            active = rceState.colorPicker,
            selectable = true
        ) {
            onActionClick(RCEAction.COLOR_PICKER)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_format_list_bulleted,
            contentDescription = R.string.rce_contentDescription_insert_bullets,
            active = rceState.bulletedList,
            selectable = true
        ) {
            onActionClick(RCEAction.BULLETED_LIST)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_format_list_numbered,
            contentDescription = R.string.rce_contentDescription_insert_numbers,
            active = rceState.numberedList,
            selectable = true
        ) {
            onActionClick(RCEAction.NUMBERED_LIST)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_insert_photo,
            contentDescription = R.string.rce_contentDescription_insert_photo,
            active = false,
            selectable = false
        ) {
            onActionClick(RCEAction.INSERT_IMAGE)
        }

        ActionButton(
            iconRes = R.drawable.ic_rce_insert_link,
            contentDescription = R.string.rce_contentDescription_insert_link,
            active = false,
            selectable = false
        ) {
            onActionClick(RCEAction.INSERT_LINK)
        }

    }
}

@Composable
fun RCEColorControls(modifier: Modifier = Modifier, onColorClick: (Int) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .height(48.dp)
            .background(colorResource(id = R.color.backgroundLightest))
            .padding(vertical = 12.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .background(colorResource(id = R.color.rce_gray))
                .clickable { onColorClick(R.color.rce_pickerWhite) }
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        context.getString(R.string.rce_contentDescription_color_white)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerWhite))
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(50))
                .background(colorResource(id = R.color.backgroundDarkest))
                .clickable { onColorClick(R.color.rce_pickerBlack) }
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        context.getString(R.string.rce_contentDescription_color_black)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerBlack))
            )
        }

        ColorButton(
            color = R.color.rce_pickerGray,
            contentDescription = context.getString(R.string.rce_contentDescription_color_gray),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerRed,
            contentDescription = context.getString(R.string.rce_contentDescription_color_red),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerOrange,
            contentDescription = context.getString(R.string.rce_contentDescription_color_orange),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerYellow,
            contentDescription = context.getString(R.string.rce_contentDescription_color_yellow),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerGreen,
            contentDescription = context.getString(R.string.rce_contentDescription_color_green),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerBlue,
            contentDescription = context.getString(R.string.rce_contentDescription_color_blue),
            onColorClick = onColorClick
        )

        ColorButton(
            color = R.color.rce_pickerPurple,
            contentDescription = context.getString(R.string.rce_contentDescription_color_purple),
            onColorClick = onColorClick
        )
    }
}

@Composable
fun ActionButton(
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    active: Boolean,
    selectable: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier
            .size(48.dp)
            .background(colorResource(id = if (active) R.color.backgroundDarkest else R.color.backgroundLightest))
            .then(
                if (selectable) {
                    Modifier.selectable(
                        selected = active,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDescription),
            tint = colorResource(id = if (active) R.color.textLightest else R.color.textDarkest)
        )
    }
}

@Composable
fun ColorButton(
    @ColorRes color: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onColorClick: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(colorResource(id = color))
            .clickable { onColorClick(color) }
            .semantics {
                this.contentDescription = contentDescription
            }
    )
}

@Preview
@Composable
fun RCEControlsPreview() {
    RCEControls(RCEState(colorPicker = true), {}, {})
}

@Preview
@Composable
fun RCETextControlsPreview() {
    RCETextControls(RCEState()) {

    }
}

@Preview
@Composable
fun RceColorControlsPreview() {
    RCEColorControls {

    }
}