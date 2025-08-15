/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.compose.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.pandautils.utils.orDefault

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <GROUP, ITEM> GroupedListView(
    items: Map<GROUP, List<ITEM>>,
    headerView: (@Composable () -> Unit)? = null,
    groupHeaderView: @Composable (GROUP, Boolean, () -> Unit) -> Unit = { group, isExpanded, onClick ->
        GroupHeader(
            group.toString(),
            isExpanded,
            onClick
        )
    },
    itemView: @Composable (ITEM, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (GroupedListViewEvent<ITEM>) -> Unit
) {
    var groupExpandedState by remember { mutableStateOf(items.keys.associateWith { true }) }
    LazyColumn(
        modifier = modifier
    ) {
        item {
            headerView?.let { it() }
        }
        items.keys.forEach { group ->
            stickyHeader {
                groupHeaderView(group, groupExpandedState[group].orDefault()) {
                    groupExpandedState[group]?.let { isExpanded ->
                        groupExpandedState = groupExpandedState.toMutableMap().apply {
                            this[group] = !isExpanded
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(
                    groupExpandedState[group].orDefault(),
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                    label = "GroupExpandAnimation"
                ) {
                    Column {
                        items[group]?.forEach { item ->
                            itemView(
                                item,
                                Modifier.clickable {
                                    actionHandler(GroupedListViewEvent.ItemClicked(item))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun GroupedListViewPreview() {

    GroupedListView(
        items = mapOf(
            "Group 1" to listOf("Item 1", "Item 2"),
            "Group 2" to listOf("Item 3", "Item 4"),
            "Group 3" to listOf("Item 5", "Item 6")
        ),
        headerView = {
            Text("Header")
        },
        groupHeaderView = { group, isExpanded, onClick ->
            GroupHeader(group, isExpanded, onClick)
        },
        itemView = { item, _ ->
            Text(item)
        },
        actionHandler = {}
    )
}

sealed class GroupedListViewEvent<ITEM> {
    data class ItemClicked<ITEM>(val groupItem: ITEM) : GroupedListViewEvent<ITEM>()
}