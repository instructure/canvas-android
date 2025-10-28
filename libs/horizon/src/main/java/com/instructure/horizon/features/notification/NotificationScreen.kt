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
package com.instructure.horizon.features.notification

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.isPreviousDay
import com.instructure.pandautils.utils.isSameDay
import com.instructure.pandautils.utils.isSameWeek
import com.instructure.pandautils.utils.localisedFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(state: NotificationUiState, mainNavController: NavHostController) {
    val activity = LocalContext.current.getActivityOrNull()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))
    }

    HorizonScaffold(
        title = stringResource(R.string.notificationsTitle),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onBackPressed = { mainNavController.popBackStack() },
    ) { modifier ->
        LoadingStateWrapper(state.screenState) {
            NotificationContent(
                mainNavController,
                state,
                showSnackbar = { message ->
                    scope.launch { snackbarHostState.showSnackbar(message) }
                },
                modifier
            )
        }
    }
}

@Composable
private fun NotificationContent(
    navController: NavHostController,
    state: NotificationUiState,
    showSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(HorizonColors.Surface.pageSecondary())
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            if (state.notificationItems.isEmpty()) {
                item {
                    EmptyNotificationItemContent()
                }
            } else {
                items(state.notificationItems) { item ->
                    NotificationItemContent(navController, item, showSnackbar)
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationItemContent() {
    Text(
        stringResource(R.string.notificationsEmptyMessage),
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun NotificationItemContent(
    navController: NavHostController,
    notificationItem: NotificationItem,
    showSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .border(
                HorizonBorder.level2(HorizonColors.LineAndBorder.lineStroke()),
                HorizonCornerRadius.level2
            )
            .clip(HorizonCornerRadius.level2)
            .fillMaxWidth()
            .clickable {
                val request = NavDeepLinkRequest.Builder
                    .fromUri(notificationItem.deepLink.toUri())
                    .build()

                try {
                    navController.navigate(request)
                } catch (e: IllegalArgumentException) {
                    showSnackbar(context.getString(R.string.notificationsFailedToOpenMessage))
                }
            }
            .padding(horizontal = 16.dp)
    ) {
        HorizonSpace(SpaceSize.SPACE_16)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusChip(
                state = StatusChipState(
                    label = notificationItem.category.label,
                    color = notificationItem.category.color,
                    fill = true
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!notificationItem.isRead){
                Badge(
                    content = BadgeContent.ColorSmall,
                    type = BadgeType.Custom(
                        backgroundColor = HorizonColors.Surface.inversePrimary(),
                        contentColor = HorizonColors.Surface.institution()
                    )
                )
            }
        }

        HorizonSpace(SpaceSize.SPACE_8)

        if (notificationItem.courseLabel != null) {
            Text(
                text = notificationItem.courseLabel,
                style = HorizonTypography.p3,
                color = HorizonColors.Text.timestamp()
            )
            HorizonSpace(SpaceSize.SPACE_8)
        }
        Text(
            text = notificationItem.title,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        HorizonSpace(SpaceSize.SPACE_4)

        Text(
            text = notificationItem.date.toLocalisedFormat(LocalContext.current),
            style = HorizonTypography.p3,
            color = HorizonColors.Text.timestamp()
        )
        HorizonSpace(SpaceSize.SPACE_16)
    }
}

private fun Date?.toLocalisedFormat(context: Context): String {
    if (this == null) return ""
    if (this.isSameDay(Date())) {
        return context.getString(R.string.notificationsDateToday)
    }
    if (this.isPreviousDay(Date())) {
        return context.getString(R.string.notificationsDateYesterday)
    }
    if (this.isSameWeek(Date())) {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(this)
    }
    return this.localisedFormat("MMM dd, yyyy")
}

@Composable
@Preview
private fun NotificationsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val sampleNotifications = listOf(
        NotificationItem(
            category = NotificationItemCategory(
                stringResource(R.string.notificationsDueDateCategoryLabel),
                StatusChipColor.Honey
            ),
            title = "Your assignment is due soon",
            courseLabel = "Biology 101",
            date = Date(),
            isRead = false,
            deepLink = "myapp://course/1/assignment/1"
        ),
        NotificationItem(
            category = NotificationItemCategory(
                stringResource(R.string.notificationsScoreChangedCategoryLabel),
                StatusChipColor.Violet
            ),
            title = "Your score has been updated",
            courseLabel = "Math 201",
            date = Calendar.getInstance().apply { time = Date(); add(Calendar.DAY_OF_WEEK, -1) }.time,
            isRead = true,
            deepLink = "course/2/grade"
        ),
    )

    val previewState = NotificationUiState(
        screenState = LoadingState(),
        notificationItems = sampleNotifications
    )

    NotificationContent(navController = rememberNavController(), state = previewState, {})
}