/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.util

import android.content.Context
import android.content.res.Resources
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.getShortMonthAndDay
import com.instructure.pandautils.utils.getTime
import com.instructure.student.R
import org.threeten.bp.OffsetDateTime
import java.util.*

suspend fun Long.isStudioEnabled(): Boolean {
    val context = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, this)
    return ExternalToolManager.getExternalToolsForCanvasContextAsync(context, true).await().dataOrNull?.any {
        it.url?.contains("instructuremedia.com/lti/launch") ?: false
    } ?: false
}

suspend fun Long.getStudioLTITool(): DataResult<LTITool> {
    val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, this)
    val studioLTITool = ExternalToolManager.getExternalToolsForCanvasContextAsync(canvasContext, true).await()
        .dataOrNull?.firstOrNull {
        it.url?.contains("instructuremedia.com/lti/launch") ?: false
    }
    return if (studioLTITool != null)
        DataResult.Success(studioLTITool)
    else DataResult.Fail()
}

fun LTITool.getResourceSelectorUrl(canvasContext: CanvasContext, assignment: Assignment) =
    String.format(Locale.getDefault(), "%s/%s/external_tools/%d/resource_selection?launch_type=homework_submission&assignment_id=%d", ApiPrefs.fullDomain, canvasContext.toAPIString(), this.id, assignment.id)

fun String.toDueAtString(context: Context): String {
    val dueDateTime = OffsetDateTime.parse(this).withOffsetSameInstant(OffsetDateTime.now().offset)
    return context.getString(com.instructure.pandares.R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(), dueDateTime.getTime())
}

fun Assignment.getDisplayGrade(
    submission: Submission?,
    resources: Resources,
    includePointsPossible: Boolean = true,
    includeLatePenalty: Boolean = false
): DisplayGrade {
    // If the submission doesn't exist, so we return an empty string
    if(submission == null) return DisplayGrade()

    // Cover the first edge case: excused assignment
    if(submission.excused) {
        return DisplayGrade(resources.getString(R.string.excused))
    }

    // Cover the second edge case: NOT_GRADED type and no grade
    if(Assignment.getGradingTypeFromAPIString(this.gradingType ?: "") == Assignment.GradingType.NOT_GRADED) {
        return DisplayGrade(resources.getString(R.string.notGraded))
    }

    // First lets see if the assignment is graded
    if(submission.grade != null && submission.grade != "null") {
        return when(Assignment.getGradingTypeFromAPIString(this.gradingType ?: "")) {
            Assignment.GradingType.POINTS ->
                if(includeLatePenalty) {
                    getPointsFraction(resources, submission.enteredScore, this.pointsPossible)
                } else {
                    getPointsFraction(resources, submission.score, this.pointsPossible)
                }
            //edge case, NOT_GRADED type with grade, it COULD happen
            Assignment.GradingType.NOT_GRADED -> DisplayGrade(resources.getString(R.string.notGraded))
            else ->{
                var grade = submission.grade.takeUnless { it == "null" }.orEmpty()
                if (this.gradingType == Assignment.PERCENT_TYPE) {
                    try {
                        val value: Double = if(includeLatePenalty) submission.enteredGrade?.removeSuffix("%")?.toDouble() as Double else submission.grade?.removeSuffix("%")?.toDouble() as Double
                        grade = NumberHelper.doubleToPercentage(value, 2)
                    } catch (e: NumberFormatException) { }
                }
                when(submission.grade) {
                    "complete" ->
                        grade = resources.getString(R.string.gradeComplete)
                    "incomplete" ->
                        grade = resources.getString(R.string.gradeIncomplete)
                }
                if (includePointsPossible) {
                    if(includeLatePenalty) {
                        getPointsFractionWithGrade(resources, submission.enteredScore, this.pointsPossible, grade)
                    } else {
                        getPointsFractionWithGrade(resources, submission.score, this.pointsPossible, grade)
                    }
                } else {
                    DisplayGrade(grade)
                }
            }

        }
    } else {
        //return empty string for "empty" state
        return DisplayGrade()
    }
}

private fun getPointsFraction(resources: Resources, points: Double, pointsPossible: Double): DisplayGrade {
    val pointsText = NumberHelper.formatDecimal(points, 2, true)
    val possibleText = NumberHelper.formatDecimal(pointsPossible, 2, true)
    val text = resources.getString(R.string.gradeFormatScoreOutOfPointsPossible, pointsText, possibleText)
    val contentDescription = resources.getString(R.string.contentDescriptionScoreOutOfPointsPossible, pointsText, possibleText)
    return  DisplayGrade(text, contentDescription)
}

fun getPointsFractionWithGrade(resources: Resources, points: Double, pointsPossible: Double, grade: String?): DisplayGrade {
    val pointsText = NumberHelper.formatDecimal(points, 2, true)
    val possibleText = NumberHelper.formatDecimal(pointsPossible, 2, true)
    val text = resources.getString(R.string.formattedScoreWithPointsPossibleAndGrade, pointsText, possibleText, grade)
    val contentDescription = resources.getString(
        R.string.contentDescriptionScoreWithPointsPossibleAndGrade,
        pointsText,
        possibleText,
        getContentDescriptionForMinusGradeString(grade.orEmpty(), resources)
    )
    return DisplayGrade(text, contentDescription)
}

fun getContentDescriptionForMinusGradeString(grade: String, resources: Resources): String {
    return if (grade.contains("-")) {
        resources.getString(
            com.instructure.pandautils.R.string.a11y_gradeLetterMinusContentDescription,
            grade.substringBefore("-"))
    } else grade
}

