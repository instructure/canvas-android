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

import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.page.onViewWithId
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class AssigneeListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToAssigneeListPage()
        assigneeListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysEveryoneItem() {
        getToAssigneeListPage()
        assigneeListPage.assertDisplaysAssigneeOptions(sectionNames = listOf("Everyone"))
    }

    @Test
    @Ditto
    fun displaysStudentItems() {
        val students = getToAssigneeListPage(students = 2).studentsList
        assigneeListPage.assertDisplaysAssigneeOptions(
                sectionNames = listOf("Everyone"),
                studentNames = students.map { it.name }
        )
    }

    @Test
    @Ditto
    fun selectsStudents() {
        val studentNames = getToAssigneeListPage(students = 2).studentsList.map{ it.name }
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

    private fun getToAssigneeListPage(
            assignments: Int = 1,
            withDescription: Boolean = false,
            lockAt: String = "",
            unlockAt: String = "",
            students: Int = 0,
            submissionTypes: List<SubmissionType> = emptyList(),
            submissions: List<Pair<String, Int>> = emptyList()): SeedApi.SeededDataApiModel {

        val data = seedData(teachers = 1, favoriteCourses = 1, students = students)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val assignment = seedAssignments(
                assignments = assignments,
                courseId = course.id,
                withDescription = withDescription,
                lockAt = lockAt,
                unlockAt = unlockAt,
                submissionTypes = submissionTypes,
                teacherToken = teacher.token)

        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment.assignmentList[0])

        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.editAssignees()
        return data
    }

}
