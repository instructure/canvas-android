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

import com.instructure.dataseeding.model.AssignmentListApiModel
import com.instructure.espresso.TestRail
import com.instructure.espresso.ditto.Ditto
import com.instructure.espresso.ditto.DittoMode
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AssignmentListPageTest : TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3109578")
    override fun displaysPageObjects() {
        getToAssignmentsPage()
        assignmentListPage.assertPageObjects()
    }

    @Test
    @Ditto(mode = DittoMode.RECORD)
    @TestRail(ID = "C3134487")
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage(0)
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3109578")
    fun displaysAssignment() {
        val assignment = getToAssignmentsPage().assignmentList[0]
        assignmentListPage.assertHasAssignment(assignment)
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134488")
    fun displaysGradingPeriods() {
        getToAssignmentsPage(gradingPeriods = true)
        assignmentListPage.assertHasGradingPeriods()
    }

    @Test
    @Ditto
    fun searchesAssignments() {
        val assignments = getToAssignmentsPage(assignments = 4)
        val searchAssignment = assignments.assignmentList[3]
        assignmentListPage.assertAssignmentCount(assignments.assignmentList.size + 1) // +1 to account for header
        assignmentListPage.openSearch()
        assignmentListPage.enterSearchQuery(searchAssignment.name.take(searchAssignment.name.length / 2))
        assignmentListPage.assertAssignmentCount(2) // header + single search result
        assignmentListPage.assertHasAssignment(searchAssignment)
    }

    private fun getToAssignmentsPage(assignments: Int = 1, gradingPeriods: Boolean = false): AssignmentListApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, gradingPeriods = gradingPeriods)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val seededAssignments = seedAssignments(
                courseId = course.id,
                assignments = assignments,
                teacherToken = teacher.token)

        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        return seededAssignments
    }
}

