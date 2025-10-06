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
package com.instructure.horizon.features.dashboard.timespent.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.compose.composables.Dropdown
import kotlin.math.roundToInt

@Composable
fun DashboardTimeSpentCardContent(
    state: DashboardTimeSpentCardState,
    modifier: Modifier = Modifier,
) {
    DashboardTimeSpentCard(modifier.padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboardTimeSpentTitle),
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.dataPoint(),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(R.drawable.schedule),
                    contentDescription = null,
                    tint = HorizonColors.Icon.medium(),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizonSpace(SpaceSize.SPACE_8)

            val hoursText = if (state.hours == 0.0) {
                "0"
            } else {
                state.hours.roundToInt().toString()
            }

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = hoursText,
                    style = HorizonTypography.h1.copy(fontSize = 38.sp, lineHeight = 0.sp),
                    color = HorizonColors.Text.body()
                )
            }

            HorizonSpace(SpaceSize.SPACE_4)

            if (state.courses.size > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboardTimeSpentHoursIn),
                        style = HorizonTypography.labelMediumBold,
                        color = HorizonColors.Text.title()
                    )

                    HorizonSpace(SpaceSize.SPACE_8)

                    val options = buildList {
                        add(stringResource(R.string.dashboardTimeSpentAllCourses))
                        addAll(state.courses.map { it.name })
                    }

                    val selectedIndex = if (state.selectedCourseId == null) {
                        0
                    } else {
                        state.courses.indexOfFirst { it.id == state.selectedCourseId } + 1
                    }

                    Dropdown(
                        options = options,
                        selectedIndex = selectedIndex,
                        onOptionSelected = { index ->
                            if (index == 0) {
                                state.onCourseSelected(null)
                            } else {
                                state.onCourseSelected(state.courses[index - 1].id)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.dashboardTimeSpentHoursInYourCourse),
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.title()
                )
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
