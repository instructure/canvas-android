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
package com.instructure.horizon.features.learn.programs.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

data class ProgramProgressItemState(
    val courseCard: ProgramCourseCardState,
    val sequentialProperties: SequentialProgramProgressProperties? = null,
)

data class SequentialProgramProgressProperties(
    val status: ProgramProgressItemStatus,
    val index: Int,
    val first: Boolean = false,
    val last: Boolean = false,
    val previousCompleted: Boolean = false,
)

sealed class ProgramProgressItemStatus(val borderColor: Color) {
    data object Completed : ProgramProgressItemStatus(HorizonColors.PrimitivesHoney.honey35())
    data object Active : ProgramProgressItemStatus(HorizonColors.LineAndBorder.containerStroke())
    data object Upcoming : ProgramProgressItemStatus(HorizonColors.LineAndBorder.lineStroke())
}

@Composable
fun ProgramProgressItem(
    state: ProgramProgressItemState,
    modifier: Modifier = Modifier
) {
    var courseCardHeight by remember { mutableIntStateOf(0) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val sequentialProperties = state.sequentialProperties
        if (sequentialProperties != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(with(LocalDensity.current) { courseCardHeight.toDp() })
            ) {
                val prevDividerColor = when {
                    sequentialProperties.first -> Color.Transparent
                    sequentialProperties.previousCompleted -> HorizonColors.LineAndBorder.containerStroke()
                    else -> HorizonColors.LineAndBorder.lineStroke()
                }
                VerticalDivider(thickness = 1.dp, color = prevDividerColor, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level6)
                        .border(
                            border = HorizonBorder.level1(color = sequentialProperties.status.borderColor),
                            shape = HorizonCornerRadius.level6
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sequentialProperties.index.toString(),
                        style = HorizonTypography.labelSmallBold,
                        textAlign = TextAlign.Center
                    )
                }
                val nextDividerColor = when {
                    sequentialProperties.last -> Color.Transparent
                    sequentialProperties.status is ProgramProgressItemStatus.Completed -> HorizonColors.LineAndBorder.containerStroke()
                    else -> HorizonColors.LineAndBorder.lineStroke()
                }
                VerticalDivider(thickness = 1.dp, color = nextDividerColor, modifier = Modifier.weight(1f))
            }
            HorizonSpace(SpaceSize.SPACE_8)
        }
        ProgramCourseCard(
            state.courseCard, modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    courseCardHeight = it.size.height
                }
                .padding(vertical = 8.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressItemFirstAndCompletedPreview() {
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Completed
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Completed,
                index = 1,
                first = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressItemOnlyItemAndInactivePreview() {
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Inactive
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Upcoming,
                index = 1,
                first = true,
                last = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressLastAndInactivePreview() {
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Inactive
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Upcoming,
                index = 5,
                first = false,
                last = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressPreviousCompletedAndActivePreview() {
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Inactive
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Active,
                index = 5,
                first = false,
                last = false,
                previousCompleted = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressPreviousAndCurrentCompletedPreview() {
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Completed
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Completed,
                index = 5,
                first = false,
                last = false,
                previousCompleted = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressPreviousWithInProgressCourseCardPreview() {
    ContextKeeper.appContext = LocalContext.current
    val courseCardState = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.InProgress,
        courseProgress = 50.0,
        chips = listOf(
            CourseCardChipState("Required"),
            CourseCardChipState("5 hours 2 mins"),
            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
        )
    )
    ProgramProgressItem(
        state = ProgramProgressItemState(
            courseCardState,
            sequentialProperties = SequentialProgramProgressProperties(
                status = ProgramProgressItemStatus.Active,
                index = 5,
                first = false,
                last = false,
                previousCompleted = true
            )
        ),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}