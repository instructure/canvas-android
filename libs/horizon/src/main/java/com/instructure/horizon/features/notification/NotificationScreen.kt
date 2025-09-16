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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.localisedFormat
import java.time.Duration
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(state: NotificationUiState, mainNavController: NavController) {
    HorizonScaffold(
        title = stringResource(R.string.notificationsTitle),
        onBackPressed = { mainNavController.popBackStack() },
    ) { modifier ->
        LoadingStateWrapper(state.screenState) {
            NotificationContent(state, modifier)
        }
    }
}

@Composable
private fun NotificationContent(state: NotificationUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(HorizonColors.Surface.pageSecondary())
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            item {
                NotificationsHeader(state.unreadCount)
            }
            if (state.notificationItems.isEmpty()) {
                item {
                    EmptyNotificationItemContent()
                }
            } else {
                items(state.notificationItems) { item ->
                    NotificationItemContent(item)
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(unreadCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusChip(
            state = StatusChipState(
                label = stringResource(R.string.notificationsUnreadCount, unreadCount),
                color = StatusChipColor.Grey,
                fill = true
            )
        )
    }
}

@Composable
private fun EmptyNotificationItemContent() {
    Text(
        stringResource(R.string.notificationsEmptyMessage),
    )
}

@Composable
private fun NotificationItemContent(
    notificationItem: NotificationItem
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .border(
                HorizonBorder.level2(HorizonColors.LineAndBorder.lineStroke()),
                HorizonCornerRadius.level2
            )
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
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
            text = notificationItem.date.toLocalisedFormat(),
            style = HorizonTypography.p3,
            color = HorizonColors.Text.timestamp()
        )
        HorizonSpace(SpaceSize.SPACE_16)
    }
}

private fun Date?.toLocalisedFormat(): String {
    if (this == null) return ""
    val lastWeekDate = Date.from(Instant.now().apply { minus(Duration.ofDays(7)) })
    val isCurrentWeek = this.after(lastWeekDate)
    val dateFormat = if (isCurrentWeek) {
        "DDDD"
    } else {
        "MMM dd"
    }
    return this.localisedFormat(dateFormat)
}