/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.parentapp.models

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.isValid
import java.util.*

fun Course.isValidForParent(): Boolean {
    // Course must have a valid name
    if (!name.isValid()) return false

    // Access must not be explicitly restricted by date
    if (accessRestrictedByDate) return false

    // Must not be in the 'completed' workflow state (i.e. must not be concluded)
    if (workflowState == "completed") return false

    val now = Date()
    val courseDatePassed = endDate?.before(now) == true

    // If restrictEnrollmentsToCourseDate is true then the course date overrides term and section dates
    return if (restrictEnrollmentsToCourseDate) {
        !courseDatePassed
    } else {
        val termDatePassed = term?.endDate?.before(now) == true
        val allSectionsPassed = sections.isNotEmpty() && sections.all { it.endDate?.before(now) == true }

        // Must not have passed course, term, or section dates
        !courseDatePassed && !termDatePassed && !allSectionsPassed
    }
}
