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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignments
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.routeTo
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class AssignmentDetailsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.SUBMISSIONS)
    fun testSubmission_submitAssignment() {
        // Test submitting for each submission type
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.SUBMISSIONS)
    fun testNavigating_viewSubmissionDetails() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true)
    fun testNavigating_viewAssignmentDetails() {
        // Test clicking on the Assignment item in the Assignment List to load the Assignment Details Page
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignmentGroups = data.addAssignments(course)
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        assignmentListPage.clickAssignment(assignmentGroups.first().assignments.first())
        assignmentDetailsPage.assertPageObjects()
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.QUIZZES)
    fun testGauge_launchQuizzesNextAssignment() {
        // Launch into Quizzes.Next assignment
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true)
    fun testAssignments_previewAttachment() {
        // Student can preview an assignment attachment
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.BOOKMARKS)
    fun testAssignments_createBookmark() {
        // Student can bookmark the assignment
    }

}
