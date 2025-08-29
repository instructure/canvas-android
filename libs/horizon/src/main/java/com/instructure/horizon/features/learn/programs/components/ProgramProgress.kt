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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

data class ProgramProgressState(
    val courses: List<ProgramProgressItemState>,
    val headerString: String? = null,
)

@Composable
fun ProgramProgress(
    state: ProgramProgressState,
    modifier: Modifier = Modifier
) {
    Column {
        if (state.headerString != null) {
            Text(text = state.headerString, style = HorizonTypography.sh4)
            HorizonSpace(SpaceSize.SPACE_8)
        }
        state.courses.forEach { course ->
            ProgramProgressItem(
                state = course,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressSequentialPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgramProgress(
        state = ProgramProgressState(
            courses = listOf(
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.Completed,
                        courseProgress = 0.0,
                        chips = listOf(
                            CourseCardChipState("Body Text"),
                        )
                    ),
                    sequentialProperties = SequentialProgramProgressProperties(
                        status = ProgramProgressItemStatus.Completed,
                        index = 1,
                        first = true,
                        last = false
                    )
                ),
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.InProgress,
                        courseProgress = 50.0,
                        chips = listOf(
                            CourseCardChipState("Required"),
                            CourseCardChipState("5 hours 2 mins"),
                            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
                        )
                    ),
                    sequentialProperties = SequentialProgramProgressProperties(
                        status = ProgramProgressItemStatus.Active,
                        index = 2,
                        first = false,
                        last = false,
                        previousCompleted = true
                    )
                ),
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.Inactive,
                        courseProgress = 0.0,
                        chips = listOf(
                            CourseCardChipState("Locked", iconRes = R.drawable.lock),
                            CourseCardChipState("Required"),
                            CourseCardChipState("5 hours 2 mins"),
                        )
                    ),
                    sequentialProperties = SequentialProgramProgressProperties(
                        status = ProgramProgressItemStatus.Upcoming,
                        index = 3,
                        first = false,
                        last = true
                    )
                )
            )
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F4)
@Composable
private fun ProgramProgressNonSequentialPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgramProgress(
        state = ProgramProgressState(
            courses = listOf(
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.Completed,
                        courseProgress = 0.0,
                        chips = listOf(
                            CourseCardChipState("Body Text"),
                        )
                    )
                ),
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.InProgress,
                        courseProgress = 50.0,
                        chips = listOf(
                            CourseCardChipState("Required"),
                            CourseCardChipState("5 hours 2 mins"),
                            CourseCardChipState("XX/XX/XX – XX/XX/XX", iconRes = R.drawable.calendar_today)
                        )
                    )
                ),
                ProgramProgressItemState(
                    courseCard = ProgramCourseCardState(
                        courseName = "Sample Course",
                        status = CourseCardStatus.Inactive,
                        courseProgress = 0.0,
                        chips = listOf(
                            CourseCardChipState("Locked", iconRes = R.drawable.lock),
                            CourseCardChipState("Required"),
                            CourseCardChipState("5 hours 2 mins"),
                        )
                    )
                )
            ),
            headerString = "Completed 2 of 3 courses"
        )
    )
}