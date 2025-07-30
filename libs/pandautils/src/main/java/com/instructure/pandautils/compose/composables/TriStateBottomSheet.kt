/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AnchorPoints {
    BOTTOM, MIDDLE, TOP
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TriStateBottomSheet(
    anchoredDraggableState: AnchoredDraggableState<AnchorPoints>,
    modifier: Modifier = Modifier,
    topContent: @Composable (currentAnchor: AnchorPoints) -> Unit,
    bottomContent: @Composable (currentAnchor: AnchorPoints) -> Unit,
    minBottomHeightWhileOverlappingDp: Dp = 0.dp,
    peekHeightDp: Dp = 56.dp,
    initialAnchor: AnchorPoints = AnchorPoints.BOTTOM,
) {

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val minBottomVisibleHeightPx =
        remember(minBottomHeightWhileOverlappingDp) { with(density) { minBottomHeightWhileOverlappingDp.toPx() } }
    val peekHeightPx = remember(peekHeightDp) { with(density) { peekHeightDp.toPx() } }

    var totalHeightPx by remember { mutableFloatStateOf(0f) }
    var layoutReady by remember { mutableStateOf(false) }

    val topAnchorY = remember(totalHeightPx, minBottomVisibleHeightPx) {
        minBottomVisibleHeightPx
    }
    val bottomAnchorY = remember(totalHeightPx, peekHeightPx) {
        (totalHeightPx - peekHeightPx).coerceAtLeast(topAnchorY)
    }
    val middleAnchorY = remember(topAnchorY, bottomAnchorY) {
        if (bottomAnchorY > topAnchorY) {
            (topAnchorY + bottomAnchorY) / 2f
        } else {
            bottomAnchorY
        }
    }

    fun getPixelForAnchor(anchor: AnchorPoints): Float {
        return when (anchor) {
            AnchorPoints.BOTTOM -> bottomAnchorY
            AnchorPoints.MIDDLE -> middleAnchorY
            AnchorPoints.TOP -> topAnchorY
        }
    }

    LaunchedEffect(
        layoutReady,
        topAnchorY,
        middleAnchorY,
        bottomAnchorY,
        anchoredDraggableState
    ) {
        if (layoutReady) {
            val newAnchors = DraggableAnchors {
                AnchorPoints.TOP at topAnchorY
                if (middleAnchorY > topAnchorY && middleAnchorY < bottomAnchorY) {
                    AnchorPoints.MIDDLE at middleAnchorY
                }
                AnchorPoints.BOTTOM at bottomAnchorY
            }

            if (newAnchors.size > 0 && newAnchors != anchoredDraggableState.anchors) {
                anchoredDraggableState.updateAnchors(newAnchors)

                val currentTarget = anchoredDraggableState.targetValue
                if (!newAnchors.hasAnchorFor(currentTarget)) {
                    if (newAnchors.hasAnchorFor(initialAnchor)) {
                        anchoredDraggableState.snapTo(initialAnchor)
                    } else if (newAnchors.size > 0) {
                        newAnchors.closestAnchor(anchoredDraggableState.offset.takeIf { !it.isNaN() }
                            ?: getPixelForAnchor(AnchorPoints.BOTTOM))?.let {
                            anchoredDraggableState.snapTo(it)
                        }
                    }
                }
            } else if (newAnchors.size == 0 && anchoredDraggableState.anchors.size > 0) {
                anchoredDraggableState.updateAnchors(DraggableAnchors { })
            }
        }
    }

    LaunchedEffect(anchoredDraggableState.anchors, initialAnchor) {
        val currentAnchors = anchoredDraggableState.anchors
        if (currentAnchors.hasAnchorFor(initialAnchor) && anchoredDraggableState.currentValue != initialAnchor) {
            if (anchoredDraggableState.targetValue != initialAnchor || !anchoredDraggableState.isAnimationRunning) {
                anchoredDraggableState.snapTo(initialAnchor)
            }
        }
    }

    val currentSheetOffsetY by remember {
        derivedStateOf {
            val offset = anchoredDraggableState.offset
            val currentAnchors = anchoredDraggableState.anchors

            if (offset.isNaN() || currentAnchors.size == 0) {
                getPixelForAnchor(initialAnchor)
            } else {
                val minAnchorValue = currentAnchors.minAnchor()
                val maxAnchorValue = currentAnchors.maxAnchor()
                offset.coerceIn(minAnchorValue, maxAnchorValue)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.backgroundLightest))
    ) {
        val newTotalHeight = with(density) { maxHeight.toPx() }
        if (newTotalHeight != totalHeightPx || !layoutReady) {
            totalHeightPx = newTotalHeight
            layoutReady =
                totalHeightPx > 0 && peekHeightPx < totalHeightPx && minBottomVisibleHeightPx < totalHeightPx
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = peekHeightDp)
                .background(colorResource(R.color.backgroundLightest))
        ) {
            topContent(anchoredDraggableState.currentValue)
        }

        if (layoutReady && anchoredDraggableState.anchors.size > 0) {
            val sheetHeightPx = (totalHeightPx - currentSheetOffsetY).coerceAtLeast(0f)
            val sheetHeightDp = with(density) { sheetHeightPx.toDp() }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeightDp)
                    .offset { IntOffset(0, currentSheetOffsetY.roundToInt()) }
                    .anchoredDraggable(
                        state = anchoredDraggableState,
                        orientation = Orientation.Vertical,
                    )
                    .zIndex(1f),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = colorResource(R.color.backgroundLightestElevated)),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (anchoredDraggableState.targetValue == AnchorPoints.TOP) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    anchoredDraggableState.animateTo(AnchorPoints.BOTTOM)
                                }
                            },
                                modifier = Modifier.testTag("collapsePanelButton")
                            ) {
                                Icon(
                                    tint = LocalCourseColor.current,
                                    painter = painterResource(R.drawable.ic_collapse_bottomsheet),
                                    contentDescription = stringResource(R.string.a11y_contentDescription_collapsePanel),
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    anchoredDraggableState.animateTo(AnchorPoints.TOP)
                                }
                            },
                                modifier = Modifier.testTag("expandPanelButton")
                            ) {
                                Icon(
                                    tint = LocalCourseColor.current,
                                    painter = painterResource(R.drawable.ic_expand_bottomsheet),
                                    contentDescription = stringResource(R.string.a11y_contentDescription_expandPanel),
                                )
                            }
                        }
                        BottomSheetDefaults.DragHandle()
                        IconButton(onClick = {
                            coroutineScope.launch {
                                when (anchoredDraggableState.targetValue) {
                                    AnchorPoints.BOTTOM -> {
                                        if (anchoredDraggableState.anchors.hasAnchorFor(AnchorPoints.MIDDLE)) {
                                            anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                                        } else {
                                            anchoredDraggableState.animateTo(AnchorPoints.BOTTOM)
                                        }
                                    }

                                    AnchorPoints.MIDDLE -> {
                                        anchoredDraggableState.animateTo(AnchorPoints.BOTTOM)
                                    }

                                    AnchorPoints.TOP -> {
                                        if (anchoredDraggableState.anchors.hasAnchorFor(AnchorPoints.MIDDLE)) {
                                            anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                                        } else {
                                            anchoredDraggableState.animateTo(AnchorPoints.BOTTOM)
                                        }
                                    }
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_down),
                                tint = LocalCourseColor.current,
                                contentDescription = stringResource(R.string.a11y_contentDescription_expandPanel),
                                modifier = Modifier.rotate(
                                    when (anchoredDraggableState.targetValue) {
                                        AnchorPoints.BOTTOM -> 180f
                                        else -> 0f
                                    }
                                )
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colorResource(R.color.backgroundLightestElevated))
                    ) {
                        bottomContent(anchoredDraggableState.currentValue)
                    }
                }
            }
        }
    }
}
