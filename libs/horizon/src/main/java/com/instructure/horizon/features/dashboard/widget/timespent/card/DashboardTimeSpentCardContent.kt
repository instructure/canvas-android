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
package com.instructure.horizon.features.dashboard.widget.timespent.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTimeSpentCardContent(
    state: DashboardTimeSpentCardState,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        stringResource(R.string.dashboardTimeSpentTitle),
        R.drawable.schedule,
        HorizonColors.PrimitivesHoney.honey12(),
        modifier.padding(bottom = 8.dp)
    ) {

        FlowRow(
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.hours.roundToInt().toString(),
                style = HorizonTypography.h1.copy(fontSize = 38.sp, letterSpacing = 0.sp),
                color = HorizonColors.Text.body()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .width(IntrinsicSize.Max)
            ) {
                if (state.courses.size > 1) {
                    Text(
                        text = stringResource(R.string.dashboardTimeSpentHoursIn),
                        style = HorizonTypography.labelMediumBold,
                        color = HorizonColors.Text.title()
                    )

                    HorizonSpace(SpaceSize.SPACE_8)

                    var isMenuOpen by remember { mutableStateOf(false) }
                    val courseSelectState = SingleSelectState(
                        isMenuOpen = isMenuOpen,
                        onMenuOpenChanged = { isMenuOpen = it },
                        size = SingleSelectInputSize.Medium,
                        options = listOf(stringResource(R.string.dashboardTimeSpentAllCourses)) + state.courses.map { it.name },
                        selectedOption = state.courses.firstOrNull { it.id == state.selectedCourseId }?.name
                            ?: stringResource(R.string.dashboardTimeSpentAllCourses),
                        onOptionSelected = { state.onCourseSelected(it) }
                    )

                    SingleSelect(courseSelectState)
                } else {
                    Text(
                        text = stringResource(R.string.dashboardTimeSpentHoursInYourCourse),
                        style = HorizonTypography.labelMediumBold,
                        color = HorizonColors.Text.title(),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 24.5,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science"),
                CourseOption(2, "Advanced Mathematics"),
                CourseOption(3, "Physics 101")
            ),
            selectedCourseId = null
        )
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentSingleCoursePreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 12.0,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science")
            ),
            selectedCourseId = null
        )
    )
}
