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

package com.instructure.instui.compose.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI top navigation bar.
 *
 * Wraps Material3 TopAppBar with InstUI token colors and typography.
 * Supports a title, subtitle, navigation icon, and trailing actions.
 *
 * Usage:
 * ```
 * TopBar(
 *     title = "Page Title",
 *     subtitle = "Course name",
 *     navigationIcon = { IconButton(onClick = { navigateBack() }) { Icon(...) } },
 *     actions = { IconButton(onClick = { }) { Icon(...) } },
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    containerColor: Color = InstUISemanticColors.Background.brand(),
    contentColor: Color = InstUISemanticColors.Text.onColor(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = InstUIHeading.titleCardMini,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = InstUITextTokens.legend,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopBar — Light", showBackground = true)
@Preview(name = "TopBar — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopBarPreview() {
    InstUITheme {
        TopBar(
            title = "Page Title",
            subtitle = "Course name longer Placeholder until 45 characters",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopBar Title Only — Light", showBackground = true)
@Preview(name = "TopBar Title Only — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopBarTitleOnlyPreview() {
    InstUITheme {
        TopBar(title = "Page Title")
    }
}