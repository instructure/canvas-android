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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R
import kotlin.math.max
import kotlin.math.min

@Composable
fun HorizontalDraggableResizeableLayout(
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    minLeftRatio: Float = 0.3f,
    minRightRatio: Float = 0.3f,
    fixedSplit: Boolean = false,
    expanded: Boolean = false
) {
    var totalWidthPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    var nonExpandedLeftPaneWidthPx by remember { mutableFloatStateOf(0f) }
    var currentTargetLeftPanePx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(totalWidthPx, minLeftRatio, minRightRatio, fixedSplit) {
        if (totalWidthPx > 0f) {
            val initialNonExpandedWidth = if (fixedSplit) {
                totalWidthPx * 0.5f
            } else {
                totalWidthPx * (1f - minRightRatio)
            }

            if (nonExpandedLeftPaneWidthPx == 0f || nonExpandedLeftPaneWidthPx > totalWidthPx) {
                nonExpandedLeftPaneWidthPx = initialNonExpandedWidth
            }

            currentTargetLeftPanePx = if (expanded) totalWidthPx else nonExpandedLeftPaneWidthPx
        }
    }

    LaunchedEffect(expanded, totalWidthPx, nonExpandedLeftPaneWidthPx) {
        if (totalWidthPx > 0f) {
            currentTargetLeftPanePx = if (expanded) {
                totalWidthPx
            } else {

                if (nonExpandedLeftPaneWidthPx <= 0f || nonExpandedLeftPaneWidthPx >= totalWidthPx) {
                    if (fixedSplit) totalWidthPx * 0.5f else totalWidthPx * (1f - minRightRatio)
                } else {
                    nonExpandedLeftPaneWidthPx
                }
            }
        }
    }

    val animatedLeftWidthDp by animateDpAsState(
        targetValue = with(density) { currentTargetLeftPanePx.toDp() },
        label = "leftPaneWidthAnimation"
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                totalWidthPx = it.size.width.toFloat()
            }
            .background(colorResource(R.color.backgroundLightest))
    ) {
        Box(
            modifier = Modifier
                .background(color = colorResource(R.color.backgroundLightest))
                .width(animatedLeftWidthDp)
                .fillMaxHeight()
        ) {
            if (animatedLeftWidthDp > 0.dp || (expanded && totalWidthPx > 0f) ) {
                leftContent()
            }
        }

        if (!expanded && totalWidthPx > 0f && animatedLeftWidthDp < with(density) { totalWidthPx.toDp() }) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(color = colorResource(id = R.color.backgroundLightest))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (!fixedSplit) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState { delta ->
                                        if (totalWidthPx > 0f) {
                                            val newProposedPx = nonExpandedLeftPaneWidthPx + delta
                                            val minBoundPx = totalWidthPx * minLeftRatio
                                            val maxBoundPx = totalWidthPx * (1f - minRightRatio)

                                            val newWidthPx = min(max(newProposedPx, minBoundPx), maxBoundPx)

                                            if (newWidthPx > 0 && newWidthPx < totalWidthPx) {
                                                nonExpandedLeftPaneWidthPx = newWidthPx
                                                currentTargetLeftPanePx = newWidthPx
                                            }
                                        }
                                    }
                                )
                        ) {
                            VerticalDragHandle(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        rightContent()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun HorizontalDraggableResizableLayoutPreview() {
    val expanded by remember { mutableStateOf(false) }
    HorizontalDraggableResizeableLayout(
        modifier = Modifier.fillMaxSize(),
        leftContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Red)) {
                Text(text = "Left Content", modifier = Modifier.padding(16.dp).align(Alignment.Center))
            }
        },
        rightContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Blue)) {
                Text(text = "Right Content", modifier = Modifier.padding(16.dp).align(Alignment.Center))
            }
        },
        expanded = expanded
    )
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun HorizontalDraggableResizableLayoutPreviewInitiallyExpanded() {
    HorizontalDraggableResizeableLayout(
        modifier = Modifier.fillMaxSize(),
        leftContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Red)) {
                Text(text = "Left Content", modifier = Modifier.padding(16.dp).align(Alignment.Center))
            }
        },
        rightContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Blue)) {
                Text(text = "Right Content", modifier = Modifier.padding(16.dp).align(Alignment.Center))
            }
        },
        expanded = true
    )
}

@Preview(showBackground = true, device = "spec:width=400dp,height=800dp,dpi=240")
@Composable
private fun HorizontalDraggableResizableLayoutPreviewPhone() {
    val expanded by remember { mutableStateOf(false) }
    HorizontalDraggableResizeableLayout(
        modifier = Modifier.fillMaxSize(),
        leftContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Red)) {
                Text(text = "Left", modifier = Modifier.padding(8.dp).align(Alignment.Center))
            }
        },
        rightContent = {
            Box(modifier = Modifier.fillMaxSize().background(color = Color.Blue)) {
                Text(text = "Right", modifier = Modifier.padding(8.dp).align(Alignment.Center))
            }
        },
        expanded = expanded,
        minLeftRatio = 0.2f,
        minRightRatio = 0.2f
    )
}