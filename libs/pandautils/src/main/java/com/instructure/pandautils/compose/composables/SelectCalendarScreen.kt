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

package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.isUser
import com.jakewharton.threetenabp.AndroidThreeTen

private const val COURSES_KEY = "courses"
private const val GROUPS_KEY = "groups"
private const val HEADER_CONTENT_TYPE = "header"
private const val FILTER_ITEM_CONTENT_TYPE = "filter_item"

@Composable
fun SelectCalendarScreen(
    uiState: SelectCalendarUiState,
    onCalendarSelected: (CanvasContext) -> Unit,
    navigationActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasAppBar(
                title = stringResource(id = R.string.selectCalendarScreenTitle),
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_close,
                navIconContentDescription = stringResource(id = R.string.back)
            )
        },
        content = { padding ->
            SelectCalendarContent(
                uiState = uiState,
                onCalendarSelected = onCalendarSelected,
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    )
}

@Composable
private fun SelectCalendarContent(
    uiState: SelectCalendarUiState,
    onCalendarSelected: (CanvasContext) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        LazyColumn(
            modifier = modifier
        ) {
            items(uiState.users, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { user ->
                val selected = user.contextId == uiState.selectedCanvasContext?.contextId
                SelectCalendarItem(user, selected, onCalendarSelected, Modifier.fillMaxWidth())
            }
            if (uiState.courses.isNotEmpty()) {
                item(key = COURSES_KEY, contentType = HEADER_CONTENT_TYPE) {
                    ListHeaderItem(text = stringResource(id = R.string.calendarFilterCourse))
                }
                items(uiState.courses, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { course ->
                    val selected = course.contextId == uiState.selectedCanvasContext?.contextId
                    SelectCalendarItem(course, selected, onCalendarSelected, Modifier.fillMaxWidth())
                }
            }
            if (uiState.groups.isNotEmpty()) {
                item(key = GROUPS_KEY, contentType = HEADER_CONTENT_TYPE) {
                    ListHeaderItem(text = stringResource(id = R.string.calendarFilterGroup))
                }
                items(uiState.groups, key = { it.contextId }, contentType = { FILTER_ITEM_CONTENT_TYPE }) { group ->
                    val selected = group.contextId == uiState.selectedCanvasContext?.contextId
                    SelectCalendarItem(group, selected, onCalendarSelected, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun SelectCalendarItem(
    canvasContext: CanvasContext,
    selected: Boolean,
    onCalendarSelected: (CanvasContext) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(
        if (canvasContext is User) {
            ThemePrefs.brandColor
        } else {
            canvasContext.backgroundColor
        }
    )
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp)
            .clickable {
                onCalendarSelected(canvasContext)
            }
            .padding(start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected, onClick = {
                onCalendarSelected(canvasContext)
            }, colors = RadioButtonDefaults.colors(
                selectedColor = color,
                unselectedColor = color
            )
        )
        Text(canvasContext.name.orEmpty(), color = colorResource(id = R.color.textDarkest), fontSize = 16.sp)
    }
}

data class SelectCalendarUiState(
    val show: Boolean = false,
    val selectedCanvasContext: CanvasContext? = null,
    val canvasContexts: List<CanvasContext> = emptyList()
) {
    val users: List<CanvasContext>
        get() = canvasContexts.filter { it.isUser }
    val courses: List<CanvasContext>
        get() = canvasContexts.filter { it.isCourse }
    val groups: List<CanvasContext>
        get() = canvasContexts.filter { it.isGroup }

}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
private fun SelectCalendarPreview() {
    ContextKeeper.appContext = LocalContext.current
    AndroidThreeTen.init(LocalContext.current)
    SelectCalendarScreen(
        uiState = SelectCalendarUiState(
            show = true,
            selectedCanvasContext = Course(id = 2),
            canvasContexts = listOf(
                Course(id = 1, name = "Black Holes"),
                Course(id = 2, name = "Cosmology"),
                Course(id = 3, name = "Life in the Universe"),
            )
        ),
        onCalendarSelected = {},
        navigationActionClick = {}
    )
}