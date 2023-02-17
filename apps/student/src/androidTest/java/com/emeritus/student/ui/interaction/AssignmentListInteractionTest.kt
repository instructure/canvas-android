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
package com.emeritus.student.ui.interaction

import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.tokenLogin
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentListInteractionTest : StudentTest() {

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    override fun displaysPageObjects() {
        getToAssignmentsPage(0)
        assignmentListPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage(0)
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysAssignment() {
        val assignment = getToAssignmentsPage()[0]
        assignmentListPage.assertHasAssignment(assignment)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun sortAssignmentsByTimeByDefault() {
        val assignment = getToAssignmentsPage()[0]
        assignmentListPage.assertHasAssignment(assignment)
        assignmentListPage.assertSortByButtonShowsSortByTime()
        assignmentListPage.assertFindsUndatedAssignmentLabel()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun sortAssignmentsByTypeWhenTypeIsSelectedInTheDialog() {
        val assignment = getToAssignmentsPage()[0]

        assignmentListPage.selectSortByType()

        assignmentListPage.assertHasAssignment(assignment)
        assignmentListPage.assertSortByButtonShowsSortByType()
    }

    private fun getToAssignmentsPage(assignmentCount: Int = 1): List<Assignment> {
        val data = MockCanvas.init(
                courseCount = 1,
                favoriteCourseCount = 1,
                studentCount = 1,
                teacherCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students.first()

        val assignmentList = mutableListOf<Assignment>()
        repeat(assignmentCount) {
            val assignment = data.addAssignment(
                    courseId = course.id,
                    submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
            )
            assignmentList.add(assignment)
        }

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        return assignmentList
    }

}

