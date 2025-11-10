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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentDueDatesInteractionTest : TeacherComposeTest() {

    @Test
    override fun displaysPageObjects() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertPageObjects()
    }

    @Test
    fun displaysNoDueDate() {
        getToDueDatesPage()
        assignmentDueDatesPage.assertDisplaysNoDueDate()
    }

    @Test
    fun displaysSingleDueDate() {
        getToDueDatesPage(dueAt = 7.days.fromNow.iso8601)
        assignmentDueDatesPage.assertDisplaysSingleDueDate()
    }

    @Test
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
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openAllDatesPage()

        return assignment
    }
}
