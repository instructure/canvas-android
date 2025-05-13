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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.GroupedListView
import com.instructure.pandautils.compose.composables.GroupedListViewEvent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SearchBarLive
import com.instructure.pandautils.features.assignments.list.AssignmentGroupItemState
import com.instructure.pandautils.features.assignments.list.AssignmentListScreenEvent
import com.instructure.pandautils.features.assignments.list.AssignmentListScreenOption
import com.instructure.pandautils.features.assignments.list.AssignmentListUiState
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListFilterScreen
import com.instructure.pandautils.features.assignments.list.filter.AssignmentStatusFilterOption
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
    listActionHandler: (GroupedListViewEvent<AssignmentGroupItemState>) -> Unit
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
                state.filterOptions?.let {
                    AssignmentListFilterScreen(
                        state.course.name,
                        Color(state.course.color),
                        state.filterOptions.assignmentFilters,
                        state.filterOptions.assignmentStatusFilters,
                        state.filterOptions.groupByOptions,
                        state.filterOptions.gradingPeriodOptions,
                        state.selectedFilterData,
                        { screenActionHandler(AssignmentListScreenEvent.UpdateFilterState(it)) },
                        { screenActionHandler(AssignmentListScreenEvent.CloseFilterScreen) }
                    )
                }
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
                if (!state.searchBarExpanded) {
                    val isAssignmentFilterActive = !(state.selectedFilterData.selectedAssignmentFilters.isEmpty()
                            || state.selectedFilterData.selectedAssignmentFilters.contains(AssignmentFilter.All)
                            || state.selectedFilterData.selectedAssignmentFilters.containsAll(state.filterOptions?.assignmentFilters?.assignmentFilterOptions ?: emptyList()))
                    val isStatusFilterActive = !(state.selectedFilterData.selectedAssignmentStatusFilter == null
                            || state.selectedFilterData.selectedAssignmentStatusFilter == AssignmentStatusFilterOption.All)
                    val isGradingPeriodFilterActive = state.selectedFilterData.selectedGradingPeriodFilter != state.currentGradingPeriod
                    val isFilterActive = isAssignmentFilterActive || isStatusFilterActive || isGradingPeriodFilterActive
                    IconButton(onClick = { screenActionHandler(AssignmentListScreenEvent.OpenFilterScreen) }) {
                        Icon(
                            painter = painterResource(id = if (isFilterActive) R.drawable.ic_filter_filled else R.drawable.ic_filter),
                            tint = colorResource(R.color.backgroundLightest),
                            contentDescription = stringResource(R.string.a11y_filterAssignments)
                        )
                    }
                }
                SearchBarLive(
                    icon = R.drawable.ic_search_white_24dp,
                    tintColor = colorResource(R.color.backgroundLightest),
                    placeholder = stringResource(R.string.a11y_searchAssignments),
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
                        modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)),
                        showMenu = state.overFlowItemsExpanded,
                        onDismissRequest = {
                            screenActionHandler(
                                AssignmentListScreenEvent.ChangeOverflowMenuState(
                                    !state.overFlowItemsExpanded
                                )
                            )
                        },
                        iconColor = colorResource(R.color.backgroundLightest)
                    ) {
                        state.overFlowItems.forEach { item ->
                            DropdownMenuItem(
                                modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)),
                                onClick = {
                                    item.onClick()
                                    screenActionHandler(
                                        AssignmentListScreenEvent.ChangeOverflowMenuState(
                                            !state.overFlowItemsExpanded
                                        )
                                    )
                                },
                            ) {
                                Text(item.label, color = colorResource(id = R.color.textDarkest))
                            }
                        }
                    }
                }
            }
        },
        navIconRes = if (state.searchBarExpanded) {
            null
        } else {
            R.drawable.ic_back_arrow
        },
        backgroundColor = Color(state.course.color),
        contentColor = colorResource(R.color.backgroundLightest),
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
    listActionHandler: (GroupedListViewEvent<AssignmentGroupItemState>) -> Unit
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
                AssignmentListScreenContainer(modifier = modifier) {
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.backgroundLightest))
                        .verticalScroll(rememberScrollState())
                        .testTag("assignmentList"),
                ) {
                    if (state.gradingPeriods.isNotEmpty()) {
                        GradingPeriodHeader(state.selectedFilterData.selectedGradingPeriodFilter)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    EmptyContent(
                        emptyMessage = stringResource(R.string.noAssignments),
                        imageRes = R.drawable.ic_no_events
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            ScreenState.Error -> {
                AssignmentListScreenContainer(modifier = modifier) {
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
private fun AssignmentListScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.backgroundLightest))
            .verticalScroll(rememberScrollState())
            .testTag("assignmentList"),
    ) {
        content()
    }
}

@Composable
private fun AssignmentListContentView(
    state: AssignmentListUiState,
    contextColor: Color,
    listActionHandler: (GroupedListViewEvent<AssignmentGroupItemState>) -> Unit
) {
    GroupedListView(
        modifier = Modifier
            .testTag("assignmentList"),
        items = state.listState,
        itemView = { item, modifier -> AssignmentListItemView(item, contextColor, modifier) },
        actionHandler = listActionHandler,
        headerView = if (state.gradingPeriods.isEmpty()) {
            null
        } else {
            { GradingPeriodHeader(state.selectedFilterData.selectedGradingPeriodFilter) }
        }
    )
}

@Composable
private fun GradingPeriodHeader(selectedGradingPeriod: GradingPeriod?) {
    val gradingPeriodName = selectedGradingPeriod?.title?.orEmpty() ?: stringResource(R.string.all)
    val gradingPeriodContentDescription = stringResource(R.string.gradingPeriod) + " " + if (selectedGradingPeriod != null) selectedGradingPeriod.title else stringResource(R.string.all)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .clearAndSetSemantics { contentDescription = gradingPeriodContentDescription }
    ) {
        Text(
            stringResource(R.string.gradingPeriod),
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            gradingPeriodName,
            color = colorResource(id = R.color.textDarkest),
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 20.dp)
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
                    val dueDate = assignment.dueDate
                    Text(
                        if (dueDate == null) {
                            stringResource(R.string.noDueDate)
                        } else {
                            stringResource(
                                R.string.dueAssignmentListItem,
                                dueDate.toFormattedString()
                            )
                        },
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
                        pluralStringResource(
                            R.plurals.assignmentListMaxpoints,
                            assignment.pointsPossible.toInt(),
                            assignment.pointsPossible.toInt()
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

@Composable
@Preview
fun AssignmentListPreview() {
    ContextKeeper.appContext = LocalContext.current
    val course = Course(name = "Course 1", courseColor = Color.Magenta.toString())
    val state = AssignmentListUiState(
        course = course,
        subtitle = "Course 1",
        state = ScreenState.Content,
        listState = mapOf(
            "Group 1" to listOf(
                AssignmentGroupItemState(
                    course,
                    Assignment(name = "Assignment 1"),
                    showDueDate = true,
                    showGrade = true,
                    showSubmissionState = true
                ),
                AssignmentGroupItemState(
                    course,
                    Assignment(name = "Assignment 2"),
                    showPublishStateIcon = true,
                    showClosedState = true,
                    showDueDate = true,
                    showMaxPoints = true,
                ),
            ),
            "Group 2" to listOf(
                AssignmentGroupItemState(
                    course,
                    Assignment(name = "Assignment 3"),
                    showDueDate = true,
                    showGrade = true,
                    showSubmissionState = true
                ),
                AssignmentGroupItemState(
                    course,
                    Assignment(name = "Assignment 4"),
                    showPublishStateIcon = true,
                    showClosedState = true,
                    showDueDate = true,
                    showMaxPoints = true,
                ),
            )
        ),
    )

    AssignmentListScreen(
        title = "Assignment list",
        state,
        Color(course.color),
        {},
        {}
    )
}