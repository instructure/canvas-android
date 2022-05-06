/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testAssignmentsE2E() {
        val data = seedData(teachers = 1, courses = 1, students = 3, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val gradedStudent = data.studentsList[1]

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(courseName = course.name)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.assertDisplaysNoAssignmentsView()

        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(assignment[0])
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(3,3)
        assignmentDetailsPage.assertNeedsGrading(0,3)

        //seed one submission for one student and assert changes
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(2,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)

        //seed another submission and grade it then assert changes
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment[0].id,
                studentId = gradedStudent.id,
                postedGrade = "15",
                excused = false
        )

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertNotSubmitted(1,3)
        assignmentDetailsPage.assertNeedsGrading(1,3)
        assignmentDetailsPage.assertHasGraded(1,3)
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E)
    fun testEditAssignmentE2E() {
        val data = seedData(teachers = 1, courses = 1, students = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(courseName = course.name)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.assertDisplaysNoAssignmentsView()

        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        assignmentListPage.refresh()
        assignmentListPage.assertHasAssignment(assignment[0])

        //assert assignment is created correctly
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentDetails(assignment[0])
        assignmentDetailsPage.assertNotSubmitted(1,1)
        assignmentDetailsPage.assertNeedsGrading(0,1)
        assignmentDetailsPage.assertSubmissionTypeOnlineTextEntry()

        //change and assert assignment name
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickAssignmentNameEditText()
        editAssignmentDetailsPage.editAssignmentName(newName = "New Assignment Name")

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentNameChanged(newAssignmentName = "New Assignment Name")

        //edit and assert assignment points
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPointsPossibleEditText()
        editAssignmentDetailsPage.editAssignmentPoints(newPoints = 20.0)

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertAssignmentPointsChanged(newAssignmentPoints = "20")

        //change assignment to unpublished
        assignmentDetailsPage.openEditPage()
        editAssignmentDetailsPage.clickPublishSwitch()
        editAssignmentDetailsPage.saveAssignment()

        assignmentDetailsPage.refresh()
        assignmentDetailsPage.waitForRender()
        assignmentDetailsPage.assertPublishedStatus(published = false)
    }

}