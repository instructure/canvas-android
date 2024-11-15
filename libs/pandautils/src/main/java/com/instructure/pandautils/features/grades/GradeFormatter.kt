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

package com.instructure.pandautils.features.grades

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertPercentScoreToLetterGrade
import com.instructure.canvasapi2.utils.convertPercentToPointBased
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.qualifiers.ApplicationContext


class GradeFormatter(@ApplicationContext private val context: Context) {

    fun getGradeString(
        course: Course?,
        courseGrade: CourseGrade?,
        isFinal: Boolean
    ): String {
        if (courseGrade == null) return context.getString(R.string.noGradeText)
        return if (isFinal) {
            formatGrade(
                courseGrade.noFinalGrade,
                courseGrade.hasFinalGradeString(),
                courseGrade.finalGrade,
                courseGrade.finalScore,
                course
            )
        } else {
            formatGrade(
                courseGrade.noCurrentGrade,
                courseGrade.hasCurrentGradeString(),
                courseGrade.currentGrade,
                courseGrade.currentScore,
                course
            )
        }
    }

    private fun formatGrade(
        noGrade: Boolean,
        hasGradeString: Boolean,
        grade: String?,
        score: Double?,
        course: Course?
    ): String {
        return if (noGrade) {
            context.getString(R.string.noGradeText)
        } else {
            val restrictQuantitativeData = course?.settings?.restrictQuantitativeData.orDefault()
            if (restrictQuantitativeData) {
                val gradingScheme = course?.gradingScheme.orEmpty()
                when {
                    hasGradeString -> grade.orEmpty()
                    gradingScheme.isNotEmpty() && score != null -> convertPercentScoreToLetterGrade(score / 100, gradingScheme)
                    else -> context.getString(R.string.noGradeText)
                }
            } else {
                val result = if (course?.pointsBasedGradingScheme == true) {
                    convertPercentToPointBased(score.orDefault(), course.scalingFactor.orDefault())
                } else {
                    NumberHelper.doubleToPercentage(score.orDefault())
                }
                if (hasGradeString) "$result ($grade)" else result
            }
        }
    }
}
