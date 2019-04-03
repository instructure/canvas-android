/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui

import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class AssignmentDueDatesPageTest : TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3134131")
    override fun displaysPageObjects() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertPageObjects()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134484")
    fun displaysNoDueDate() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysNoDueDate()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134485")
    fun displaysSingleDueDate() {
        getToDueDatesPage(dueAt = 7.days.fromNow.iso8601)
        assignmentDueDatesPage.assertDisplaysSingleDueDate()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134486")
    fun displaysAvailabilityDates() {
        getToDueDatesPage(lockAt = 7.days.fromNow.iso8601, unlockAt = 7.days.ago.iso8601)
        assignmentDueDatesPage.assertDisplaysAvailabilityDates()
    }

    private fun getToDueDatesPage(dueAt: String = "", lockAt: String = "", unlockAt: String = ""): AssignmentApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val assignments = seedAssignments(
                courseId = course.id,
                assignments = 1,
                dueAt = dueAt,
                lockAt = lockAt,
                unlockAt = unlockAt,
                teacherToken = teacher.token)
        val assignment = assignments.assignmentList[0]

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openAllDatesPage()

        return assignment
    }
}
