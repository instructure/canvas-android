/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * CourseGrade object for displaying course grade totals. Property should always be accessed from
 * Course.getCourseGrade()
 *
 * Note: Current vs Final
 * As a general rule, current represents a grade calculated only from graded assignments, where as
 * final grades use all assignments, regardless of grading status, in their calculation.
 *
 * @currentGrade - Current grade string value, for the current grading period or the current term
 * (see Course.getCourseGrade, ignoreMGP).
 *
 * @currentScore - Current score value, a double representation of a percentage grade, for the
 * current grading period or the current term (see Course.getCourseGrade, ignoreMGP). Needs
 * formatting prior to use.
 *
 * @finalGrade - Final grade string value, for the current grading period or the current term
 * (see Course.getCourseGrade, ignoreMGP).
 *
 * @finalScore - Final score value, a double representation of a percentage grade, for the current
 * grading period or the current term (see Course.getCourseGrade, ignoreMGP). Needs formatting
 * prior to use.
 *
 * @isLocked - Represents the lock status of a course, this is different from hideFinalGrades, as
 * it takes both that value, and totalsForAllGradingPeriodsOption into account. The latter is only
 * used when relevant, see Course.isCourseGradeLocked
 *
 * @noCurrentGrade - If the course contains no valid current grade or score, this flag will be true. This is usually
 * represented in the UI with "N/A". See Course.noCurrentGrade for logic.
 *
 * @noFinalGrade - If the course contains no valid final grade or score, this flag will be true. This is usually
 * represented in the UI with "N/A". See Course.noFinalGrade for logic.
 */
@Parcelize
data class CourseGrade(
        var currentGrade: String? = null,
        var currentScore: Double? = null,
        var finalGrade: String? = null,
        var finalScore: Double? = null,
        var isLocked: Boolean = false,
        var noCurrentGrade: Boolean = false,
        var noFinalGrade: Boolean = false) : Parcelable {

    fun hasCurrentGradeString(): Boolean {
        return currentGrade?.let {
            !(it.contains("N/A") || it.isEmpty())
        } ?: false
    }

    fun hasFinalGradeString(): Boolean {
        return finalGrade?.let {
            !(it.contains("N/A") || it.isEmpty())
        } ?: false
    }

}

