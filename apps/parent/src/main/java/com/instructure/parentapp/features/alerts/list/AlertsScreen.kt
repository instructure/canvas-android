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
@file:OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)

package com.instructure.parentapp.features.alerts.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.drawableId
import java.util.Date


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlertsScreen(
    uiState: AlertsUiState,
    actionHandler: (AlertsAction) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = LazyListState()
) {
    CanvasTheme {
        CanvasScaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            content = { padding ->
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.isRefreshing,
                    onRefresh = {
                        actionHandler(AlertsAction.Refresh)
                    }
                )
                Box(modifier = modifier.pullRefresh(pullRefreshState)) {
                    when {
                        uiState.isError -> {
                            ErrorContent(
                                errorMessage = stringResource(id = R.string.errorLoadingAlerts),
                                retryClick = {
                                    actionHandler(AlertsAction.Refresh)
                                }, modifier = Modifier.fillMaxSize()
                            )
                        }

                        uiState.isLoading -> {
                            Loading(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("loading"),
                                color = Color(uiState.studentColor)
                            )
                        }

                        uiState.alerts.isEmpty() -> {
                            EmptyContent(
                                emptyTitle = stringResource(id = R.string.parentNoAlerts),
                                emptyMessage = stringResource(id = R.string.parentNoAlersMessage),
                                imageRes = R.drawable.ic_panda_noalerts,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("emptyAlerts")
                                    .verticalScroll(rememberScrollState())
                            )
                        }

                        else -> {
                            AlertsListContent(
                                uiState = uiState,
                                actionHandler = actionHandler,
                                lazyListState = lazyListState,
                                scaffoldPadding = padding,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = uiState.isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .testTag("pullRefreshIndicator"),
                        contentColor = Color(uiState.studentColor)
                    )
                }

            },
            modifier = modifier
        )
    }
}

@Composable
fun AlertsListContent(
    uiState: AlertsUiState,
    actionHandler: (AlertsAction) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier.testTag("alertsList"),
        contentPadding = scaffoldPadding
    ) {
        items(uiState.alerts, key = { it.alertId }) { alert ->
            AlertsListItem(
                alert = alert,
                userColor = uiState.studentColor,
                actionHandler = actionHandler,
                modifier = Modifier.animateItem()
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
    LaunchedEffect(uiState.addedItemIndex) {
        if (uiState.addedItemIndex != -1) {
            val firstVisibleIndex = lazyListState.firstVisibleItemIndex
            if (uiState.addedItemIndex < firstVisibleIndex && firstVisibleIndex > 0) {
                lazyListState.animateScrollToItem(lazyListState.firstVisibleItemIndex - 1)
            }
        }
    }
}

@Composable
fun AlertsListItem(
    alert: AlertsItemUiState,
    userColor: Int,
    actionHandler: (AlertsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    fun alertTitle(alertType: AlertType, alertThreshold: String?): String {
        val threshold = alertThreshold.orEmpty()
        return when (alertType) {
            AlertType.ASSIGNMENT_MISSING -> context.getString(R.string.assignmentMissingAlertTitle)
            AlertType.ASSIGNMENT_GRADE_HIGH -> context.getString(
                R.string.assignmentGradeHighAlertTitle,
                threshold
            )

            AlertType.ASSIGNMENT_GRADE_LOW -> context.getString(
                R.string.assignmentGradeLowAlertTitle,
                threshold
            )

            AlertType.COURSE_GRADE_HIGH -> context.getString(
                R.string.courseGradeHighAlertTitle,
                threshold
            )

            AlertType.COURSE_GRADE_LOW -> context.getString(
                R.string.courseGradeLowAlertTitle,
                threshold
            )

            AlertType.COURSE_ANNOUNCEMENT -> context.getString(R.string.courseAnnouncementAlertTitle)
            AlertType.INSTITUTION_ANNOUNCEMENT -> context.getString(R.string.institutionAnnouncementAlertTitle)
        }
    }

    fun alertIcon(alertType: AlertType, lockedForUser: Boolean): Int {
        return when {
            lockedForUser -> R.drawable.ic_lock_lined
            alertType.isAlertInfo() || alertType.isAlertPositive() -> R.drawable.ic_info
            alertType.isAlertNegative() -> R.drawable.ic_warning
            else -> R.drawable.ic_info
        }
    }

    fun alertColor(alertType: AlertType): Int {
        return when {
            alertType.isAlertInfo() -> context.getColor(R.color.textDark)
            alertType.isAlertNegative() -> context.getColor(R.color.textDanger)
            alertType.isAlertPositive() -> userColor
            else -> context.getColor(R.color.textDark)
        }
    }

    Row(modifier = modifier
        .fillMaxWidth()
        .clickable {
            actionHandler(
                AlertsAction.Navigate(
                    alert.alertId,
                    alert.contextId,
                    alert.htmlUrl,
                    alert.alertType
                )
            )
        }
        .padding(8.dp)
        .testTag("alertItem")
        .semantics {
            role = Role.Button
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.align(Alignment.Top)) {
            if (alert.unread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(userColor))
                        .testTag("unreadIndicator")
                )
            }

            val iconId = alertIcon(alert.alertType, alert.lockedForUser)
            Icon(
                modifier = Modifier
                    .padding(start = if (alert.unread) 0.dp else 8.dp, end = 32.dp)
                    .semantics {
                        drawableId = iconId
                    },
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = Color(alertColor(alert.alertType))
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alertTitle(alert.alertType, alert.observerAlertThreshold),
                color = Color(alertColor(alert.alertType)),
                fontSize = 12.sp
            )
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = alert.title,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            alert.date?.let {
                Text(
                    text = DateHelper.getDateAtTimeString(
                        LocalContext.current,
                        com.instructure.pandares.R.string.alertDateTime,
                        it
                    ) ?: "",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.textDark)
                )
            }
        }
        IconButton(
            modifier = Modifier
                .testTag("dismissButton"),
            onClick = { actionHandler(AlertsAction.DismissAlert(alert.alertId)) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                tint = colorResource(id = R.color.textDark),
                contentDescription = stringResource(
                    id = R.string.a11y_contentDescription_observerAlertDelete
                )
            )
        }
    }
}

@Preview
@Composable
fun AlertsScreenPreview() {
    AlertsScreen(
        uiState = AlertsUiState(
            alerts = listOf(
                AlertsItemUiState(
                    alertId = 1L,
                    contextId = 1L,
                    title = "Alert title",
                    alertType = AlertType.COURSE_ANNOUNCEMENT,
                    date = Date(),
                    observerAlertThreshold = null,
                    lockedForUser = false,
                    unread = true,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 2L,
                    contextId = 2L,
                    title = "Assignment missing",
                    alertType = AlertType.ASSIGNMENT_MISSING,
                    date = Date(),
                    observerAlertThreshold = null,
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 3L,
                    contextId = 3L,
                    title = "Course grade low",
                    alertType = AlertType.COURSE_GRADE_LOW,
                    date = Date(),
                    observerAlertThreshold = "8",
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 4L,
                    contextId = 4L,
                    title = "Course grade high",
                    alertType = AlertType.COURSE_GRADE_HIGH,
                    date = Date(),
                    observerAlertThreshold = "80%",
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 5L,
                    contextId = 5L,
                    title = "Institution announcement",
                    alertType = AlertType.INSTITUTION_ANNOUNCEMENT,
                    date = Date(),
                    observerAlertThreshold = null,
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 6L,
                    contextId = 6L,
                    title = "Assignment grade low",
                    alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                    date = Date(),
                    observerAlertThreshold = "8",
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 7L,
                    contextId = 7L,
                    title = "Assignment grade high",
                    alertType = AlertType.ASSIGNMENT_GRADE_HIGH,
                    date = Date(),
                    observerAlertThreshold = "80%",
                    lockedForUser = false,
                    unread = false,
                    htmlUrl = ""
                ),
                AlertsItemUiState(
                    alertId = 8L,
                    contextId = 8L,
                    title = "Locked alert",
                    alertType = AlertType.COURSE_ANNOUNCEMENT,
                    date = Date(),
                    observerAlertThreshold = null,
                    lockedForUser = true,
                    unread = false,
                    htmlUrl = ""
                )
            )
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AlertsScreenErrorPreview() {
    AlertsScreen(
        uiState = AlertsUiState(isError = true),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AlertsScreenEmptyPreview() {
    AlertsScreen(
        uiState = AlertsUiState(),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AlertsScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    AlertsScreen(
        uiState = AlertsUiState(isLoading = true),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AlertsScreenRefreshingPreview() {
    AlertsScreen(
        uiState = AlertsUiState(isRefreshing = true),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AlertsListItemPreview() {
    AlertsListItem(
        alert = AlertsItemUiState(
            alertId = 1L,
            contextId = 1L,
            title = "Alert title",
            alertType = AlertType.COURSE_ANNOUNCEMENT,
            date = Date(),
            observerAlertThreshold = null,
            lockedForUser = false,
            unread = true,
            htmlUrl = ""
        ),
        userColor = Color.Blue.toArgb(),
        actionHandler = {}
    )
}