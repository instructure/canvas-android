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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AssignmentDueDatesPageTest : TeacherTest() {

    @Test
    @TestRail(ID = "C3134131")
    override fun displaysPageObjects() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertPageObjects()
    }

    @Test
    @TestRail(ID = "C3134484")
    fun displaysNoDueDate() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysNoDueDate()
    }

    @Test
    @TestRail(ID = "C3134485")
    fun displaysSingleDueDate() {
        getToDueDatesPage(dueAt = 7.days.fromNow.iso8601)
        assignmentDueDatesPage.assertDisplaysSingleDueDate()
    }

    @Test
    @TestRail(ID = "C3134486")
    fun displaysAvailabilityDates() {
        getToDueDatesPage(lockAt = 7.days.fromNow.iso8601, unlockAt = 7.days.ago.iso8601)
        assignmentDueDatesPage.assertDisplaysAvailabilityDates()
    }

    private fun getToDueDatesPage(dueAt: String? = null, lockAt: String? = null, unlockAt: String? = null): Assignment {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                dueAt = dueAt,
                lockAt = lockAt,
                unlockAt = unlockAt,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openAllDatesPage()

        return assignment
    }
}
