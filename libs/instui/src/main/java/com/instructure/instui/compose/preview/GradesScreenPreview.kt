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

package com.instructure.instui.compose.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.SectionHeader
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.ArrowLeft
import com.instructure.instui.token.icon.line.ArrowOpenDown
import com.instructure.instui.token.icon.line.ArrowOpenUp
import com.instructure.instui.token.icon.line.Assignment
import com.instructure.instui.token.icon.line.Lock
import com.instructure.instui.token.icon.line.More
import com.instructure.instui.token.icon.line.Warning
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * Dummy Grades screen preview demonstrating InstUI component usage.
 *
 * This preview mirrors the Figma "AM Grades" screen design:
 * - TopBar with course color, title, and subtitle
 * - Card showing restricted grades info
 * - Collapsible section headers
 * - List items with assignment icon, title, due date, status pill, and score
 */

private val CourseColor = Color(0xFF00828E)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Grades Screen — Light",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    name = "Grades Screen — Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun GradesScreenPreview() {
    InstUITheme {
        Scaffold(
            topBar = {
                TopBar(
                    title = "Page Title",
                    subtitle = "Course name longer Placeholder until 45 character",
                    containerColor = CourseColor,
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(InstUIIcons.Line.ArrowLeft, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(InstUIIcons.Line.More, contentDescription = "More")
                        }
                    },
                )
            },
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InstUISemanticColors.Background.container())
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
            ) {
                // Restricted grades card on page-colored background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InstUISemanticColors.Background.page())
                        .padding(16.dp)
                ) {
                    Card(elevation = Elevation.Level1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Total grades are restricted",
                                style = InstUITextTokens.contentSmall,
                                color = InstUITextTokens.mutedColor,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                InstUIIcons.Line.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = InstUISemanticColors.Icon.base(),
                            )
                        }
                    }
                }

                Separator()

                // Collapsed section
                SectionHeader(
                    title = "Overdue Assignments",
                    onClick = {},
                    trailing = {
                        Icon(
                            InstUIIcons.Line.ArrowOpenDown,
                            contentDescription = "Expand",
                            modifier = Modifier.size(16.dp),
                            tint = InstUISemanticColors.Icon.base(),
                        )
                    },
                )

                // Expanded section
                SectionHeader(
                    title = "Overdue Assignments",
                    onClick = {},
                    trailing = {
                        Icon(
                            InstUIIcons.Line.ArrowOpenUp,
                            contentDescription = "Collapse",
                            modifier = Modifier.size(16.dp),
                            tint = InstUISemanticColors.Icon.base(),
                        )
                    },
                )

                // Assignment list items (no separators between them per Figma)
                AssignmentListItem()
                AssignmentListItem()
                AssignmentListItem()
            }
        }
    }
}

@Composable
private fun AssignmentListItem() {
    ListItem(
        title = "Assignment name",
        subtext1 = "Due Oct 3, 2023 9:41",
        leading = {
            Icon(
                InstUIIcons.Line.Assignment,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = InstUISemanticColors.Icon.base(),
            )
        },
        pill = { Pill(text = "Missing", variant = PillVariant.Error) },
        score = {
            Text(
                text = "-/100",
                style = InstUITextTokens.contentImportant,
                color = CourseColor,
            )
        },
    )
}

/**
 * Minimal preview showing just the list items without scaffold,
 * useful for quick iteration on the list item design.
 */
@Preview(name = "Assignment Items — Light", showBackground = true)
@Preview(name = "Assignment Items — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AssignmentItemsPreview() {
    InstUITheme {
        Column(
            modifier = Modifier.background(InstUISemanticColors.Background.container())
        ) {
            AssignmentListItem()
            AssignmentListItem()
            AssignmentListItem()
        }
    }
}