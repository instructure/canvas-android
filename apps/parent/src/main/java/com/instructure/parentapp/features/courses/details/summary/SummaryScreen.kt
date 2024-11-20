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

package com.instructure.parentapp.features.courses.details.summary

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDisplayDate
import com.instructure.pandautils.utils.iconRes
import com.instructure.parentapp.R

@Composable
internal fun SummaryScreen(
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit,
) {
    val summaryViewModel: SummaryViewModel = viewModel()
    val uiState by summaryViewModel.uiState.collectAsState()

    CanvasTheme {
        when (uiState.state) {
            is ScreenState.Loading -> {
                SummaryLoadingScreen()
            }

            is ScreenState.Error -> {
                SummaryErrorScreen()
            }

            is ScreenState.Empty -> {
                SummaryEmptyScreen()
            }

            is ScreenState.Content -> {
                SummaryContentScreen(uiState.items, uiState.course, navigateToAssignmentDetails, navigateToCalendarEvent)
            }
        }
    }
}

@Composable
private fun SummaryLoadingScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Loading()
    }
}

@Composable
private fun SummaryErrorScreen() {
    ErrorContent(errorMessage = stringResource(R.string.failed_to_load_summary))
}

@Composable
private fun SummaryEmptyScreen() {
    EmptyContent(
        imageRes = R.drawable.ic_panda_nosyllabus,
        emptyMessage = stringResource(R.string.no_summary_items_to_display)
    )
}

@Composable
private fun SummaryContentScreen(
    items: List<ScheduleItem>,
    course: Course,
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        items.forEach {
            ScheduleItemRow(it, course, navigateToAssignmentDetails, navigateToCalendarEvent)
        }
    }
}

@Composable
private fun ScheduleItemRow(
    scheduleItem: ScheduleItem,
    course: Course,
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (scheduleItem.type == "event") {
                    val uri = Uri.parse(scheduleItem.htmlUrl)
                    val eventId = uri.getQueryParameter("event_id")?.toLongOrNull() ?: 0
                    navigateToCalendarEvent(course.type.apiString, course.id, eventId)
                } else if (scheduleItem.assignment != null) {
                    navigateToAssignmentDetails(scheduleItem.courseId, scheduleItem.assignment?.id ?: 0)

                }
            }
    ) {
        Icon(
            painter = painterResource(id = scheduleItem.iconRes),
            contentDescription = "Summary Item Icon",
            tint = Color(course.color),
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp)
        )

        Column {
            Text(
                text = scheduleItem.title.orEmpty(),
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )

            Text(
                text = scheduleItem.getDisplayDate(context = LocalContext.current),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }
    }
}