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
package com.instructure.horizon.horizonui.organisms

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import kotlin.math.abs

@Composable
fun AnimatedHorizontalPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    sizeAnimationRange: Float = 0.2f,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondViewportPageCount: Int = PagerDefaults.BeyondViewportPageCount,
    pageSpacing: Dp = 0.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(pagerState),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((Int) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(pagerState, Orientation.Horizontal),
    snapPosition: SnapPosition = SnapPosition.Start,
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    pageContent: @Composable (PagerScope.(Int, Modifier) -> Unit),
) {
    HorizontalPager(
        pagerState,
        contentPadding = contentPadding,
        pageSize = pageSize,
        beyondViewportPageCount = beyondViewportPageCount,
        pageSpacing = pageSpacing,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        key = key,
        pageNestedScrollConnection = pageNestedScrollConnection,
        snapPosition = snapPosition,
        overscrollEffect = overscrollEffect,
        modifier = modifier.semantics {
            role = Role.Carousel
        }.animateContentSize()
    ) {
        var cardWidthList by remember { mutableStateOf(emptyMap<Int, Float>()) }
        val scaleAnimation by animateFloatAsState(
            if (it == pagerState.currentPage) {
                (1 - abs(pagerState.currentPageOffsetFraction.convertScaleRange(sizeAnimationRange)))
            } else {
                (1f - (sizeAnimationRange * 2)) + (abs(pagerState.currentPageOffsetFraction.convertScaleRange(sizeAnimationRange)))
            },
            label = "DashboardCourseCardAnimation",
        )
        val animationDirection = when {
            it < pagerState.currentPage -> 1
            it > pagerState.currentPage -> -1
            else -> if (pagerState.currentPageOffsetFraction > 0) 1 else -1
        }
        pageContent(
            it,
            Modifier
                .onGloballyPositioned { coordinates ->
                    cardWidthList = cardWidthList + (it to coordinates.size.width.toFloat())
                }
                .offset {
                    IntOffset(
                        (animationDirection * ((cardWidthList[it]
                            ?: 0f) / 2 * (1 - scaleAnimation))).toInt(),
                        0
                    )
                }
                .scale(scaleAnimation)
        )
    }
}

@Composable
fun AnimatedHorizontalPagerIndicator(
    pagerState: PagerState
) {
    val selectedIndex = pagerState.currentPage
    val offset = pagerState.currentPageOffsetFraction

    var scrollToIndex: Int? by remember { mutableStateOf(null) }
    LaunchedEffect(scrollToIndex) {
        if (scrollToIndex != null) {
            pagerState.animateScrollToPage(scrollToIndex ?: return@LaunchedEffect)
            scrollToIndex = null
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                hideFromAccessibility()
            }
    ) {
        items(pagerState.pageCount) { itemIndex ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .padding(5.dp)
                    .border(1.dp, HorizonColors.Icon.medium(), CircleShape)
                    .clip(CircleShape)
                    .clickable { scrollToIndex = itemIndex }
                    .clearAndSetSemantics {
                        hideFromAccessibility()
                    }
            ) {
                if (itemIndex == selectedIndex) {
                    Box(
                        modifier = Modifier
                            .size(10.dp * (1 - abs(offset)))
                            .clip(CircleShape)
                            .background(HorizonColors.Icon.medium())
                    )
                } else if (itemIndex == selectedIndex + (1 * if (offset > 0) 1 else -1)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp * (abs(offset)))
                            .clip(CircleShape)
                            .background(HorizonColors.Icon.medium())
                    )
                }
            }
        }
    }
}

private fun Float.convertScaleRange(newScale: Float): Float {
    val oldMin = -0.5f
    val oldMax = 0.5f
    val newMin = -newScale
    val newMax = newScale
    return ((this - oldMin) / (oldMax - oldMin) ) * (newMax - newMin) + newMin
}