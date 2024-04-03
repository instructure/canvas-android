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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
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
fun RCEControls(rceState: RCEState, onActionClick: (RCEAction) -> Unit) {
    Column {
        RCETextControls(rceState, onActionClick)
        AnimatedVisibility(
            visible = rceState.colorPicker
        ) {
            RCEColorControls()
        }
        Divider()
    }
}

@Composable
fun RCETextControls(rceState: RCEState, onActionClick: (RCEAction) -> Unit) {
    LazyRow {
        item {
            ActionIcon(
                iconRes = R.drawable.ic_rce_undo,
                contentDescription = R.string.rce_contentDescription_undo,
                active = false
            ) {
                onActionClick(RCEAction.UNDO)
            }
            ActionIcon(
                iconRes = R.drawable.ic_rce_redo,
                contentDescription = R.string.rce_contentDescription_redo,
                active = false
            ) {
                onActionClick(RCEAction.UNDO)
            }
            ActionIcon(
                iconRes = R.drawable.ic_rce_format_bold,
                contentDescription = R.string.rce_contentDescription_bold,
                active = rceState.bold
            ) {
                onActionClick(RCEAction.BOLD)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_format_italic,
                contentDescription = R.string.rce_contentDescription_italic,
                active = rceState.italic
            ) {
                onActionClick(RCEAction.ITALIC)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_format_underlined,
                contentDescription = R.string.rce_contentDescription_underline,
                active = rceState.underline
            ) {
                onActionClick(RCEAction.UNDERLINE)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_format_color_text,
                contentDescription = R.string.rce_contentDescription_format_text_color,
                active = rceState.colorPicker
            ) {
                onActionClick(RCEAction.COLOR_PICKER)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_format_list_bulleted,
                contentDescription = R.string.rce_contentDescription_insert_bullets,
                active = rceState.bulletedList
            ) {
                onActionClick(RCEAction.BULLETED_LIST)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_format_list_numbered,
                contentDescription = R.string.rce_contentDescription_insert_numbers,
                active = rceState.numberedList
            ) {
                onActionClick(RCEAction.NUMBERED_LIST)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_insert_photo,
                contentDescription = R.string.rce_contentDescription_insert_photo,
                active = false
            ) {
                onActionClick(RCEAction.INSERT_IMAGE)
            }

            ActionIcon(
                iconRes = R.drawable.ic_rce_insert_link,
                contentDescription = R.string.rce_contentDescription_insert_link,
                active = false
            ) {
                onActionClick(RCEAction.INSERT_LINK)
            }
        }
    }
}

@Composable
fun RCEColorControls() {
    val context = LocalContext.current
    LazyRow(
        modifier = Modifier
            .height(48.dp)
            .padding(vertical = 12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(50))
                    .background(colorResource(id = R.color.rce_gray))
                    .semantics {
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
                        .background(colorResource(id = R.color.white))
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerBlack))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_black)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerGray))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_gray)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerRed))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_red)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerOrange))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_orange)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerYellow))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_yellow)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerGreen))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_green)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerBlue))
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_blue)
                    }
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.rce_pickerPurple))
                    .clickable { /*TODO*/ }
                    .semantics {
                        contentDescription =
                            context.getString(R.string.rce_contentDescription_color_purple)
                    }
            )
        }

    }
}

@Composable
fun ActionIcon(
    @DrawableRes iconRes: Int,
    @StringRes contentDescription: Int,
    active: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .size(48.dp)
            .background(colorResource(id = if (active) R.color.black else R.color.white)),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = stringResource(id = contentDescription),
            tint = colorResource(id = if (active) R.color.white else R.color.black)
        )
    }
}

@Preview
@Composable
fun RCEControlsPreview() {
    RCEControls(RCEState()) {

    }
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
    RCEColorControls()
}