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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun<GROUP: GroupedListViewGroup<GROUP_ITEM>, GROUP_ITEM: GroupedListViewGroupItem> GroupedListView(
    state: GroupedListViewState<GROUP>,
    headerView: (@Composable () -> Unit)? = null,
    groupHeaderView: @Composable (GROUP, () -> Unit) -> Unit = { group, onClick -> GroupedListGroupHeaderView(group, onClick) },
    itemView: @Composable (GROUP_ITEM, Modifier) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (GroupedListViewEvent<GROUP, GROUP_ITEM>) -> Unit
) {

    LazyColumn(
        modifier = modifier
    ) {
        item {
            headerView?.let { it() }
        }
        state.groups.forEach { group ->
            stickyHeader {
                groupHeaderView(group) {
                    actionHandler(GroupedListViewEvent.GroupClicked(group))
                }
            }
            if (group.isExpanded) {
                items(group.items) { item ->
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

private fun Modifier.conditional(condition : Boolean, modifier : Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun
        <GROUP: GroupedListViewGroup<GROUP_ITEM>,
        GROUP_ITEM: GroupedListViewGroupItem>
    GroupedListGroupHeaderView(group: GROUP, onClick: () -> Unit) {
    val headerContentDescription = stringResource(
        if (group.isExpanded) {
            R.string.content_description_collapse_content_with_param
        } else {
            R.string.content_description_expand_content_with_param
        }, group.title
    )
    val iconRotation by animateFloatAsState(targetValue = if (group.isExpanded) 180f else 0f, label = "expandedIconRotation")

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .clickable {
                onClick()
            }
            .semantics {
                heading()
                contentDescription = headerContentDescription
                role = Role.Button
            }
    ) {
        Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = group.title,
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
                modifier = Modifier.semantics {
                    invisibleToUser()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                tint = colorResource(id = R.color.textDarkest),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(iconRotation)
            )
        }
        Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
    }
}

@Preview
@Composable
private fun GroupedListViewPreview() {
    val state = GroupedListViewState(
        groups = listOf(
            GroupedListViewGroup(
                id = "1",
                title = "Group 1",
                isExpanded = true,
                items = listOf(
                    GroupedListViewGroupItem(id = "1.1"),
                    GroupedListViewGroupItem(id = "1.2"),
                    GroupedListViewGroupItem(id = "1.3")
                )
            ),
            GroupedListViewGroup(
                id = "2",
                title = "Group 2",
                isExpanded = true,
                items = listOf(
                    GroupedListViewGroupItem(id = "2.1"),
                    GroupedListViewGroupItem(id = "2.2"),
                    GroupedListViewGroupItem(id = "2.3")
                )
            )
        )
    )

    GroupedListView(
        state = state,
        headerView = {
            Text("Header")
        },
        groupHeaderView = { group, onClick ->
            GroupedListGroupHeaderView(group, onClick)
        },
        itemView = { item, _ ->
            Text("Item ${item.id}")
        },
        actionHandler = {}
    )
}

data class GroupedListViewState<GROUP>(
    val groups: List<GROUP>,
)

open class GroupedListViewGroup<GROUP_ITEM: GroupedListViewGroupItem>(
    val id: Any?,
    val title: String,
    val isExpanded: Boolean,
    val items: List<GROUP_ITEM>
)

open class GroupedListViewGroupItem(
    val id: Any?
)

sealed class GroupedListViewEvent<GROUP: GroupedListViewGroup<GROUP_ITEM>, GROUP_ITEM: GroupedListViewGroupItem> {
    data class GroupClicked<GROUP: GroupedListViewGroup<GROUP_ITEM>, GROUP_ITEM: GroupedListViewGroupItem>(val group: GROUP) : GroupedListViewEvent<GROUP, GROUP_ITEM>()
    data class ItemClicked<GROUP: GroupedListViewGroup<GROUP_ITEM>, GROUP_ITEM: GroupedListViewGroupItem>(val groupItem: GROUP_ITEM) : GroupedListViewEvent<GROUP, GROUP_ITEM>()
}