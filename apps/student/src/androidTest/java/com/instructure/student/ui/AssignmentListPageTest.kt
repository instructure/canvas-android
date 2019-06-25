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
package com.instructure.student.ui

import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.AssignmentListApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.student.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import org.junit.Test

class AssignmentListPageTest : StudentTest() {

    @Test
    @Ditto
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    override fun displaysPageObjects() {
        getToAssignmentsPage(0)
        assignmentListPage.assertPageObjects()
    }

    @Test
    @Ditto
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysNoAssignmentsView() {
        getToAssignmentsPage(0)
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @Ditto
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysAssignment() {
        val assignment = getToAssignmentsPage().assignmentList[0]
        assignmentListPage.assertHasAssignment(assignment)
    }

    private fun getToAssignmentsPage(assignmentCount: Int = 1): AssignmentListApiModel {
        val data = seedData(teachers = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val assignments = seedAssignments(
            courseId = course.id,
            assignments = assignmentCount,
            teacherToken = teacher.token,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        if (assignmentCount > 0) {
            val submissions = listOf(
                    SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)
            )

            seedAssignmentSubmission(
                submissionSeeds = submissions,
                assignmentId = assignments.assignmentList[0].id,
                courseId = course.id,
                studentToken = if (data.studentsList.isEmpty()) "" else data.studentsList[0].token
            )
        }

        tokenLogin(student)
        routeTo("courses/${course.id}/assignments")
        return assignments
    }

}

