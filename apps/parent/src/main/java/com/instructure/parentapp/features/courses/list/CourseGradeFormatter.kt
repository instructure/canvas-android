/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.courses.list

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.convertPercentToPointBased
import com.instructure.pandautils.utils.orDefault
import com.instructure.parentapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat


class CourseGradeFormatter(@ApplicationContext private val context: Context) {

    private val percentageFormat = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 2
    }

    fun getGradeText(course: Course, selectedStudentId: Long): String? {
        val enrollment = course.enrollments?.find { it.userId == selectedStudentId } ?: return null
        val grade = course.parentGetCourseGradeFromEnrollment(
            enrollment,
            course.enrollments.orEmpty().any {
                !it.hasActiveGradingPeriod()
            }
        )

        val restrictQuantitativeData = course.settings?.restrictQuantitativeData.orDefault()
        if (grade.isLocked || (restrictQuantitativeData && !grade.hasCurrentGradeString())) return null

        val formattedScore = grade.currentScore?.takeIf {
            !restrictQuantitativeData
        }?.let {
            if (course.pointsBasedGradingScheme) {
                convertPercentToPointBased(it, course.scalingFactor)
            } else {
                percentageFormat.format(it / 100)
            }
        }.orEmpty()

        return when {
            grade.noCurrentGrade -> context.getString(R.string.noGrade)
            !grade.currentGrade.isNullOrEmpty() -> "${grade.currentGrade} $formattedScore"
            else -> formattedScore
        }
    }
}
