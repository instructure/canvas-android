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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R

/**
 * App bar for edit screens/modal screens, the colors are always the same and has smaller elevation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasAppBar(
    title: String,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes navIconRes: Int = R.drawable.ic_close,
    navIconContentDescription: String = stringResource(id = R.string.close),
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = colorResource(id = R.color.backgroundLightestElevated),
    textColor: Color = colorResource(id = R.color.textDarkest)
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
            )
        },
        colors = TopAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = textColor,
            scrolledContainerColor = backgroundColor,
            navigationIconContentColor = textColor,
            actionIconContentColor = textColor
        ),
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("navigationButton"),
                onClick = navigationActionClick) {
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
    CanvasAppBar(title = "Title", navigationActionClick = {})
}