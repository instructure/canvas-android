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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI segmented control / tab bar.
 *
 * Wraps Material3 [SecondaryTabRow] with InstUI token colors and typography.
 * When the real design system component is ready, only this file's internals change.
 *
 * Usage:
 * ```
 * SegmentedControl(
 *     tabs = listOf("Home", "Modules", "My Work", "More"),
 *     selectedIndex = 1,
 *     onTabSelected = { index -> },
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedControl(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalCourseColor.current,
) {
    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = InstUISemanticColors.Background.base(),
        contentColor = accentColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                color = accentColor,
            )
        },
        divider = {},
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = if (selected) InstUIHeading.titleCardMini else InstUITextTokens.content,
                        color = if (selected) accentColor else InstUISemanticColors.Text.base(),
                    )
                },
            )
        }
    }
}

@Preview(name = "SegmentedControl — Light", showBackground = true)
@Preview(name = "SegmentedControl — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SegmentedControlPreview() {
    InstUITheme(courseColor = Color(0xFFBF5811)) {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(16.dp)
        ) {
            SegmentedControl(
                tabs = listOf("Home", "Modules", "My Work", "More"),
                selectedIndex = 1,
                onTabSelected = {},
            )
        }
    }
}