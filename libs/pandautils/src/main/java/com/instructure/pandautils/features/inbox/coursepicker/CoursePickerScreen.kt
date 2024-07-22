/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.coursepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CoursePickerScreen(
    coursePickerViewModel: CoursePickerViewModel,
    onContextSelected: (CanvasContext) -> Unit,
) {
    val screenState = coursePickerViewModel.state.collectAsState()
    val pullToRefreshState = rememberPullRefreshState(refreshing = false, onRefresh = {
        coursePickerViewModel.loadData()
    })
    val courses by coursePickerViewModel.courses.collectAsState()
    val groups by coursePickerViewModel.groups.collectAsState()

    LazyColumn(
        modifier = Modifier
            .pullRefresh(pullToRefreshState)
    ) {
        when (val state = screenState.value) {
            CoursePickerViewModel.State.LOADING -> {
                item {
                    CircularProgressIndicator()
                }
            }
            CoursePickerViewModel.State.DATA -> {
                item {
                    SectionHeaderView("Courses")
                }

                items(courses) {
                    DataRow(it, onContextSelected)
                }

                item {
                    SectionHeaderView("Groups")
                }

                items(groups) {
                    DataRow(it, onContextSelected)
                }
            }
        }
    }
}


@Composable
private fun SectionHeaderView(subTitle: String) {
    Text(
        subTitle,
        color = colorResource(id = R.color.textLight),
        modifier = Modifier
            .background(colorResource(id = R.color.backgroundDark))
    )
}

@Composable
private fun DataRow(context: CanvasContext, onContextSelected: (CanvasContext) -> Unit) {
    Text(
        context.name ?: "",
        color = colorResource(id = R.color.textDarkest),
        modifier = Modifier
            .clickable {
                onContextSelected(context)
            }
    )
}