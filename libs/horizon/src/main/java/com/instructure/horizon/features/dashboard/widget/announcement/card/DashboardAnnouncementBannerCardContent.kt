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
package com.instructure.horizon.features.dashboard.widget.announcement.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementBannerItem
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardAnnouncementBannerCardContent(
    state: DashboardAnnouncementBannerCardState,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { state.announcements.size })
    val scope = rememberCoroutineScope()

    DashboardCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                AnnouncementPageContent(
                    announcement = state.announcements[page],
                    mainNavController = mainNavController
                )
            }

            HorizonSpace(SpaceSize.SPACE_16)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HorizonColors.Surface.cardPrimary())
                        .border(1.dp, HorizonColors.LineAndBorder.containerStroke(), CircleShape)
                        .alpha(if (pagerState.currentPage > 0) 1f else 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left),
                            contentDescription = stringResource(R.string.dashboardAnnouncementBannerPreviousAnnouncement),
                            tint = HorizonColors.Icon.default(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(
                        R.string.dashboardAnnouncementBannerPaginationLabel,
                        pagerState.currentPage + 1,
                        state.announcements.size
                    ),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.title()
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(HorizonColors.Surface.cardPrimary())
                        .border(1.dp, HorizonColors.LineAndBorder.containerStroke(), CircleShape)
                        .alpha(if (pagerState.currentPage < state.announcements.size - 1) 1f else 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        enabled = pagerState.currentPage < state.announcements.size - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right),
                            contentDescription = stringResource(R.string.dashboardAnnouncementBannerNextAnnouncement),
                            tint = HorizonColors.Icon.default(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementPageContent(
    announcement: AnnouncementBannerItem,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        StatusChip(
            state = StatusChipState(
                label = stringResource(R.string.notificationsAnnouncementCategoryLabel),
                color = StatusChipColor.Sky,
                fill = true
            )
        )

        HorizonSpace(SpaceSize.SPACE_16)
        announcement.source?.let {
            Text(
                text = stringResource(
                    R.string.dashboardAnnouncementBannerFrom,
                    it
                ),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp()
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        announcement.date?.let { date ->
            Text(
                text = formatDate(date),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
            )
        }

        Text(
            text = announcement.title,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        Button(
            label = stringResource(R.string.dashboardAnnouncementBannerGoToAnnouncement),
            onClick = {
                mainNavController.navigate(announcement.route)
            },
            height = ButtonHeight.SMALL,
            width = ButtonWidth.FILL,
            color = ButtonColor.BlackOutline
        )
    }
}

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

@Composable
@Preview
private fun DashboardAnnouncementBannerCardContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardAnnouncementBannerCardContent(
        state = DashboardAnnouncementBannerCardState(
            announcements = listOf(
                AnnouncementBannerItem(
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    type = AnnouncementType.COURSE,
                    route = ""
                ),
                AnnouncementBannerItem(
                    title = "Second announcement with different content to show pagination.",
                    source = "Another Course Name",
                    date = Date(),
                    type = AnnouncementType.COURSE,
                    route = ""
                ),
                AnnouncementBannerItem(
                    title = "Third global announcement without a source.",
                    date = Date(),
                    type = AnnouncementType.GLOBAL,
                    route = ""
                )
            )
        ),
        mainNavController = rememberNavController()
    )
}
