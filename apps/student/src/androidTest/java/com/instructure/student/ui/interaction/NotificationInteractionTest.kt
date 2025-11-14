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

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockcanvas.addSubmissionStreamItem
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.utils.Const
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test
import java.util.UUID

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class NotificationInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testClick_itWorks() {
        // Test that push notifications work when you click on them
        val data = goToNotifications()
        val assignment = data.assignments.values.first()

        notificationPage.assertNotificationDisplayed(assignment.name!!)
        notificationPage.clickNotification(assignment.name!!)

        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfNotRestricted_points() {
        val grade = "10.0"
        val data = goToNotifications(
            restrictQuantitativeData = false,
            gradingType = Assignment.GradingType.POINTS,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfNotRestricted_percent() {
        val grade = "10%"
        val data = goToNotifications(
            restrictQuantitativeData = false,
            gradingType = Assignment.GradingType.PERCENT,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfNotRestricted_letter() {
        val grade = "A"
        val data = goToNotifications(
            restrictQuantitativeData = false,
            gradingType = Assignment.GradingType.LETTER_GRADE,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfNotRestricted_gpa() {
        val grade = "GPA"
        val data = goToNotifications(
            restrictQuantitativeData = false,
            gradingType = Assignment.GradingType.GPA_SCALE,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfNotRestricted_passFail() {
        val grade = "complete"
        val data = goToNotifications(
            restrictQuantitativeData = false,
            gradingType = Assignment.GradingType.PASS_FAIL,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }


    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeUpdatedIfRestricted_points() {
        val grade = "15.0"
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.POINTS,
            score = 15.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, "C")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_convertGradeIfRestricted_percent() {
        val grade = "50%"
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.PERCENT,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, "F")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfRestricted_letter() {
        val grade = "A"
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.LETTER_GRADE,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfRestricted_gpa() {
        val grade = "GPA"
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.GPA_SCALE,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showGradeIfRestricted_passFail() {
        val grade = "complete"
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.PASS_FAIL,
            score = 10.0,
            grade = grade
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertHasGrade(assignment.name!!, grade)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showExcused() {
        val data = goToNotifications(
            restrictQuantitativeData = true,
            gradingType = Assignment.GradingType.POINTS,
            excused = true
        )

        val assignment = data.assignments.values.first()

        notificationPage.assertExcused(assignment.name!!)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showCheckpointLabelForReplyToTopic() {
        val data = goToNotifications(checkpointTag = Const.REPLY_TO_TOPIC)

        val assignment = data.assignments.values.first()

        notificationPage.assertCheckpointLabel(assignment.name!!, "Reply to topic")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.NOTIFICATIONS, TestCategory.INTERACTION)
    fun testNotificationList_showCheckpointLabelForAdditionalReplies() {
        val replyCount = 3
        val data = goToNotifications(checkpointTag = Const.REPLY_TO_ENTRY, replyRequiredCount = replyCount)

        val assignment = data.assignments.values.first()

        notificationPage.assertCheckpointLabel(assignment.name!!, "Additional replies ($replyCount)")
    }

    private fun goToNotifications(
        numSubmissions: Int = 1,
        restrictQuantitativeData: Boolean = false,
        gradingType: Assignment.GradingType = Assignment.GradingType.POINTS,
        score: Double = -1.0,
        grade: String? = null,
        excused: Boolean = false,
        checkpointTag: String? = null,
        replyRequiredCount: Int = 0
    ): MockCanvas {
        val data = MockCanvas.init(courseCount = 1, favoriteCourseCount = 1, studentCount = 1, teacherCount = 1)

        val course = data.courses.values.first()
        val student = data.students.first()

        val gradingScheme = listOf(
            listOf("A", 0.9),
            listOf("B", 0.8),
            listOf("C", 0.7),
            listOf("D", 0.6),
            listOf("F", 0.0)
        )

        data.courses[course.id] = course.copy(
            settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData),
            gradingSchemeRaw = gradingScheme)

        repeat(numSubmissions) {
            val isCheckpoint = checkpointTag != null

            val checkpoints = if (isCheckpoint) {
                listOf(
                    Checkpoint(
                        name = if (checkpointTag == Const.REPLY_TO_TOPIC) "Reply to topic" else "Additional replies",
                        tag = checkpointTag,
                        pointsPossible = 10.0,
                        dueAt = 1.days.ago.iso8601,
                        overrides = emptyList()
                    )
                )
            } else {
                emptyList()
            }

            val submissionType = if (isCheckpoint) {
                Assignment.SubmissionType.DISCUSSION_TOPIC
            } else {
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY
            }

            val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(submissionType),
                gradingType = Assignment.gradingTypeToAPIString(gradingType).orEmpty(),
                pointsPossible = 20,
                checkpoints = checkpoints
            )

            val finalAssignment = if (isCheckpoint) {
                assignment.copy(
                    subAssignmentTag = checkpointTag,
                    discussionTopicHeader = DiscussionTopicHeader(
                        id = assignment.id,
                        replyRequiredCount = replyRequiredCount
                    )
                )
            } else {
                assignment
            }

            if (isCheckpoint) {
                data.assignments[assignment.id] = finalAssignment
            }

            val submission = data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = student.id,
                type = submissionType.apiString,
                body = if (isCheckpoint) "Discussion checkpoint submission" else "Some words + ${UUID.randomUUID()}"
            )

            data.addSubmissionStreamItem(
                user = student,
                course = course,
                assignment = finalAssignment,
                submission = submission,
                submittedAt = 1.days.ago.iso8601,
                type = "submission",
                score = score,
                grade = grade,
                excused = excused
            )
        }

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.waitForRender()
        dashboardPage.clickNotificationsTab()

        return data
    }
}
