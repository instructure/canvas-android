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
package com.instructure.horizon.features.dashboard.widget

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
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
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
fun DashboardPaginatedWidgetCard(
    state: DashboardPaginatedWidgetCardState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { state.items.size })

    if (state.items.isNotEmpty()) {
        DashboardCard(
            modifier = modifier,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                HorizonSpace(SpaceSize.SPACE_24)
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 24.dp,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) { page ->
                    DashboardPaginatedWidgetCardItem(
                        item = state.items[page],
                        isLoading = state.isLoading,
                        mainNavController = mainNavController,
                        homeNavController = homeNavController,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .semantics(mergeDescendants = true) {}
                    )
                }

                if (state.items.size > 1) {
                    HorizonSpace(SpaceSize.SPACE_16)

                    PagerIndicator(
                        state.isLoading,
                        pagerState,
                        Modifier.padding(horizontal = 24.dp)
                    )
                }

                HorizonSpace(SpaceSize.SPACE_24)
            }
        }
    }
}

@Composable
private fun DashboardPaginatedWidgetCardItem(
    item: DashboardPaginatedWidgetCardItemState,
    isLoading: Boolean,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        item.chipState?.let { chipState ->
            StatusChip(
                state = StatusChipState(
                    label = chipState.label,
                    color = chipState.color,
                    fill = true
                ),
                modifier = Modifier.shimmerEffect(
                    isLoading,
                    backgroundColor = chipState.color.fillColor.copy(0.8f),
                    shimmerColor = chipState.color.fillColor.copy(0.5f)
                )
            )
            HorizonSpace(SpaceSize.SPACE_16)
        }
        item.source?.let { source ->
            Text(
                text = stringResource(
                    R.string.dashboardAnnouncementBannerFrom,
                    source
                ),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.dataPoint(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
            HorizonSpace(SpaceSize.SPACE_4)
        }
        item.date?.let { date ->
            Text(
                text = date.localisedFormat("MMM dd, yyyy"),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier.shimmerEffect(isLoading)
            )
            HorizonSpace(SpaceSize.SPACE_8)
        }

        item.title?.let { title ->
            Text(
                text = title,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shimmerEffect(isLoading)
            )

            HorizonSpace(SpaceSize.SPACE_16)
        }

        item.buttonState?.let { buttonState ->
            Button(
                label = buttonState.label,
                onClick = {
                    when (buttonState.route) {
                        is DashboardPaginatedWidgetCardButtonRoute.HomeRoute -> {
                            homeNavController.navigate(buttonState.route.route)
                        }
                        is DashboardPaginatedWidgetCardButtonRoute.MainRoute -> {
                            mainNavController.navigate(buttonState.route.route)
                        }
                    }
                },
                height = buttonState.height,
                width = buttonState.width,
                color = buttonState.color,
                modifier = Modifier
                    .shimmerEffect(
                        isLoading,
                        shape = HorizonCornerRadius.level6
                    )
            )
        }
    }
}

@Composable
private fun PagerIndicator(
    isLoading: Boolean,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
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
@Preview
private fun DashboardPaginatedWidgetCardAnnouncementContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardPaginatedWidgetCard(
        state = DashboardPaginatedWidgetCardState(
            items = listOf(
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Second announcement with different content to show pagination.",
                    source = "Another Course Name",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Third global announcement without a source.",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                )
            )
        ),
        rememberNavController(),
        rememberNavController(),
    )
}

@Composable
@Preview
private fun DashboardPaginatedWidgetCardAnnouncementSingleContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardPaginatedWidgetCard(
        state = DashboardPaginatedWidgetCardState(
            items = listOf(
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                ),
            )
        ),
        rememberNavController(),
        rememberNavController(),
    )
}

@Composable
@Preview
private fun DashboardPaginatedWidgetCardAnnouncementLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardPaginatedWidgetCard(
        state = DashboardPaginatedWidgetCardState(
            items = listOf(
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Announcement title shown here.",
                    source = "Institution or Course Name Here",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Second announcement with different content to show pagination.",
                    source = "Another Course Name",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                ),
                DashboardPaginatedWidgetCardItemState(
                    chipState = DashboardPaginatedWidgetCardChipState(
                        label = "Announcement",
                        color = StatusChipColor.Sky
                    ),
                    title = "Third global announcement without a source.",
                    date = Date(),
                    buttonState = DashboardPaginatedWidgetCardButtonState(
                        label = "Go to announcement",
                        height = ButtonHeight.SMALL,
                        width = ButtonWidth.FILL,
                        color = ButtonColor.WhiteWithOutline,
                        route = DashboardPaginatedWidgetCardButtonRoute.MainRoute("")
                    )
                )
            ),
            isLoading = true
        ),
        rememberNavController(),
        rememberNavController(),
    )
}