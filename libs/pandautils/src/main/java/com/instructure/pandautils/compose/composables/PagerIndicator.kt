/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.compose.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontal pager indicator that shows the current page position with animated transitions.
 * Active page indicator expands to a pill shape while inactive indicators remain circular.
 *
 * @param pagerState The state of the pager to track
 * @param modifier Modifier to be applied to the indicator row
 * @param activeColor Color of the active page indicator
 * @param inactiveColor Color of inactive page indicators
 * @param indicatorHeight Height of the indicator dots/pills
 * @param activeIndicatorWidth Width of the active (expanded) indicator
 * @param inactiveIndicatorWidth Width of inactive (circular) indicators
 * @param indicatorSpacing Spacing between individual indicators
 * @param animationDurationMillis Duration of the expand/collapse animation in milliseconds
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Black,
    inactiveColor: Color = Color.Black.copy(alpha = 0.4f),
    indicatorHeight: Dp = 8.dp,
    activeIndicatorWidth: Dp = 32.dp,
    inactiveIndicatorWidth: Dp = 8.dp,
    indicatorSpacing: Dp = 8.dp,
    animationDurationMillis: Int = 300
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { index ->
            val isActive = pagerState.currentPage == index
            val width by animateDpAsState(
                targetValue = if (isActive) activeIndicatorWidth else inactiveIndicatorWidth,
                animationSpec = tween(durationMillis = animationDurationMillis),
                label = "indicatorWidth"
            )
            val color by animateFloatAsState(
                targetValue = if (isActive) activeColor.alpha else inactiveColor.alpha,
                animationSpec = tween(durationMillis = animationDurationMillis),
                label = "indicatorAlpha"
            )
            Box(
                modifier = Modifier
                    .height(indicatorHeight)
                    .width(width)
                    .clip(if (isActive) RoundedCornerShape(4.dp) else CircleShape)
                    .background(
                        if (isActive) activeColor.copy(alpha = color)
                        else inactiveColor.copy(alpha = color)
                    )
            )
            if (index < pagerState.pageCount - 1) {
                Spacer(modifier = Modifier.width(indicatorSpacing))
            }
        }
    }
}