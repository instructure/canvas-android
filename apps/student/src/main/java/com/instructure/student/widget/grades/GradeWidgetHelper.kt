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
package com.instructure.student.widget.grades

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const

internal fun Course.toWidgetCourseItem(apiPrefs: ApiPrefs): WidgetCourseItem {
    val themedColor = ColorKeeper.getOrGenerateColor(this)
    return WidgetCourseItem(
        name,
        courseCode ?: name,
        isLocked(),
        getGradeText(),
        themedColor.light,
        themedColor.dark,
        getUrl(apiPrefs)
    )
}

private fun Course.getGradeText(): String? {
    return if (!isTeacher && !isTA) {
        val courseGrade = getCourseGrade(false)
        if (courseGrade == null || courseGrade.isLocked || courseGrade.noCurrentGrade) {
            ""
        } else if (settings?.restrictQuantitativeData == true) {
            if (courseGrade.currentGrade.isNullOrEmpty()) {
                ""
            } else {
                courseGrade.currentGrade.orEmpty()
            }
        } else {
            val scoreString = NumberHelper.doubleToPercentage(courseGrade.currentScore, 2)
            "${if (courseGrade.hasCurrentGradeString()) courseGrade.currentGrade else ""} $scoreString"
        }
    } else {
        null
    }
}

private fun Course.isLocked(): Boolean {
    val courseGrade = getCourseGrade(false)
    return courseGrade == null || courseGrade.isLocked
}

private fun Course.getUrl(apiPrefs: ApiPrefs): String {
    val domain = apiPrefs.fullDomain

    val courseUrl = Const.COURSE_URL + id
    return domain + courseUrl + Const.GRADE_URL
}
