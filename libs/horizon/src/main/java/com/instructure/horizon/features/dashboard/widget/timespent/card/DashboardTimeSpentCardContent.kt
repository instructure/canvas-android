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

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTimeSpentCardContent(
    state: DashboardTimeSpentCardState,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        stringResource(R.string.dashboardTimeSpentTitle),
        R.drawable.schedule,
        HorizonColors.PrimitivesHoney.honey12(),
        modifier,
        isLoading,
        false
    ) {
        if (state.hours == 0 && state.minutes == 0 && state.courses.isEmpty()) {
            Text(
                text = stringResource(R.string.dashboardTimeSpentEmptyMessage),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .shimmerEffect(isLoading)
            )
        } else {
            val courseValue = state.courses.firstOrNull { it.id == state.selectedCourseId }?.name
                ?: stringResource(R.string.dashboardTimeSpentTotal)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (state.minutes == 0) {
                    DashboardTimeSpentSingleTimeUnit(
                        state.hours,
                        pluralStringResource(R.plurals.dashboardTimeSpentHoursUnit, state.hours),
                        courseValue,
                        isLoading
                    )
                } else if (state.hours == 0) {
                    DashboardTimeSpentSingleTimeUnit(
                        state.minutes,
                        pluralStringResource(R.plurals.dashboardTimeSpentMinutesUnit, state.minutes),
                        courseValue,
                        isLoading
                    )
                } else {
                    DashboardTimeSpentMultiTimeUnit(
                        hours = state.hours,
                        minutes = state.minutes,
                        courseValue = courseValue,
                        isLoading = isLoading
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                ) {
                    if (state.courses.size > 1) {
                        HorizonSpace(SpaceSize.SPACE_8)

                        var isMenuOpen by remember { mutableStateOf(false) }
                        val courseSelectState = SingleSelectState(
                            isMenuOpen = isMenuOpen,
                            onMenuOpenChanged = { isMenuOpen = it },
                            size = SingleSelectInputSize.Medium,
                            isSingleLineOptions = true,
                            isFullWidth = true,
                            options = listOf(stringResource(R.string.dashboardTimeSpentTotal)) + state.courses.map { it.name },
                            selectedOption = courseValue,
                            onOptionSelected = { state.onCourseSelected(it) }
                        )

                        SingleSelect(
                            courseSelectState,
                            modifier = Modifier
                                .shimmerEffect(isLoading)
                                .focusable()
                                .widthIn(min = 100.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTimeSpentSingleTimeUnit(
    timeValue: Int,
    timeUnitValue: String,
    courseValue: String,
    isLoading: Boolean,
) {
    val widgetContentDescription = stringResource(
        R.string.a11y_dashboardTimeSpentContentDescription,
        timeValue,
        timeUnitValue,
        courseValue
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = widgetContentDescription
            }
    ) {
        Text(
            text = timeValue.toString(),
            style = HorizonTypography.h1.copy(fontSize = 38.sp, letterSpacing = 0.sp),
            color = HorizonColors.Text.body(),
            modifier = Modifier
                .shimmerEffect(isLoading)
                .semantics {
                    hideFromAccessibility()
                }
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = timeUnitValue,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.title(),
            modifier = Modifier
                .shimmerEffect(isLoading)
                .semantics {
                    hideFromAccessibility()
                }
        )
    }
}

@Composable
private fun DashboardTimeSpentMultiTimeUnit(
    hours: Int,
    minutes: Int,
    courseValue: String,
    isLoading: Boolean,
) {
    val widgetContentDescription = stringResource(
        R.string.a11y_dashboardTimeSpentCombinedContentDescription,
        hours,
        minutes,
        courseValue
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = widgetContentDescription
            }
    ) {
        DashboardTimeSpentSingleTimeUnit(
            timeValue = hours,
            timeUnitValue = pluralStringResource(R.plurals.dashboardTimeSpentHoursShortUnit, hours),
            courseValue = courseValue,
            isLoading = isLoading
        )
        HorizonSpace(SpaceSize.SPACE_8)
        DashboardTimeSpentSingleTimeUnit(
            timeValue = minutes,
            timeUnitValue = pluralStringResource(R.plurals.dashboardTimeSpentMinutesShortUnit, minutes),
            courseValue = courseValue,
            isLoading = isLoading
        )
    }
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 20,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science"),
                CourseOption(2, "Advanced Mathematics"),
                CourseOption(3, "Physics 101")
            ),
            selectedCourseId = null
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardSelectedContentPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 20,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science"),
                CourseOption(2, "Advanced Mathematics"),
                CourseOption(3, "Physics 101")
            ),
            selectedCourseId = 1
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentSingleCourseHoursPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 20,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science")
            ),
            selectedCourseId = null
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentSingleCourseMinutesPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            minutes = 20,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science")
            ),
            selectedCourseId = null
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardContentSingleCourseCombinedPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            hours = 10,
            minutes = 20,
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science")
            ),
            selectedCourseId = null
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardEmptyContentPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(
            courses = listOf(
                CourseOption(1, "Introduction to Computer Science")
            ),
            selectedCourseId = null
        ),
        isLoading = false
    )
}

@Composable
@Preview
private fun DashboardTimeSpentCardLoadingPreview() {
    DashboardTimeSpentCardContent(
        state = DashboardTimeSpentCardState(),
        isLoading = true
    )
}
