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
package com.instructure.pandautils.features.smartsearch

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasAppBar

@Composable
fun SmartSearchPreferencesScreen(
    color: Color,
    filters: List<SmartSearchFilter>,
    navigationClick: (List<SmartSearchFilter>) -> Unit
) {
    val selectedTypes = remember { filters.toMutableStateList() }
    Scaffold(
        topBar = {
            CanvasAppBar(
                backgroundColor = color,
                textColor = colorResource(R.color.textLightest),
                title = stringResource(R.string.searchPreferencesTitle),
                navigationActionClick = { navigationClick(selectedTypes) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(color = colorResource(R.color.backgroundLight))
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.resultTypeTitle),
                    fontSize = 14.sp,
                    color = colorResource(R.color.textDark),
                )

                TextButton(
                    onClick = {
                        if (selectedTypes.size == 4) {
                            selectedTypes.clear()
                        } else {
                            selectedTypes.clear()
                            selectedTypes.addAll(SmartSearchFilter.entries)
                        }

                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = color
                    ),
                ) {
                    Text(
                        if (selectedTypes.size == 4) {
                            stringResource(R.string.unselect_all)
                        } else {
                            stringResource(R.string.select_all)
                        }, fontWeight = FontWeight.Normal, fontSize = 14.sp
                    )
                }
            }

            SmartSearchFilter.entries.forEach { filter ->
                fun toggleFilter() {
                    if (selectedTypes.contains(filter)) {
                        selectedTypes.remove(filter)
                    } else {
                        selectedTypes.add(filter)
                    }
                }
                Row(
                    modifier = Modifier.clickable {
                        toggleFilter()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier.padding(start = 8.dp),
                        checked = selectedTypes.contains(filter),
                        onCheckedChange = { toggleFilter() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = color,
                            uncheckedColor = color
                        )
                    )
                    Text(
                        text = stringResource(getFilterTitle(filter)),
                        fontSize = 16.sp,
                        color = colorResource(R.color.textDarkest),
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .weight(1f)
                    )
                    Icon(
                        painter = painterResource(getFilterIcon(filter)),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

@StringRes
private fun getFilterTitle(smartSearchFilter: SmartSearchFilter): Int {
    return when (smartSearchFilter) {
        SmartSearchFilter.ANNOUNCEMENTS -> R.string.smartSearchAnnouncementsTitle
        SmartSearchFilter.ASSIGNMENTS -> R.string.smartSearchAssignmentsTitle
        SmartSearchFilter.DISCUSSION_TOPICS -> R.string.smartSearchDiscussionTopicsTitle
        SmartSearchFilter.PAGES -> R.string.smartSearchPagesTitle
    }
}

@DrawableRes
private fun getFilterIcon(smartSearchFilter: SmartSearchFilter): Int {
    return when (smartSearchFilter) {
        SmartSearchFilter.ANNOUNCEMENTS -> R.drawable.ic_announcement
        SmartSearchFilter.ASSIGNMENTS -> R.drawable.ic_assignment
        SmartSearchFilter.DISCUSSION_TOPICS -> R.drawable.ic_discussion
        SmartSearchFilter.PAGES -> R.drawable.ic_pages
    }
}

@Composable
@Preview
fun SmartSearchPreferencesScreenPreview() {
    SmartSearchPreferencesScreen(
        Color.Magenta,
        filters = SmartSearchFilter.entries,
        navigationClick = {}
    )
}
