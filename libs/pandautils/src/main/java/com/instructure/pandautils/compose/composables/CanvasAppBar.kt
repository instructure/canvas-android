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
 */
package com.instructure.pandautils.compose.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

/**
 * App bar for edit screens/modal screens, the colors are always the same and has smaller elevation.
 */
@Composable
fun CanvasAppBar(
    title: String,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes navIconRes: Int = R.drawable.ic_close,
    navIconContentDescription: String = stringResource(id = R.string.close),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = colorResource(id = R.color.backgroundLightestElevated),
    textColor: Color = colorResource(id = R.color.textDarkest)
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    isTraversalGroup = true
                    traversalIndex = -1f
                    heading()
                }
            ) {
                Text(text = title)
                subtitle?.let {
                    Text(text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        elevation = 2.dp,
        backgroundColor = backgroundColor,
        contentColor = textColor,
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("navigationButton"),
                onClick = navigationActionClick
            ) {
                Icon(
                    painter = painterResource(id = navIconRes),
                    contentDescription = navIconContentDescription
                )
            }
        },
        modifier = modifier.testTag("toolbar"),
        actions = actions
    )
}

@Preview
@Composable
fun CanvasAppBarPreview() {
    CanvasAppBar(title = "Title", subtitle = "Subtitle that is really really really long to make sure it does not fit into one line", navigationActionClick = {})
}