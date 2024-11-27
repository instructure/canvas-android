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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.canvasapi2.models.CanvasContext
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
        SummaryContent(
            uiState = uiState,
            onRefresh = { summaryViewModel.refresh() },
            navigateToAssignmentDetails = navigateToAssignmentDetails,
            navigateToCalendarEvent = navigateToCalendarEvent)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SummaryContent(
    uiState: SummaryUiState,
    onRefresh: () -> Unit,
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit,
) {
    val pullToRefreshState = rememberPullRefreshState(refreshing = (uiState.state == ScreenState.Loading), onRefresh = {
        onRefresh()
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullToRefreshState)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (uiState.state) {
                is ScreenState.Loading -> {
                    SummaryLoadingScreen()
                }

                is ScreenState.Error -> {
                    SummaryErrorScreen(onRefresh)
                }

                is ScreenState.Empty -> {
                    SummaryEmptyScreen()
                }

                is ScreenState.Content -> {
                    SummaryContentScreen(uiState.items, uiState.courseId, navigateToAssignmentDetails, navigateToCalendarEvent)
                }
            }
        }

        PullRefreshIndicator(
            refreshing = (uiState.state == ScreenState.Loading),
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
        )
    }
}

@Composable
private fun SummaryLoadingScreen() {
    Loading(modifier = Modifier.testTag("Loading"))
}

@Composable
private fun SummaryErrorScreen(
    onRefresh: () -> Unit
) {
    ErrorContent(
        errorMessage = stringResource(R.string.failed_to_load_summary),
        retryClick = onRefresh,
        )
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
    courseId: Long,
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 64.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) {
            ScheduleItemRow(it, courseId, navigateToAssignmentDetails, navigateToCalendarEvent)
        }
    }
}

@Composable
private fun ScheduleItemRow(
    scheduleItem: ScheduleItem,
    courseId: Long,
    navigateToAssignmentDetails: (Long, Long) -> Unit,
    navigateToCalendarEvent: (String, Long, Long) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (scheduleItem.assignment != null) {
                    navigateToAssignmentDetails(
                        scheduleItem.courseId,
                        scheduleItem.assignment?.id ?: 0
                    )
                } else {
                    val uri = Uri.parse(scheduleItem.htmlUrl)
                    val eventId = uri
                        .getQueryParameter("event_id")
                        ?.toLongOrNull() ?: 0
                    navigateToCalendarEvent (CanvasContext.Type.COURSE.apiString, courseId, eventId)
                }
            }
    ) {
        Icon(
            painter = painterResource(id = scheduleItem.iconRes),
            contentDescription = "Summary Item Icon",
            tint = Color(CanvasContext.emptyCourseContext(courseId).color),
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