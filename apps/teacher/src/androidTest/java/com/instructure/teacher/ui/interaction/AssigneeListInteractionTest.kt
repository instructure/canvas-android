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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.page.onViewWithId
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssigneeListInteractionTest : TeacherComposeTest() {

    @Test
    override fun displaysPageObjects() {
        getToAssigneeListPage()
        assigneeListPage.assertPageObjects()
    }

    @Test
    fun displaysEveryoneItem() {
        getToAssigneeListPage()
        assigneeListPage.assertDisplaysAssigneeOptions(sectionNames = listOf("Everyone"))
    }

    @Test
    fun displaysStudentItems() {
        val students = getToAssigneeListPage(students = 2).students
        assigneeListPage.assertDisplaysAssigneeOptions(
                sectionNames = listOf("Everyone"),
                studentNames = students.map { it.name }
        )
    }

    @Test
    fun selectsStudents() {
        val studentNames = getToAssigneeListPage(students = 2).students.map{ it.name }
        assigneeListPage.assertDisplaysAssigneeOptions(
                sectionNames = listOf("Everyone"),
                studentNames = studentNames
        )
        assigneeListPage.assertAssigneesSelected(listOf("Everyone"))
        assigneeListPage.toggleAssignees(studentNames)
        val expectedAssignees = studentNames + "Everyone else"
        assigneeListPage.assertAssigneesSelected(expectedAssignees)
        assigneeListPage.saveAndClose()
        val assignText = editAssignmentDetailsPage.onViewWithId(R.id.assignTo)
        for (assignee in expectedAssignees) assignText.assertContainsText(assignee)
    }

    private fun getToAssigneeListPage(students: Int = 0): MockCanvas {

        val data = MockCanvas.init(
                teacherCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1,
                studentCount = students,
                createSections = true)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                    courseId = course.id,
                    submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.editAssignees()
        return data
    }

}
