/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.checkToastText
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockCanvas.addObserverAlert
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.parentapp.R
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test
import java.util.Calendar
import java.util.Date

@HiltAndroidTest
class AssignmentDetailsInteractionTest : ParentComposeTest() {
    override fun displaysPageObjects() = Unit

    private val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions())

    @Test
    fun testSubmissionStatus_Missing() {
        val data = setupData()
        val successfulAssignment = data.assignments.values.first { it.submission == null && it.dueDate == null }
        gotoAssignment(data, successfulAssignment)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()
        assignmentDetailsPage.assertDisplaysDate("No Due Date")
    }

    @Test
    fun testSubmissionStatus_NotSubmitted() {
        val data = setupData()
        val successfulAssignment = data.assignments.values.first { it.submission == null && it.dueDate == null }
        gotoAssignment(data, successfulAssignment)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()
        assignmentDetailsPage.assertDisplaysDate("No Due Date")
    }

    @Test
    fun testDisplayToolbarTitles() {
        val data = setupData()
        val course  = data.courses.values.first()
        val assignment = data.assignments.values.first()
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

   }

    @Test
    fun testDisplayDueDate() {
        val data = setupData()
        val calendar = Calendar.getInstance().apply { set(2023, 0, 31, 23, 59, 0) }
        val expectedDueDate = "January 31, 2023 11:59 PM"
        val course = data.courses.values.first()
        val assignmentWithNoDueDate = data.addAssignment(course.id, name = "Test Assignment", dueAt = calendar.time.toApiString())

        gotoAssignment(data, assignmentWithNoDueDate)

        assignmentDetailsPage.assertDisplaysDate(expectedDueDate)
    }

    @Test
    fun testNavigating_viewAssignmentDetails() {
        // Test clicking on the Assignment item in the Assignment List to load the Assignment Details Page
        val data = setupData()
        val assignmentList = data.assignments
        val assignmentWithSubmissionEntry = assignmentList.filter {it.value.submission != null}
        val assignmentWithSubmission = assignmentWithSubmissionEntry.entries.first().value

        gotoAssignment(data, assignmentWithSubmission)

        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentDetails(assignmentWithSubmission)
    }

    @Test
    fun testLetterGradeAssignmentWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("B")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    fun testGpaScaleAssignmentWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("3.7")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    fun testPointsAssignmentWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeNotDisplayed()
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    fun testPointsAssignmentExcusedWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("EX")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testPercentageAssignmentWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("90%")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 100 pts")
        assignmentDetailsPage.assertScoreDisplayed("90")
    }

    @Test
    fun testPassFailAssignmentWithoutQuantitativeRestriction() {
        val data = setupData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("Complete")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 0 pts")
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testLetterGradeAssignmentWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("B")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testGpaScaleAssignmentWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("3.7")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testPointsAssignmentWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "65", 65.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("D")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testPointsAssignmentExcusedWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("EX")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testPercentageAssignmentWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "70%", 70.0, 100)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("C")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testPassFailAssignmentWithQuantitativeRestriction() {
        val data = setupData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertGradeDisplayed("Complete")
        assignmentDetailsPage.assertOutOfTextNotDisplayed()
        assignmentDetailsPage.assertScoreNotDisplayed()
    }

    @Test
    fun testReminderSectionIsNotVisibleWhenThereIsNoFutureDueDate() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertReminderSectionNotDisplayed()
    }

    @Test
    fun testReminderSectionIsVisibleWhenThereIsFutureDueDate() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.assertReminderSectionDisplayed()
    }

    @Test
    fun testAddReminder() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Hour Before")

        assignmentDetailsPage.assertReminderDisplayedWithText("1 Hour Before")
    }

    @Test
    fun testRemoveReminder() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Hour Before")

        assignmentDetailsPage.assertReminderDisplayedWithText("1 Hour Before")

        assignmentDetailsPage.removeReminderWithText("1 Hour Before")

        assignmentDetailsPage.assertReminderNotDisplayedWithText("1 Hour Before")
    }

    @Test
    fun testAddCustomReminder() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.clickCustom()
        assignmentDetailsPage.assertDoneButtonIsDisabled()
        assignmentDetailsPage.fillQuantity("15")
        assignmentDetailsPage.assertDoneButtonIsDisabled()
        assignmentDetailsPage.clickHoursBefore()
        assignmentDetailsPage.clickDone()

        assignmentDetailsPage.assertReminderDisplayedWithText("15 Hours Before")
    }

    @Test
    fun testAddReminderInPastShowsError() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 30)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Hour Before")

        checkToastText(R.string.reminderInPast, activityRule.activity)
    }

    @Test
    fun testAddReminderForTheSameTimeShowsError() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Hour Before")
        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Hour Before")

        checkToastText(R.string.reminderAlreadySet, activityRule.activity)
    }

    @Test
    fun testAddReminderForTheSameTimeWithDifferentMeasureOfTimeShowsError() {
        val data = setupData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment", dueAt = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 10)
        }.time.toApiString())
        gotoAssignment(data, assignment)

        assignmentDetailsPage.clickAddReminder()
        assignmentDetailsPage.selectTimeOption("1 Week Before")
        assignmentDetailsPage.clickAddReminder()

        assignmentDetailsPage.clickCustom()
        assignmentDetailsPage.fillQuantity("7")
        assignmentDetailsPage.clickDaysBefore()
        assignmentDetailsPage.clickDone()

        checkToastText(R.string.reminderAlreadySet, activityRule.activity)
    }

    private fun setupData(restrictQuantitativeData: Boolean = false): MockCanvas {
        val data = MockCanvas.init(
            parentCount = 1,
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

    private fun gotoAssignment(data: MockCanvas, assignment: Assignment) {
        val student = data.students.first()
        val observer = data.parents.first()
        val course = data.courses.values.first()

        tokenLogin(data.domain, data.tokenFor(observer)!!, observer)
        composeTestRule.waitForIdle()

        dashboardPage.clickAlerts()
        composeTestRule.waitForIdle()

        val alert = data.addObserverAlert(
            observer,
            student,
            course,
            AlertType.ASSIGNMENT_MISSING,
            AlertWorkflowState.UNREAD,
            Date(),
            "https://${data.domain}/courses/${course.id}/assignments/${assignment.id}",
            false
        )

        alertsPage.refresh()
        composeTestRule.waitForIdle()

        alertsPage.clickOnAlert(alert.title)
        composeTestRule.waitForIdle()
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