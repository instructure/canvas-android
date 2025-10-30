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

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockcanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.routeTo
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class AssignmentDetailsInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ONLINE_URL)
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
        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_URL))
        data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = data.users.values.first().id,
            type = Assignment.SubmissionType.ONLINE_URL.apiString
        )
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()
        urlSubmissionUploadPage.submitText("https://google.com")
        assignmentDetailsPage.assertStatusSubmitted()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testSubmissionStatus_NotSubmitted() {

        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val assignmentWithoutSubmissionEntry = assignmentList.filter {it.value.submission == null && it.value.dueAt == null}
        val assignmentWithoutSubmission = assignmentWithoutSubmissionEntry.entries.first().value

        assignmentListPage.clickAssignment(assignmentWithoutSubmission)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()
        assignmentDetailsPage.assertDisplaysDate("No Due Date")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testDisplayToolbarTitles() {
        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val testAssignment = assignmentList.entries.first().value
        val course = data.courses.values.first()

        assignmentListPage.clickAssignment(testAssignment)

        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course!!.name!!)

   }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testDisplayBookmarkMenu() {
        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val testAssignment = assignmentList.entries.first().value

        assignmentListPage.clickAssignment(testAssignment)

        assignmentDetailsPage.openOverflowMenu()
        assignmentDetailsPage.assertDisplaysAddBookmarkButton()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testDisplayDueDate() {
        val data = setUpData()
        goToAssignmentList()
        val calendar = Calendar.getInstance().apply { set(2023, 0, 31, 23, 59, 0) }
        val expectedDueDate = "Jan 31, 2023 11:59 PM"
        val course = data.courses.values.first()
        val assignmentWithNoDueDate = data.addAssignment(course.id, name = "Test Assignment", dueAt = calendar.time.toApiString())
        assignmentListPage.refreshAssignmentList()
        assignmentListPage.clickAssignment(assignmentWithNoDueDate)

        assignmentDetailsPage.assertDisplaysDate(expectedDueDate)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testDisplayDueDates() {
        val data = setUpData()
        goToAssignmentList()

        var calendar = Calendar.getInstance().apply { set(2023, 0, 29, 23, 59, 0) }
        val expectedReplyToTopicDueDate = "Jan 29, 2023 11:59 PM"
        val replyToTopicDueDate = calendar.time.toApiString()

        calendar = Calendar.getInstance().apply { set(2023, 0, 31, 23, 59, 0) }
        val expectedReplyToEntryDueDate = "Jan 31, 2023 11:59 PM"
        val replyToEntryDueDate = calendar.time.toApiString()
        val course = data.courses.values.first()

        val checkpoints = listOf(
            Checkpoint(
                name = "Reply to Topic",
                tag = "reply_to_topic",
                dueAt = replyToTopicDueDate,
                pointsPossible = 10.0
            ),
            Checkpoint(
                name = "Reply to Entry",
                tag = "reply_to_entry",
                dueAt = replyToEntryDueDate,
                pointsPossible = 10.0
            )
        )
        val assignmentWithNoDueDate = data.addAssignment(course.id, name = "Test Assignment", dueAt = calendar.time.toApiString(), checkpoints = checkpoints)
        assignmentListPage.refreshAssignmentList()
        assignmentListPage.clickAssignment(assignmentWithNoDueDate)

        assignmentDetailsPage.assertDisplaysDate(expectedReplyToTopicDueDate, 0)
        assignmentDetailsPage.assertDisplaysDate(expectedReplyToEntryDueDate, 1)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testNavigating_viewAssignmentDetails() {
        // Test clicking on the Assignment item in the Assignment List to load the Assignment Details Page
        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val assignmentWithSubmissionEntry = assignmentList.filter {it.value.submission != null}
        val assignmentWithSubmission = assignmentWithSubmissionEntry.entries.first().value

        assignmentListPage.clickAssignment(assignmentWithSubmission)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentDetails(assignmentWithSubmission)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testNavigating_viewSubmissionDetailsWithSubmission() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val assignmentWithSubmissionEntry = assignmentList.filter {it.value.submission != null}
        val assignmentWithSubmission = assignmentWithSubmissionEntry.entries.first().value

        assignmentListPage.clickAssignment(assignmentWithSubmission)

        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testNavigating_viewSubmissionDetailsWithoutSubmission() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = setUpData()
        goToAssignmentList()
        val assignmentList = data.assignments
        val assignmentWithoutSubmissionEntry = assignmentList.filter {it.value.submission == null}
        val assignmentWithoutSubmission = assignmentWithoutSubmissionEntry.entries.first().value

        assignmentListPage.clickAssignment(assignmentWithoutSubmission)

        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithoutQuantitativeRestriction() {
        val data = setUpData()
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("B")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("3.7")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeNotDisplayed()
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("EX")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("90%")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("Complete")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 0 pts")
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testLetterGradeAssignmentWithQuantitativeRestriction() {
        val data = setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("B")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testGpaScaleAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("3.7")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "65", 65.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("D")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPointsAssignmentExcusedWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("EX")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPercentageAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "70%", 70.0, 100)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("C")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testPassFailAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertGradeDisplayed("Complete")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_MULTIPLE_TYPE)
    fun testSubmission_multipleSubmissionType() {
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, Assignment.SubmissionType.ONLINE_UPLOAD, Assignment.SubmissionType.MEDIA_RECORDING, Assignment.SubmissionType.DISCUSSION_TOPIC, Assignment.SubmissionType.ONLINE_URL))
        data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = data.users.values.first().id,
            type = Assignment.SubmissionType.ONLINE_URL.apiString
        )
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()

        assignmentDetailsPage.assertSubmissionTypeDisplayed("Text Entry")
        assignmentDetailsPage.assertSubmissionTypeDisplayed("Website URL")
        assignmentDetailsPage.assertSubmissionTypeDisplayed("File Upload")
        assignmentDetailsPage.assertSubmissionTypeDisplayed("Media Recording")

        //Try 1 submission to check if it's possible to submit even when there are multiple submission types available.
        assignmentDetailsPage.selectSubmissionType(SubmissionType.ONLINE_URL)
        urlSubmissionUploadPage.submitText("https://google.com")
        assignmentDetailsPage.assertStatusSubmitted()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testReminderSectionIsVisibleWhenThereIsNoFutureDueDate() {
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)

        assignmentReminderPage.assertReminderSectionDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testReminderSectionsAreVisibleWhenThereAreNoFutureDueDates() {
        val data = setUpData()
        val course = data.courses.values.first()

        val pastDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time.toApiString()

        val checkpoints = listOf(
            Checkpoint(
                name = "Reply to Topic",
                tag = "reply_to_topic",
                dueAt = pastDate,
                pointsPossible = 10.0
            ),
            Checkpoint(
                name = "Reply to Entry",
                tag = "reply_to_entry",
                dueAt = pastDate,
                pointsPossible = 10.0
            )
        )
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = pastDate, checkpoints = checkpoints)
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.assertReminderViewDisplayed(0)
        assignmentDetailsPage.assertReminderViewDisplayed(1)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testReminderSectionIsVisibleWhenThereIsNoDueDate() {
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment")
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)

        assignmentReminderPage.assertReminderSectionDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testReminderSectionIsVisibleWhenThereIsFutureDueDate() {
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)

        assignmentReminderPage.assertReminderSectionDisplayed()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testAddReminder() {
        val reminderCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 2)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)
        assignmentReminderPage.clickAddReminder()
        assignmentReminderPage.clickCustomReminderOption()
        assignmentReminderPage.selectDate(reminderCalendar)
        assignmentReminderPage.selectTime(reminderCalendar)

        assignmentReminderPage.assertReminderDisplayedWithText(reminderCalendar.time.toFormattedString())
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testRemoveReminder() {
        val reminderCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 2)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)
        assignmentReminderPage.clickAddReminder()
        assignmentReminderPage.clickCustomReminderOption()
        assignmentReminderPage.selectDate(reminderCalendar)
        assignmentReminderPage.selectTime(reminderCalendar)


        assignmentReminderPage.assertReminderDisplayedWithText(reminderCalendar.time.toFormattedString())

        assignmentReminderPage.removeReminderWithText(reminderCalendar.time.toFormattedString())

        assignmentReminderPage.assertReminderNotDisplayedWithText(reminderCalendar.time.toFormattedString())
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testAddReminderInPastShowsError() {
        val reminderCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 30)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)
        assignmentReminderPage.clickAddReminder()
        assignmentReminderPage.clickCustomReminderOption()
        assignmentReminderPage.selectDate(reminderCalendar)
        assignmentReminderPage.selectTime(reminderCalendar)

        checkToastText(R.string.reminderInPast, activityRule.activity)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testAddReminderForTheSameTimeShowsError() {
        val reminderCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val data = setUpData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 2)
        }.time.toApiString())
        goToAssignmentList()

        assignmentListPage.clickAssignment(assignment)

        assignmentReminderPage.clickAddReminder()
        assignmentReminderPage.clickCustomReminderOption()
        assignmentReminderPage.selectDate(reminderCalendar)
        assignmentReminderPage.selectTime(reminderCalendar)

        assignmentReminderPage.clickAddReminder()
        assignmentReminderPage.clickCustomReminderOption()
        assignmentReminderPage.selectDate(reminderCalendar)
        assignmentReminderPage.selectTime(reminderCalendar)

        checkToastText(R.string.reminderAlreadySet, activityRule.activity)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun testDiscussionCheckpointsDisplayed() {
        val data = setUpData()
        val course = data.courses.values.first()

        val checkpoint1 = Checkpoint(
            tag = "reply_to_topic",
            pointsPossible = 5.0,
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        val checkpoint2 = Checkpoint(
            tag = "reply_to_entry",
            pointsPossible = 5.0,
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time.toApiString()
        )

        val assignment = data.addAssignment(
            courseId = course.id,
            name = "Discussion Checkpoint Assignment",
            checkpoints = listOf(checkpoint1, checkpoint2),
            submissionTypeList = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC)
        )

        goToAssignmentList()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.assertCheckpointDisplayed(0, "Reply to topic", "-/5")
        assignmentDetailsPage.assertCheckpointDisplayed(1, "Additional replies (0)", "-/5")
    }

    private fun setUpData(restrictQuantitativeData: Boolean = false): MockCanvas {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = 1
        )

        val course = data.courses.values.first()

        val gradingScheme = listOf(
            listOf("A", 0.9),
            listOf("B", 0.8),
            listOf("C", 0.7),
            listOf("D", 0.6),
            listOf("F", 0.0)
        )

        data.courseSettings[course.id] = CourseSettings(restrictQuantitativeData = restrictQuantitativeData)

        val newCourse = course
            .copy(settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData),
                gradingSchemeRaw = gradingScheme)
        data.courses[course.id] = newCourse

        data.addAssignmentsToGroups(newCourse)

        return data
    }

    private fun goToAssignmentList() {
        val data = MockCanvas.data
        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        val assignmentGroups = data.assignmentGroups[course.id]!!

        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        // Let's find and click an assignment with a submission, so that we get meaningful
        // data in the submission details.
        val assignmentWithSubmission = assignmentGroups.flatMap { it.assignments }.find {it.submission != null}
        val assignmentWithoutSubmission = assignmentGroups.flatMap { it.assignments }.find {it.submission == null}
        assertNotNull("Expected at least one assignment with a submission", assignmentWithSubmission)
        assertNotNull("Expected at least one assignment without a submission", assignmentWithoutSubmission)
    }

    private fun addAssignment(data: MockCanvas, gradingType: Assignment.GradingType, grade: String?, score: Double?, maxScore: Int, excused: Boolean = false): Assignment {
        val course = data.courses.values.first()
        val student = data.students.first()

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            gradingType = Assignment.gradingTypeToAPIString(gradingType) ?: "",
            pointsPossible = maxScore,
        )

        data.addSubmissionForAssignment(assignment.id, student.id, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString, grade = grade, score = score, excused = excused)

        return assignment
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }
}
