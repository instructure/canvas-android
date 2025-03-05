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
package com.instructure.teacher.features.submission

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.teacher.R
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter

@Composable
fun SubmissionListScreen(uiState: SubmissionListUiState, navigationIconClick: () -> Unit) {
    Scaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        topBar = {
            CanvasAppBar(
                title = stringResource(R.string.submissions),
                subtitle = uiState.assignmentName,
                navigationActionClick = { navigationIconClick() },
                navIconContentDescription = stringResource(R.string.back),
                navIconRes = R.drawable.ic_back_arrow,
                backgroundColor = uiState.courseColor,
                textColor = colorResource(id = R.color.textLightest),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(
                                id = if (uiState.filter != SubmissionListFilter.ALL || uiState.sections.isNotEmpty()) {
                                    R.drawable.ic_filter_filled
                                } else {
                                    R.drawable.ic_filter_outline
                                }
                            ),
                            contentDescription = stringResource(R.string.a11y_contentDescription_filter),
                            tint = colorResource(id = R.color.textLightest)
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = stringResource(R.string.a11y_contentDescription_postPolicy),
                            tint = colorResource(id = R.color.textLightest)
                        )
                    }
                }
            )
        }
    ) { padding ->
        SubmissionListContent(uiState, Modifier.padding(padding), uiState.courseColor)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SubmissionListContent(
    uiState: SubmissionListUiState,
    modifier: Modifier = Modifier,
    courseColor: Color
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.refreshing, onRefresh = {
        uiState.actionHandler(SubmissionListAction.Refresh)
    })
    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)) {
        LazyColumn(modifier = modifier) {
            uiState.submissions.forEach { (titleRes, submissionList) ->
                SubmissionListSection(
                    titleRes,
                    submissionList,
                    courseColor
                ) {
                    uiState.actionHandler(SubmissionListAction.SubmissionClicked(it))
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.refreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("pullRefreshIndicator"),
            contentColor = uiState.courseColor
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.SubmissionListSection(
    headerTitle: Int,
    submissions: List<SubmissionUiState>,
    courseColor: Color,
    itemClick: (Long) -> Unit
) {
    stickyHeader {
        SubmissionListHeader(titleRes = headerTitle)
    }
    items(submissions) {
        SubmissionListItem(it, courseColor, itemClick)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubmissionListItem(
    submissionListUiState: SubmissionUiState,
    courseColor: Color,
    itemClick: (Long) -> Unit
) {
    Column(modifier = Modifier.clickable { itemClick(submissionListUiState.submissionId) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp),
                imageUrl = submissionListUiState.avatarUrl,
                name = submissionListUiState.userName,
            )
            Column {
                Text(
                    text = submissionListUiState.userName,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = colorResource(id = R.color.textDarkest),
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow {
                    submissionListUiState.tags.forEach { tag ->
                        SubmissionTag(tag, tag != submissionListUiState.tags.last())
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = submissionListUiState.grade ?: "-",
                fontSize = 16.sp,
                color = courseColor,
                fontWeight = FontWeight.SemiBold
            )
        }
        CanvasDivider()
    }

}

@Composable
private fun SubmissionTag(tag: SubmissionTag, hasDivider: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        tag.icon?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = stringResource(tag.text),
                tint = colorResource(id = tag.color ?: R.color.textDark),
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 1.dp)
            )
        }

        Text(
            text = stringResource(tag.text),
            fontSize = 12.sp,
            color = colorResource(id = tag.color ?: R.color.textDark),
            modifier = Modifier.padding(end = 4.dp)
        )

        if (hasDivider) {
            Text(
                text = "|",
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }

}

@Composable
private fun SubmissionListHeader(@StringRes titleRes: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(titleRes),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                modifier = Modifier.width(12.dp),
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = stringResource(R.string.a11y_contentDescription_expand),
                tint = colorResource(id = R.color.textDarkest)
            )
        }
        CanvasDivider()
    }

}

@Preview
@Composable
fun SubmissionListScreenPreview() {
    SubmissionListScreen(
        SubmissionListUiState(
            "Test course",
            courseColor = Color.Magenta,
            SubmissionListFilter.GRADED,
            submissions = mapOf(
                R.string.submitted to listOf(
                    SubmissionUiState(
                        1,
                        "Test User",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                        null
                    ),
                    SubmissionUiState(
                        2,
                        "Test User 2",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.SUBMITTED, SubmissionTag.NEEDS_GRADING),
                        null
                    )
                ),
                R.string.not_submitted to listOf(
                    SubmissionUiState(
                        3,
                        "Test User 3",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.NOT_SUBMITTED),
                        null
                    ),
                    SubmissionUiState(
                        4,
                        "Test User 4",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.MISSING),
                        null
                    )
                ),
                R.string.graded to listOf(
                    SubmissionUiState(
                        5,
                        "Test User 5",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.GRADED),
                        "100%"
                    ),
                    SubmissionUiState(
                        6,
                        "Test User 6",
                        "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50",
                        listOf(SubmissionTag.EXCUSED),
                        "Excused"
                    )
                )
            )
        ) {}
    ) {}
}