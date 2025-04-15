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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun HorizontalDraggableResizableLayout(
    modifier: Modifier = Modifier,
    leftContent: @Composable () -> Unit,
    rightContent: @Composable () -> Unit,
    minLeftRatio: Float = 0.3f,
    minRightRatio: Float = 0.3f
) {
    var leftSectionWidthPx by remember { mutableStateOf(0f) }
    var totalWidthPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Row(
        modifier = modifier.onGloballyPositioned {
            totalWidthPx = it.size.width.toFloat()
            if (leftSectionWidthPx == 0f) {
                leftSectionWidthPx = totalWidthPx - minRightRatio * totalWidthPx
            }
        }
    ) {
        Box(
            modifier = Modifier
                .width(with(density) { leftSectionWidthPx.toDp() })
                .fillMaxHeight()
        ) {
            leftContent()
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val newWidth = min(
                                    max(
                                        leftSectionWidthPx + delta,
                                        totalWidthPx * minLeftRatio
                                    ), totalWidthPx - totalWidthPx * minRightRatio
                                )
                                if (newWidth > 0 && newWidth < totalWidthPx) {
                                    leftSectionWidthPx = newWidth
                                }
                            },
                        )
                ) {
                    VerticalDragHandle(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
                rightContent()
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun HorizontalDraggableResizableLayoutPreview() {
    HorizontalDraggableResizableLayout(
        modifier = Modifier
            .fillMaxSize(),
        leftContent = {
            Text(text = "Left Content", modifier = Modifier.padding(16.dp))
        },
        rightContent = {
            Text(text = "Right Content", modifier = Modifier.padding(16.dp))
        }
    )
}