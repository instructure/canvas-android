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
package com.emeritus.student.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class PushNotificationInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NONE, TestCategory.INTERACTION, false)
    fun testClick_itWorks() {
        // Test that push notifications work when you click on them
        val data = goToNotifications()
        val assignment = data.assignments.values.first()

        notificationPage.assertNotificationDisplayed(assignment.name!!)
        notificationPage.clickNotification(assignment.name!!)

        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    private fun goToNotifications(numSubmissions: Int = 1) : MockCanvas {
        val data = MockCanvas.init(courseCount = 1, favoriteCourseCount = 1, studentCount = 1, teacherCount = 1)

        val course = data.courses.values.first()
        val student = data.students.first()

        repeat(numSubmissions) {
            val assignment = data.addAssignment(
                    courseId = course.id,
                    submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
            )

            val submission = data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = student.id,
                    type = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                    body = "Some words + ${UUID.randomUUID()}"
            )

            val streamItem = data.addSubmissionStreamItem(
                    user = student,
                    course = course,
                    assignment = assignment,
                    submission = submission,
                    submittedAt = 1.days.ago.iso8601,
                    type = "submission"
            )
        }

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.waitForRender()
        dashboardPage.clickNotificationsTab()

        return data
    }

}