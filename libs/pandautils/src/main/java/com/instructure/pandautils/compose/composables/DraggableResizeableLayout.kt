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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.utils.toDp
import kotlinx.coroutines.launch

enum class AnchorPoints {
    BOTTOM, MIDDLE, TOP
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DraggableResizableLayout(
    anchoredDraggableState: AnchoredDraggableState<AnchorPoints>,
    modifier: Modifier = Modifier,
    topContent: @Composable (currentAnchor: AnchorPoints) -> Unit,
    bottomContent: @Composable (currentAnchor: AnchorPoints) -> Unit,
    minBottomHeightDp: Dp = 56.dp,
    minTopHeightDp: Dp = 56.dp,
    initialAnchor: AnchorPoints = AnchorPoints.TOP,
) {

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val imeInsets = WindowInsets.ime
    val isKeyboardActuallyVisible by remember {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    var lastKeyboardInducedSnapTo by remember { mutableStateOf<AnchorPoints?>(null) }

    LaunchedEffect(isKeyboardActuallyVisible, anchoredDraggableState.anchors) {
        if (anchoredDraggableState.anchors.size == 0) {
            return@LaunchedEffect
        }

        val currentTarget = anchoredDraggableState.targetValue

        if (isKeyboardActuallyVisible) {
            val targetAnchor = AnchorPoints.TOP
            if (anchoredDraggableState.anchors.hasAnchorFor(targetAnchor)) {
                if (currentTarget != targetAnchor) {
                    coroutineScope.launch {
                        anchoredDraggableState.animateTo(targetAnchor)
                        lastKeyboardInducedSnapTo = targetAnchor
                    }
                }
            }
        } else {
            val targetAnchor = AnchorPoints.MIDDLE
            if (anchoredDraggableState.anchors.hasAnchorFor(targetAnchor)) {
                if (currentTarget != targetAnchor) {
                    coroutineScope.launch {
                        anchoredDraggableState.animateTo(targetAnchor)
                        lastKeyboardInducedSnapTo = targetAnchor
                    }
                }
            }
        }
    }

    val minBottomHeightPx =
        remember(minBottomHeightDp) { with(density) { minBottomHeightDp.toPx() } }
    val minTopHeightPx = remember(minBottomHeightDp) { with(density) { minTopHeightDp.toPx() } }

    var totalHeightPx by remember { mutableFloatStateOf(0f) }
    var layoutReady by remember { mutableStateOf(false) }

    val topAnchorTopLayoutHeightPx = remember(totalHeightPx, minBottomHeightPx) {
        (totalHeightPx - minBottomHeightPx).coerceAtLeast(minBottomHeightPx)
    }
    val middleAnchorTopLayoutHeightPx =
        remember(minTopHeightPx, topAnchorTopLayoutHeightPx) {
            if (topAnchorTopLayoutHeightPx > minTopHeightPx) {
                (minTopHeightPx + topAnchorTopLayoutHeightPx) / 2f
            } else {
                minTopHeightPx
            }
        }

    fun getPixelForAnchor(anchor: AnchorPoints): Float {
        return when (anchor) {
            AnchorPoints.BOTTOM -> minTopHeightPx
            AnchorPoints.MIDDLE -> middleAnchorTopLayoutHeightPx
            AnchorPoints.TOP -> topAnchorTopLayoutHeightPx
        }
    }

    LaunchedEffect(
        layoutReady,
        minTopHeightPx,
        middleAnchorTopLayoutHeightPx,
        topAnchorTopLayoutHeightPx,
        anchoredDraggableState
    ) {
        if (layoutReady) {
            val newAnchors = DraggableAnchors {
                AnchorPoints.BOTTOM at minTopHeightPx
                if (middleAnchorTopLayoutHeightPx > minTopHeightPx && middleAnchorTopLayoutHeightPx < topAnchorTopLayoutHeightPx) {
                    AnchorPoints.MIDDLE at middleAnchorTopLayoutHeightPx
                }
                if (topAnchorTopLayoutHeightPx > minTopHeightPx) {
                    AnchorPoints.TOP at topAnchorTopLayoutHeightPx
                } else {
                    AnchorPoints.TOP at minTopHeightPx
                }
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

    val currentTopSectionHeightPx by remember {
        derivedStateOf {
            val offset = anchoredDraggableState.offset
            val currentAnchors = anchoredDraggableState.anchors

            if (offset.isNaN()) {
                val anchor = anchoredDraggableState.targetValue
                if (currentAnchors.hasAnchorFor(anchor)) {
                    getPixelForAnchor(anchor)
                } else {
                    getPixelForAnchor(AnchorPoints.BOTTOM)
                }
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
            layoutReady = totalHeightPx > 0 && minBottomHeightPx < totalHeightPx
        }

        if (layoutReady && anchoredDraggableState.anchors.size > 0) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentTopSectionHeightPx.toInt().toDp.coerceAtLeast(0).dp)
                        .background(colorResource(R.color.backgroundLightest))
                ) {
                    topContent(anchoredDraggableState.currentValue)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .anchoredDraggable(
                            state = anchoredDraggableState,
                            orientation = Orientation.Vertical,
                        ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = colorResource(R.color.backgroundLightestElevated)),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (anchoredDraggableState.currentValue == AnchorPoints.BOTTOM) {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        anchoredDraggableState.animateTo(AnchorPoints.TOP)
                                    }
                                }) {
                                    Icon(
                                        tint = LocalCourseColor.current,
                                        painter = painterResource(R.drawable.ic_collapse_bottomsheet),
                                        contentDescription = stringResource(R.string.a11y_contentDescription_collapsePanel),
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        anchoredDraggableState.animateTo(AnchorPoints.BOTTOM)
                                    }
                                }) {
                                    Icon(
                                        tint = LocalCourseColor.current,
                                        painter = painterResource(R.drawable.ic_expand_bottomsheet),
                                        contentDescription = stringResource(R.string.a11y_contentDescription_expandPanel),
                                    )
                                }
                            }
                            BottomSheetDefaults.DragHandle()
                            IconButton(onClick = {
                                when (anchoredDraggableState.currentValue) {
                                    AnchorPoints.BOTTOM -> {
                                        coroutineScope.launch {
                                            anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                                        }
                                    }

                                    AnchorPoints.MIDDLE -> {
                                        coroutineScope.launch {
                                            anchoredDraggableState.animateTo(AnchorPoints.TOP)
                                        }
                                    }

                                    AnchorPoints.TOP -> {
                                        coroutineScope.launch {
                                            anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                                        }
                                    }
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_right),
                                    tint = LocalCourseColor.current,
                                    contentDescription = stringResource(R.string.a11y_contentDescription_expandPanel),
                                    modifier = Modifier.rotate(
                                        when (anchoredDraggableState.currentValue) {
                                            AnchorPoints.TOP -> 270f
                                            else -> 90f
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
}