/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.SecondaryFeatureCategory
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.routeTo
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Test

@HiltAndroidTest
class AssignmentDetailsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false, SecondaryFeatureCategory.SUBMISSIONS_ONLINE_URL)
    fun testSubmission_submitAssignment() {
        // TODO - Test submitting for each submission type
        // For now, I'm going to just test one submission type
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignment = data.addAssignment(courseId = course.id, submissionType = Assignment.SubmissionType.ONLINE_URL)
        val submission = data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = data.users.values.first().id,
                type = Assignment.SubmissionType.ONLINE_URL.apiString)
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        urlSubmissionUploadPage.submitText("https://google.com")
        assignmentDetailsPage.assertSubmittedStatus()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false)
    fun testNavigating_viewSubmissionDetails() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        goToAssignmentFromList()
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testNavigating_viewAssignmentDetails() {
        // Test clicking on the Assignment item in the Assignment List to load the Assignment Details Page
        goToAssignmentFromList()
        assignmentDetailsPage.assertPageObjects()
    }

    @Stub
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, SecondaryFeatureCategory.ASSIGNMENT_QUIZZES)
    fun testQuizzesNext_launchQuizzesNextAssignment() {
        // Launch into Quizzes.Next assignment
    /* First attempt based on hardcoded verifier response
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignment = data.addAssignment(courseId = course.id, groupType = AssignmentGroupType.UPCOMING, submissionType = Assignment.SubmissionType.EXTERNAL_TOOL, isQuizzesNext = true)
        val submission = Submission(
                id = 123L,
                submittedAt = Date(),
                attempt = 1L,
                late = false
        )
        data.addSubmission(course.id, submission, assignment.id)
        data.addLTITool("Quizzes 2", "https://mobiledev.instructure.com/courses/1567973/external_tools/sessionless_launch?verifier=f85d3d69189890cde2f427a8efdc0e64850d8583bf8f2e0e0fa3704782d48b5378df5d52a35a4497ec18d3b0e201b3b2cab95e1347e7c5e286ac6636bf295c6b")
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        //https://mobiledev.instructure.com/api/v1/courses/1567973/external_tools/sessionless_launch?assignment_id=24378681&launch_type=assessment
    */
    }

    private fun goToAssignmentFromList() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignmentGroups = data.addAssignmentsToGroups(course)
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        // Let's find and click an assignment with a submission, so that we get meaningful
        // data in the submission details.
        val assignmentWithSubmission = assignmentGroups.flatMap { it.assignments }.find {it.submission != null}
        assertNotNull("Expected at least one assignment with a submission", assignmentWithSubmission)
        assignmentListPage.clickAssignment(assignmentWithSubmission!!)
    }

}
