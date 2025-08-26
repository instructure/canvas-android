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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.programs.ProgressBarUiState
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState

data class ProgramCourseCardState(
    val courseName: String,
    val status: CourseCardStatus,
    val courseProgress: Double? = null,
    val chips: List<CourseCardChipState> = emptyList(),
)

sealed class CourseCardStatus(
    val chipColor: StatusChipColor,
    val chipFill: Boolean = true,
    val courseNameColor: Color = HorizonColors.Text.title(),
    val borderColor: Color = HorizonColors.LineAndBorder.containerStroke(),
    val courseIconRes: Int? = null,
) {
    data object Completed : CourseCardStatus(
        StatusChipColor.Honey,
        courseIconRes = R.drawable.check_circle_full,
        borderColor = HorizonColors.PrimitivesHoney.honey35()
    )

    data object Enrolled : CourseCardStatus(StatusChipColor.Grey, courseNameColor = HorizonColors.Surface.institution())
    data object InProgress : CourseCardStatus(StatusChipColor.Grey, courseNameColor = HorizonColors.Surface.institution())
    data object Inactive :
        CourseCardStatus(StatusChipColor.WhiteWithBorder, borderColor = HorizonColors.LineAndBorder.lineStroke(), chipFill = false)

    data object Active : CourseCardStatus(StatusChipColor.Grey, courseNameColor = HorizonColors.Surface.institution())
}

data class CourseCardChipState(
    val label: String,
    val iconRes: Int? = null,
    val overrideColor: StatusChipColor? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgramCourseCard(state: ProgramCourseCardState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level2)
            .border(HorizonBorder.level1(state.status.borderColor), HorizonCornerRadius.level2)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.status.courseIconRes != null) {
                Icon(
                    painterResource(state.status.courseIconRes),
                    contentDescription = null,
                    tint = state.status.borderColor,
                    modifier = Modifier.size(20.dp)
                )
                HorizonSpace(SpaceSize.SPACE_4)
            }
            Text(state.courseName, style = HorizonTypography.h4, color = state.status.courseNameColor)
        }
        if (state.status is CourseCardStatus.InProgress && state.courseProgress != null) {
            HorizonSpace(SpaceSize.SPACE_12)
            ProgramsProgressBar(ProgressBarUiState(state.courseProgress))
            HorizonSpace(SpaceSize.SPACE_8)
        }
        if (state.chips.isNotEmpty()) {
            HorizonSpace(SpaceSize.SPACE_12)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                state.chips.forEach {
                    val statusChipState = StatusChipState(
                        label = it.label,
                        color = it.overrideColor ?: state.status.chipColor,
                        fill = state.status.chipFill,
                        iconRes = it.iconRes
                    )
                    StatusChip(statusChipState)
                }
            }
        }
        if (state.status is CourseCardStatus.Active) {
            HorizonSpace(SpaceSize.SPACE_24)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    label = stringResource(R.string.programsCourseCard_enrollButton),
                    color = ButtonColor.Institution,
                    height = ButtonHeight.SMALL
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProgramCourseCardCompletedPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Completed,
        courseProgress = 0.0,
        chips = listOf(
            CourseCardChipState("Body Text"),
        )
    )
    ProgramCourseCard(state, Modifier.fillMaxWidth())
}

@Composable
@Preview(showBackground = true)
fun ProgramCourseCardEnrolledPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Enrolled,
        courseProgress = 0.0,
        chips = listOf(
            CourseCardChipState("Enrolled", iconRes = R.drawable.check_circle_full, overrideColor = StatusChipColor.Green),
            CourseCardChipState("Required"),
            CourseCardChipState("5 hours 2 mins"),
            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
        )
    )
    ProgramCourseCard(state, Modifier.fillMaxWidth())
}

@Composable
@Preview(showBackground = true)
fun ProgramCourseCardInProgressPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.InProgress,
        courseProgress = 50.0,
        chips = listOf(
            CourseCardChipState("Required"),
            CourseCardChipState("5 hours 2 mins"),
            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
        )
    )
    ProgramCourseCard(state, Modifier.fillMaxWidth())
}

@Composable
@Preview(showBackground = true)
fun ProgramCourseCardActivePreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Active,
        courseProgress = 0.0,
        chips = listOf(
            CourseCardChipState("Required"),
            CourseCardChipState("5 hours 2 mins"),
            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
        )
    )
    ProgramCourseCard(state, Modifier.fillMaxWidth())
}

@Composable
@Preview(showBackground = true)
fun ProgramCourseCardInactivePreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = ProgramCourseCardState(
        courseName = "Sample Course",
        status = CourseCardStatus.Inactive,
        courseProgress = 0.0,
        chips = listOf(
            CourseCardChipState("Locked", iconRes = R.drawable.lock),
            CourseCardChipState("Required"),
            CourseCardChipState("5 hours 2 mins"),
        )
    )
    ProgramCourseCard(state, Modifier.fillMaxWidth())
}