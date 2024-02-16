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
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToAssignmentsPage()
        assignmentListPage.assertPageObjects()
    }

    @Test
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage(0)
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    fun displaysAssignment() {
        val assignment = getToAssignmentsPage().assignments.values.first()
        assignmentListPage.assertHasAssignment(assignment)
    }

    @Test
    fun displaysGradingPeriods() {
        getToAssignmentsPage(gradingPeriods = true)
        assignmentListPage.assertHasGradingPeriods()
    }

    @Test
    fun searchesAssignments() {
        val data = getToAssignmentsPage(assignments = 4)
        val searchAssignment = data.assignments.values.toList()[3]
        assignmentListPage.assertAssignmentCount(data.assignments.values.size + 1) // +1 to account for header
        assignmentListPage.openSearch()
        val assignmentName = searchAssignment.name!!
        assignmentListPage.enterSearchQuery(assignmentName.take(assignmentName.length / 2))
        assignmentListPage.assertAssignmentCount(2) // header + single search result
        assignmentListPage.assertHasAssignment(searchAssignment)
    }

    private fun getToAssignmentsPage(assignments: Int = 1, gradingPeriods: Boolean = false): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1, withGradingPeriods = gradingPeriods)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        // Create a single assignment group, to which all assignments will be assigned
        val assignmentGroup = AssignmentGroup(id = 1, name = "assignments")
        data.assignmentGroups[course.id] = mutableListOf(assignmentGroup)

        // Create the requested number of assignments
        repeat(assignments) {
            data.addAssignment(
                    courseId = course.id,
                    submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                    assignmentGroupId = assignmentGroup.id)
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        return data
    }
}

