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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

/**
 * App bar for main screens, colors are defined by the Canvas theme.
 */
@Composable
fun CanvasThemedAppBar(
    title: String,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    @DrawableRes navIconRes: Int = R.drawable.ic_back_arrow,
    navIconContentDescription: String = stringResource(id = R.string.back),
    backgroundColor: Color = Color(color = ThemePrefs.primaryColor),
    contentColor: Color = Color(color = ThemePrefs.primaryTextColor),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                        .testTag("todoDetailsPageTitle")
                        .semantics { heading() }
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = actions,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        navigationIcon = {
            IconButton(onClick = navigationActionClick) {
                Icon(painterResource(id = navIconRes), contentDescription = navIconContentDescription)
            }
        },
        modifier = modifier.testTag("toolbar"),
        elevation = 0.dp
    )
}

@Preview
@Composable
fun CanvasThemedAppBarPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasThemedAppBar(title = "Title", subtitle = "Subtitle", navigationActionClick = {})
}