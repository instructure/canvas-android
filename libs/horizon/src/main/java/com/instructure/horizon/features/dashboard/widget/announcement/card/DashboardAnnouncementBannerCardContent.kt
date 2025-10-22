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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementBannerItem
import com.instructure.horizon.features.dashboard.widget.announcement.AnnouncementType
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.pandautils.utils.localisedFormat
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DashboardAnnouncementBannerCardContent(
    state: DashboardAnnouncementBannerCardState,
    isLoading: Boolean,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { state.announcements.size })

    DashboardCard(
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            HorizonSpace(SpaceSize.SPACE_24)
            HorizontalPager(
                state = pagerState,
                pageSpacing = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) { page ->
                AnnouncementPageContent(
                    announcement = state.announcements[page],
                    isLoading = isLoading,
                    mainNavController = mainNavController,
                    modifier = Modifier.semantics(mergeDescendants = true) {}
                )
            }

            if (state.announcements.size > 1) {
                HorizonSpace(SpaceSize.SPACE_16)

                PagerIndicator(isLoading, pagerState)
            }

            HorizonSpace(SpaceSize.SPACE_24)
        }
    }
}

@Composable
private fun PagerIndicator(
    isLoading: Boolean,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            iconRes = R.drawable.chevron_left,
            contentDescription = stringResource(R.string.dashboardAnnouncementBannerPreviousAnnouncement),
            size = IconButtonSize.NORMAL,
            color = IconButtonColor.Custom(
                backgroundColor = HorizonColors.Surface.cardPrimary(),
                iconColor = HorizonColors.Icon.default(),
                borderColor = HorizonColors.LineAndBorder.lineStroke()
            ),
            enabled = pagerState.currentPage > 0,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            },
            modifier = Modifier.shimmerEffect(isLoading)
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = stringResource(
                R.string.dashboardAnnouncementBannerPaginationLabel,
                pagerState.currentPage + 1,
                pagerState.pageCount
            ),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.title(),
            modifier = Modifier.shimmerEffect(isLoading)
        )

        Spacer(Modifier.weight(1f))

        IconButton(
            iconRes = R.drawable.chevron_right,
            contentDescription = stringResource(R.string.dashboardAnnouncementBannerNextAnnouncement),
            size = IconButtonSize.NORMAL,
            color = IconButtonColor.Custom(
                backgroundColor = HorizonColors.Surface.cardPrimary(),
                iconColor = HorizonColors.Icon.default(),
                borderColor = HorizonColors.LineAndBorder.lineStroke()
            ),
            enabled = pagerState.currentPage < pagerState.pageCount - 1,
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            modifier = Modifier.shimmerEffect(isLoading)
        )
    }
}

@Composable
private fun AnnouncementPageContent(
    announcement: AnnouncementBannerItem,
    isLoading: Boolean,
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
            ),
            modifier = Modifier.shimmerEffect(
                isLoading,
                backgroundColor = HorizonColors.PrimitivesSky.sky12.copy(0.8f),
                shimmerColor = HorizonColors.PrimitivesSky.sky12.copy(0.5f)
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
                color = HorizonColors.Text.dataPoint(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        announcement.date?.let { date ->
            Text(
                text = date.localisedFormat("MMM dd, yyyy"),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
        }

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = announcement.title,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier
                .fillMaxWidth()
                .shimmerEffect(isLoading)
        )

        HorizonSpace(SpaceSize.SPACE_16)

        Button(
            label = stringResource(R.string.dashboardAnnouncementBannerGoToAnnouncement),
            onClick = {
                mainNavController.navigate(announcement.route)
            },
            height = ButtonHeight.SMALL,
            width = ButtonWidth.FILL,
            color = ButtonColor.BlackOutline,
            modifier = Modifier.shimmerEffect(isLoading)
        )
    }
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
        isLoading = false,
        mainNavController = rememberNavController()
    )
}

@Composable
@Preview
private fun DashboardAnnouncementBannerCardLoadingPreview() {
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
        isLoading = true,
        mainNavController = rememberNavController()
    )
}
