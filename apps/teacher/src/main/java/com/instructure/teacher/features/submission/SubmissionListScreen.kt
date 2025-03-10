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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.SearchBar
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.teacher.R
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter

@Composable
fun SubmissionListScreen(uiState: SubmissionListUiState, navigationIconClick: () -> Unit) {
    var showFilterDialog by remember { mutableStateOf(false) }

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
                    IconButton(onClick = {
                        showFilterDialog = true
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (uiState.filter != SubmissionListFilter.ALL || uiState.selectedSections.isNotEmpty()) {
                                    R.drawable.ic_filter_filled
                                } else {
                                    R.drawable.ic_filter_outline
                                }
                            ),
                            contentDescription = stringResource(R.string.a11y_contentDescription_filter),
                            tint = colorResource(id = R.color.textLightest)
                        )
                    }

                    IconButton(onClick = { uiState.actionHandler(SubmissionListAction.ShowPostPolicy) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_eye),
                            contentDescription = stringResource(R.string.a11y_contentDescription_postPolicy),
                            tint = colorResource(id = R.color.textLightest)
                        )
                    }

                    IconButton(onClick = { uiState.actionHandler(SubmissionListAction.SendMessage) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mail),
                            contentDescription = stringResource(R.string.a11y_sendMessage),
                            tint = colorResource(id = R.color.textLightest)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (showFilterDialog) {
            SubmissionListFilters(
                uiState.filter,
                uiState.filterValue,
                uiState.courseColor,
                uiState.assignmentName,
                uiState.sections,
                uiState.selectedSections,
                uiState.actionHandler
            ) {
                showFilterDialog = false
            }
        }

        when {
            uiState.loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }
            uiState.error -> {
                ErrorContent(
                    errorMessage = stringResource(R.string.errorLoadingSubmission),
                    retryClick = { uiState.actionHandler(SubmissionListAction.Refresh) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            uiState.submissions.isEmpty() -> {
                EmptyContent(
                    emptyMessage = stringResource(R.string.no_submissions),
                    imageRes = R.drawable.ic_panda_nocourses,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                SubmissionListContent(uiState, Modifier.padding(padding), uiState.courseColor)
            }
        }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(modifier = modifier) {
            if (!uiState.anonymousGrading) {
                item {
                    SearchBar(
                        icon = R.drawable.ic_search_white_24dp,
                        searchQuery = uiState.searchQuery,
                        tintColor = colorResource(R.color.textDarkest),
                        placeholder = stringResource(R.string.search),
                        collapsable = false,
                        onSearch = {
                            uiState.actionHandler(SubmissionListAction.Search(it))
                        },
                        onClear = {
                            uiState.actionHandler(SubmissionListAction.Search(""))
                        })
                }
            }
            item { Header(uiState.headerTitle) }
            items(uiState.submissions, key = { it.submissionId }) { submission ->
                SubmissionListItem(submission, courseColor, uiState.anonymousGrading) {
                    uiState.actionHandler(SubmissionListAction.SubmissionClicked(it))
                }
                CanvasDivider()
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

@Composable
private fun Header(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLight))
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDarkest),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubmissionListItem(
    submissionListUiState: SubmissionUiState,
    courseColor: Color,
    anonymousGrading: Boolean,
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
                anonymous = anonymousGrading,
                imageUrl = submissionListUiState.avatarUrl,
                name = submissionListUiState.userName,
            )
            Column {
                Text(
                    text = if (anonymousGrading) stringResource(R.string.anonymousStudentLabel) else submissionListUiState.userName,
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
            if (submissionListUiState.hidden) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eye_off),
                    contentDescription = stringResource(R.string.a11y_hidden),
                    tint = colorResource(id = R.color.textDanger),
                    modifier = Modifier.size(24.dp).padding(start = 8.dp).testTag("hiddenIcon")
                )
            }
        }
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


@Preview
@Composable
fun SubmissionListScreenPreview() {
    SubmissionListScreen(
        SubmissionListUiState(
            "Test assignment",
            courseColor = Color.Magenta,
            headerTitle = "All Submissions",
            filter = SubmissionListFilter.GRADED,
            anonymousGrading = false,
            submissions = listOf(
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
                ),
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
                ),
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
        ) {}
    ) {}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SubmissionListScreenDarkPreview() {
    SubmissionListScreen(
        SubmissionListUiState(
            "Test assignment",
            courseColor = Color.Magenta,
            anonymousGrading = false,
            headerTitle = "All Submissions",
            filter = SubmissionListFilter.GRADED,
            submissions = listOf(
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
                ),
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
                ),
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
                    "Excused",
                    hidden = true
                )
            )
        ) {}
    ) {}
}

@Preview
@Composable
private fun SubmissionListErrorPreview() {
    SubmissionListScreen(
        SubmissionListUiState(
            "Test assignment",
            courseColor = Color.Magenta,
            anonymousGrading = false,
            headerTitle = "All Submissions",
            filter = SubmissionListFilter.GRADED,
            error = true
        ) {}
    ) {}
}

@Preview
@Composable
private fun SubmissionListEmptyPreview() {
    SubmissionListScreen(
        SubmissionListUiState(
            "Test assignment",
            courseColor = Color.Magenta,
            anonymousGrading = false,
            headerTitle = "All Submissions",
            filter = SubmissionListFilter.GRADED,
            submissions = emptyList()
        ) {}
    ) {}
}
