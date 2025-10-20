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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.FullScreenDialog

enum class SmartSearchSortType {
    RELEVANCE,
    TYPE
}

@Composable
fun SmartSearchPreferencesScreen(
    color: Color,
    sortType: SmartSearchSortType,
    filters: List<SmartSearchFilter>,
    navigationClick: (List<SmartSearchFilter>, SmartSearchSortType) -> Unit
) {
    val selectedTypes = remember { filters.toMutableStateList() }
    var selectedSort by remember { mutableStateOf(sortType) }
    FullScreenDialog(onDismissRequest = { navigationClick(selectedTypes, selectedSort) }) {
        CanvasScaffold(
            topBar = {
                CanvasAppBar(
                    backgroundColor = color,
                    textColor = colorResource(R.color.textLightest),
                    title = stringResource(R.string.searchPreferencesTitle),
                    navigationActionClick = { navigationClick(selectedTypes, selectedSort) }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(color = colorResource(R.color.backgroundLightest))
                    .testTag("preferencesScreen")
            ) {
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
                        stringResource(R.string.sortByTitle),
                        fontSize = 14.sp,
                        color = colorResource(R.color.textDark),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { selectedSort = SmartSearchSortType.RELEVANCE }
                        .testTag("relevanceTypeSelector")) {
                    RadioButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("relevanceRadioButton"),
                        selected = SmartSearchSortType.RELEVANCE == selectedSort,
                        onClick = { selectedSort = SmartSearchSortType.RELEVANCE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = color,
                            unselectedColor = color,
                        )
                    )

                    Text(
                        text = stringResource(R.string.sortByRelevanceTitle),
                        fontSize = 16.sp,
                        color = colorResource(R.color.textDarkest),
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .weight(1f)
                            .testTag("sortTitle")
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { selectedSort = SmartSearchSortType.TYPE }
                        .testTag("typeTypeSelector")) {
                    RadioButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("typeRadioButton"),
                        selected = SmartSearchSortType.TYPE == selectedSort,
                        onClick = { selectedSort = SmartSearchSortType.TYPE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = color,
                            unselectedColor = color,
                        )
                    )

                    Text(
                        text = stringResource(R.string.sortByTypeTitle),
                        fontSize = 16.sp,
                        color = colorResource(R.color.textDarkest),
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                            .weight(1f)
                            .testTag("sortTitle")
                    )
                }

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
                        modifier = Modifier.testTag("toggleAllButton")
                    ) {
                        Text(
                            if (selectedTypes.size == 4) {
                                stringResource(R.string.unselect_all)
                            } else {
                                stringResource(R.string.select_all)
                            },
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
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
                        modifier = Modifier
                            .testTag("${filter.name.lowercase()}FilterRow")
                            .clickable {
                                toggleFilter()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("checkbox"),
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
                                .testTag("filterTitle")
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
        sortType = SmartSearchSortType.RELEVANCE,
        filters = SmartSearchFilter.entries,
        navigationClick = { _, _ -> }
    )
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SmartSearchPreferencesScreenDarkPreview() {
    SmartSearchPreferencesScreen(
        Color.Magenta,
        sortType = SmartSearchSortType.RELEVANCE,
        filters = SmartSearchFilter.entries,
        navigationClick = { _, _ -> }
    )
}

