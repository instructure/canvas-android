/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.parentapp.R
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Calendar

@HiltAndroidTest
class AssignmentReminderE2ETest: ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.ASSIGNMENT_REMINDER)
    fun testAssignmentCustomReminderE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]
        val futureDueDate = 2.days.fromNow
        val pastDueDate = 2.days.ago

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days past due date.")
        val alreadyPastAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = pastDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the '${course.name}' course and assert that the details of the course has opened.")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG,"Click on assignment '${testAssignment.name}'.")
        courseDetailsPage.clickAssignment(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Assignment Details' as the user is on the assignment details page and the subtitle is the '${course.name}' course's name.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)
        assignmentDetailsPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the reminder section is displayed as well.")
        reminderPage.assertReminderSectionDisplayed()

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneHour = futureDueDate.apply { add(Calendar.HOUR, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneHour)
        reminderPage.selectTime(reminderDateOneHour)
        reminderPage.assertReminderDisplayedWithText(reminderDateOneHour.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' again, and assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice.")
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneHour)
        reminderPage.selectTime(reminderDateOneHour)
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        Log.d(STEP_TAG, "Remove the '1 Hour Before' reminder, confirm the deletion dialog and assert that the '1 Hour Before' reminder is not displayed any more.")
        reminderPage.removeReminderWithText(reminderDateOneHour.time.toFormattedString())
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneHour.time.toFormattedString())
        futureDueDate.apply { add(Calendar.HOUR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Week Before' and assert that a toast message is occurring which warns that we cannot pick up a reminder which has already passed (for example cannot pick '1 Week Before' reminder for an assignment which ends tomorrow).")
        val reminderDateOneWeek = futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneWeek)
        reminderPage.selectTime(reminderDateOneWeek)
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneWeek.time.toFormattedString())
        checkToastText(R.string.reminderInPast, activityRule.activity)
        futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Day Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneDay = futureDueDate.apply { add(Calendar.DAY_OF_MONTH, -1) }
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneDay)
        reminderPage.selectTime(reminderDateOneDay)
        reminderPage.assertReminderDisplayedWithText(reminderDateOneDay.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Day Before' again, and assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice.")
        reminderPage.clickCustomReminderOption()
        reminderPage.selectDate(reminderDateOneDay)
        reminderPage.selectTime(reminderDateOneDay)

        Log.d(ASSERTION_TAG, "Assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice. (Because 1 days and 24 hours is the same)")
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        futureDueDate.apply { add(Calendar.DAY_OF_MONTH, 1) }

        Log.d(STEP_TAG, "Navigate back to Assignment List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on assignment '${alreadyPastAssignment.name}'.")
        courseDetailsPage.clickAssignment(alreadyPastAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the reminder section is NOT displayed, because the '${alreadyPastAssignment.name}' assignment has already passed..")
        reminderPage.assertReminderSectionDisplayed()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.ASSIGNMENT_REMINDER)
    fun testAssignmentBeforeReminderE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]
        val futureDueDate = 2.days.fromNow
        val pastDueDate = 2.days.ago

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days ahead due date.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = futureDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days past due date.")
        val alreadyPastAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = pastDueDate.iso8601, pointsPossible = 15.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the '${course.name}' course and assert that the details of the course has opened.")
        coursesPage.clickCourseItem(course.name)
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(STEP_TAG,"Click on assignment '${testAssignment.name}'.")
        courseDetailsPage.clickAssignment(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Assignment Details' as the user is on the assignment details page and the subtitle is the '${course.name}' course's name.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)
        assignmentDetailsPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the reminder section is displayed as well.")
        reminderPage.assertReminderSectionDisplayed()

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneHour = futureDueDate.apply { add(Calendar.HOUR, -1) }
        reminderPage.clickBeforeReminderOption("1 Hour Before")
        reminderPage.assertReminderDisplayedWithText(reminderDateOneHour.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Hour Before' again, and assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice.")
        reminderPage.clickBeforeReminderOption("1 Hour Before")
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)

        Log.d(STEP_TAG, "Remove the '1 Hour Before' reminder, confirm the deletion dialog and assert that the '1 Hour Before' reminder is not displayed any more.")
        reminderPage.removeReminderWithText(reminderDateOneHour.time.toFormattedString())
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneHour.time.toFormattedString())
        futureDueDate.apply { add(Calendar.HOUR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Week Before' and assert that a toast message is occurring which warns that we cannot pick up a reminder which has already passed (for example cannot pick '1 Week Before' reminder for an assignment which ends tomorrow).")
        val reminderDateOneWeek = futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, -1) }
        reminderPage.clickBeforeReminderOption("1 Week Before")
        reminderPage.assertReminderNotDisplayedWithText(reminderDateOneWeek.time.toFormattedString())
        composeTestRule.waitForIdle()
        checkToastText(R.string.reminderInPast, activityRule.activity)
        composeTestRule.waitForIdle()
        futureDueDate.apply { add(Calendar.WEEK_OF_YEAR, 1) }

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        Log.d(STEP_TAG, "Select '1 Day Before' and assert that the reminder has been picked up and displayed on the Assignment Details Page.")
        val reminderDateOneDay = futureDueDate.apply { add(Calendar.DAY_OF_MONTH, -1) }
        reminderPage.clickBeforeReminderOption("1 Day Before")
        reminderPage.assertReminderDisplayedWithText(reminderDateOneDay.time.toFormattedString())

        Log.d(STEP_TAG, "Click on the '+' button (Add reminder) to pick up a new reminder.")
        reminderPage.clickAddReminder()

        reminderPage.clickBeforeReminderOption("1 Day Before")

        Log.d(ASSERTION_TAG, "Assert that a toast message is occurring which warns that we cannot pick up the same time reminder twice. (Because 1 days and 24 hours is the same)")
        composeTestRule.waitForIdle()
        checkToastText(R.string.reminderAlreadySet, activityRule.activity)
        composeTestRule.waitForIdle()

        futureDueDate.apply { add(Calendar.DAY_OF_MONTH, 1) }

        Log.d(STEP_TAG, "Navigate back to Assignment List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on assignment '${alreadyPastAssignment.name}'.")
        courseDetailsPage.clickAssignment(alreadyPastAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the reminder section is NOT displayed, because the '${alreadyPastAssignment.name}' assignment has already passed..")
        reminderPage.assertReminderSectionDisplayed()
    }
}