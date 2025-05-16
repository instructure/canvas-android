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

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.toPx
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraggableResizableLayout(
    modifier: Modifier = Modifier,
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    minBottomHeight: Float = 56.toPx.toFloat(),
) {
    var topSectionHeightPx by remember { mutableStateOf(0f) }
    var totalHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Column(
        modifier = modifier.onGloballyPositioned {
            totalHeightPx = it.size.height.toFloat()
            if (topSectionHeightPx == 0f) {
                topSectionHeightPx = totalHeightPx - minBottomHeight
            }
        }
    ) {
        Box(
            modifier = Modifier
                .background(color = colorResource(R.color.backgroundLightest))
                .fillMaxWidth()
                .height(with(density) { topSectionHeightPx.toDp() })
        ) {
            topContent()
        }

        Box(
            modifier = Modifier
                .background(color = colorResource(R.color.backgroundLightest))
                .fillMaxWidth()
                .fillMaxSize()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                val newHeight = max(min(topSectionHeightPx + delta, totalHeightPx - minBottomHeight), minBottomHeight)
                                if (newHeight > 0 && newHeight < totalHeightPx) {
                                    topSectionHeightPx = newHeight
                                }
                            },
                        )
                ) {
                    DragHandle(modifier = Modifier.align(Alignment.Center))
                }
                bottomContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DraggableResizableLayoutPreview() {
    DraggableResizableLayout(
        modifier = Modifier
            .fillMaxSize(),
        topContent = {
            Text(text = "Top Content", modifier = Modifier.padding(16.dp))
        },
        bottomContent = {
            Text(text = "Bottom Content", modifier = Modifier.padding(16.dp))
        }
    )
}
