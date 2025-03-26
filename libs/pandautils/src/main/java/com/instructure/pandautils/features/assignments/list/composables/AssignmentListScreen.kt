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
package com.instructure.pandautils.features.assignments.list.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.GroupedListView
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.compose.composables.LiveSearchBar
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentGroupState
import com.instructure.pandautils.features.assignments.list.AssignmentListScreenEvent
import com.instructure.pandautils.features.assignments.list.AssignmentListScreenOption
import com.instructure.pandautils.features.assignments.list.AssignmentListUiState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterOption
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterScreen
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getGrade
import com.instructure.pandautils.utils.getSubmissionStateLabel
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toFormattedString
import java.util.Date

@Composable
fun AssignmentListScreen(
    title: String,
    state: AssignmentListUiState,
    contextColor: Color,
    screenActionHandler: (AssignmentListScreenEvent) -> Unit,
    listActionHandler: (GroupedListViewEvent<AssignmentGroupState, AssignmentGroupItemState>) -> Unit
) {
    CanvasTheme {
        when (state.screenOption) {
            AssignmentListScreenOption.List -> {
                Scaffold(
                    backgroundColor = colorResource(id = R.color.backgroundLightest),
                    topBar = {
                        AppBar(
                            title,
                            state,
                            screenActionHandler
                        )
                    },
                    content = { paddingValues ->
                        AssignmentListWrapper(
                            state,
                            contextColor,
                            Modifier.padding(paddingValues),
                            screenActionHandler,
                            listActionHandler
                        )
                    }
                )
            }

            AssignmentListScreenOption.Filter -> {
                AssignmentListFilterScreen(
                    state.filterState,
                    { screenActionHandler(AssignmentListScreenEvent.UpdateFilterState(it)) },
                    { screenActionHandler(AssignmentListScreenEvent.CloseFilterScreen) }
                )
            }
        }
    }
}

@Composable
private fun AppBar(
    title: String,
    state: AssignmentListUiState,
    screenActionHandler: (AssignmentListScreenEvent) -> Unit
) {
    CanvasThemedAppBar(
        title = title,
        subtitle = state.subtitle,
        actions = {
            if (state.state != ScreenState.Loading) {
                IconButton(onClick = { screenActionHandler(AssignmentListScreenEvent.OpenFilterScreen) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = stringResource(R.string.a11y_filterAssignments)
                    )
                }
                LiveSearchBar(
                    icon = R.drawable.ic_search_white_24dp,
                    tintColor = colorResource(com.instructure.pandautils.R.color.backgroundLightest),
                    placeholder = "Search Assignments",
                    query = state.searchQuery,
                    queryChanged = {
                        screenActionHandler(
                            AssignmentListScreenEvent.SearchContentChanged(
                                it
                            )
                        )
                    },
                    expanded = state.searchBarExpanded,
                    onExpand = {
                        screenActionHandler(AssignmentListScreenEvent.SearchContentChanged(""))
                        screenActionHandler(AssignmentListScreenEvent.ExpandCollapseSearchBar(it))
                    },
                )
                if (state.overFlowItems.isNotEmpty()) {
                    OverflowMenu(
                        showMenu = state.overFlowItemsExpanded,
                        onDismissRequest = {
                            screenActionHandler(
                                AssignmentListScreenEvent.ChangeOverflowMenuState(
                                    !state.overFlowItemsExpanded
                                )
                            )
                        }
                    ) {
                        state.overFlowItems.forEach { item ->
                            DropdownMenuItem(
                                onClick = {
                                    item.onClick()
                                    screenActionHandler(
                                        AssignmentListScreenEvent.ChangeOverflowMenuState(
                                            !state.overFlowItemsExpanded
                                        )
                                    )
                                },
                            ) {
                                Text(item.label)
                            }
                        }
                    }
                }
            }
        },
        backgroundColor = Color(state.course.color),
        contentColor = colorResource(com.instructure.pandautils.R.color.backgroundLightest),
        navigationActionClick = { screenActionHandler(AssignmentListScreenEvent.NavigateBack) }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AssignmentListWrapper(
    state: AssignmentListUiState,
    contextColor: Color,
    modifier: Modifier = Modifier,
    screenActionHandler: (AssignmentListScreenEvent) -> Unit,
    listActionHandler: (GroupedListViewEvent<AssignmentGroupState, AssignmentGroupItemState>) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = {
            screenActionHandler(AssignmentListScreenEvent.Refresh)
        }
    )

    Box(
        modifier = modifier
            .pullRefresh(pullRefreshState)
    ) {
        when (state.state) {
            ScreenState.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.backgroundLightest))
                ) {
                    Loading()
                }
            }

            ScreenState.Content -> {
                AssignmentListContentView(
                    state,
                    contextColor,
                    listActionHandler
                )
            }

            ScreenState.Empty -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.backgroundLightest))
                        .verticalScroll(rememberScrollState())
                        .testTag("assignmentList"),
                ){
                    EmptyContent(
                        emptyMessage = stringResource(R.string.noAssignments),
                        imageRes = R.drawable.ic_no_events
                    )
                }
            }

            ScreenState.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.backgroundLightest))
                        .verticalScroll(rememberScrollState())
                        .testTag("assignmentList"),
                ){
                    ErrorContent(
                        errorMessage = stringResource(R.string.errorLoadingAssignments),
                        retryClick = { screenActionHandler(AssignmentListScreenEvent.Refresh) }
                    )
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = state.isRefreshing,
            state = pullRefreshState,
            contentColor = Color(state.course.color),
        )
    }
}

@Composable
private fun AssignmentListContentView(
    state: AssignmentListUiState,
    contextColor: Color,
    listActionHandler: (GroupedListViewEvent<AssignmentGroupState, AssignmentGroupItemState>) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.gradingPeriod),
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.weight(1f))

            val gradingPeriodGroup = state.filterState.filterGroups.firstOrNull { it.options.any { it is AssignmentListFilterOption.GradingPeriod } }
            val selectedGradingPeriod = gradingPeriodGroup?.options?.get(gradingPeriodGroup.selectedOptionIndexes.firstOrNull().orDefault()) as? AssignmentListFilterOption.GradingPeriod
            val gradingPeriodName = if (selectedGradingPeriod?.period != null) selectedGradingPeriod.stringValue else stringResource(R.string.all)
            Text(
                gradingPeriodName,
                color = colorResource(id = R.color.textDarkest),
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
            )
        }

        GroupedListView(
            modifier = Modifier
                .testTag("assignmentList"),
            state = state.listState,
            itemView = { item, modifier -> AssignmentListItemView(item, contextColor, modifier) },
            actionHandler = listActionHandler
        )
    }
}

@Composable
private fun AssignmentListItemView(item: AssignmentGroupItemState, contextColor: Color, modifier: Modifier) {
    val assignment = item.assignment
    Row(
        modifier = modifier
            .background(colorResource(R.color.backgroundLightest))
            .padding(vertical = 8.dp)
            .testTag("assignmentListItem")
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
        ) {
            Icon(
                painter = painterResource(assignment.getAssignmentIcon()),
                tint = contextColor,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
            if (item.showPublishStateIcon) {
                val publishedIcon = if (assignment.published) R.drawable.ic_complete_solid else R.drawable.ic_unpublish
                val publishColor = if (assignment.published) R.color.textSuccess else R.color.textDark
                val publishedContentDescriptionRes = if (assignment.published) R.string.published else R.string.unpublished
                Icon(
                    painter = painterResource(publishedIcon),
                    tint = colorResource(publishColor),
                    contentDescription = stringResource(publishedContentDescriptionRes),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                assignment.name.orEmpty(),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                if (item.showClosedState) {
                    if (assignment.lockDate?.before(Date()).orDefault()) {
                        Text(
                            stringResource(R.string.closed),
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                        AssignmentDivider()
                    }
                }
                if (item.showDueDate) {
                    Text(
                        assignment.dueDate?.toFormattedString() ?: stringResource(R.string.noDueDate),
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                }
                if (item.showSubmissionState) {
                    val submissionStateLabel = assignment.getSubmissionStateLabel()
                    if (submissionStateLabel != SubmissionStateLabel.NONE) {
                        AssignmentDivider()
                        Icon(
                            painter = painterResource(submissionStateLabel.iconRes),
                            tint = colorResource(submissionStateLabel.colorRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Text(
                            stringResource(submissionStateLabel.labelRes),
                            color = colorResource(submissionStateLabel.colorRes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                if (item.showGrade) {
                    val gradeText = assignment.getGrade(
                        submission = assignment.submission,
                        context = LocalContext.current,
                        restrictQuantitativeData = item.course.settings?.restrictQuantitativeData.orDefault(),
                        gradingScheme = item.course.gradingScheme,
                        showZeroPossiblePoints = true,
                        showNotGraded = true
                    )
                    Text(
                        gradeText.text,
                        color = contextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (item.showNeedsGrading) {
                    if (assignment.needsGradingCount.toInt() != 0) {
                        AssignmentNeedsGradingChip(
                            assignment.needsGradingCount.toInt(),
                            contextColor
                        )
                        AssignmentDivider()
                    }
                }
                if (item.showMaxPoints) {
                    Text(
                        stringResource(
                            R.string.assignmentListMaxpoints,
                            assignment.pointsPossible.toFormattedString()
                        ),
                        color = contextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignmentNeedsGradingChip(count: Int, contextColor: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(contextColor)
    ) {
        Log.d("AssignmentNeedsGradingChip", "count: ${stringResource(R.string.needsGradingCount, count.toString())}")
        Text(
            stringResource(R.string.needsGradingCount, count),
            modifier = Modifier
                .padding(4.dp),
            color = colorResource(R.color.textLightest),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun AssignmentDivider() {
    Row {
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            color = colorResource(id = R.color.textDark),
            thickness = 1.dp,
            modifier = Modifier
                .height(16.dp)
                .width(1.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}