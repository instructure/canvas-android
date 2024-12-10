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
package com.instructure.student.ui.e2e.k5

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.withAncestor
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.lang.Thread.sleep
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@HiltAndroidTest
class ScheduleE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Rule
    @JvmField
    var globalTimeout: Timeout = Timeout.millis(1200000) // //TODO: workaround for that sometimes this test is running infinite time because of scrollToElement does not find an element.

    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.E2E, SecondaryFeatureCategory.SCHEDULE)
    fun scheduleE2ETest() {

        Log.d(PREPARATION_TAG,"Seeding data for K5 sub-account.")
        val data = seedDataForK5(teachers = 1, students = 1, courses = 4, homeroomCourses = 1, announcements = 3)

        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val homeroomCourse = data.coursesList[0]
        val homeroomAnnouncement = data.announcementsList[0]
        val nonHomeroomCourses = data.coursesList.filter { !it.homeroomCourse }

        //Note that all of the calendars are set to UTC timezone
        val yesterDayCalendar = getCustomDateCalendar(-1)
        val tomorrowCalendar = getCustomDateCalendar(1)
        val currentDateCalendar = getCustomDateCalendar(0)
        val twoWeeksBeforeCalendar = getCustomDateCalendar(-15)
        val twoWeeksAfterCalendar = getCustomDateCalendar(15)

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' MISSING assignment for ${nonHomeroomCourses[2].name} course.")
        val testMissingAssignment = AssignmentsApi.createAssignment(nonHomeroomCourses[2].id, teacher.token, dueAt = currentDateCalendar.time.toApiString(), gradingType = GradingType.LETTER_GRADE, pointsPossible = 100.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' Two weeks before end date assignment for ${nonHomeroomCourses[1].name} course.")
        val testTwoWeeksBeforeAssignment = AssignmentsApi.createAssignment(nonHomeroomCourses[1].id, teacher.token, dueAt = twoWeeksBeforeCalendar.time.toApiString(), gradingType = GradingType.PERCENT, pointsPossible = 100.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' Two weeks after end date assignment for ${nonHomeroomCourses[0].name} course.")
        val testTwoWeeksAfterAssignment = AssignmentsApi.createAssignment(nonHomeroomCourses[0].id, teacher.token, dueAt = twoWeeksAfterCalendar.time.toApiString(), gradingType = GradingType.POINTS, pointsPossible = 25.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to K5 Schedule Page and assert it is loaded.")
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.SCHEDULE)

        //Depends on how we handle Sunday, need to clarify with calendar team
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) {  schedulePage.assertIfCourseHeaderAndScheduleItemDisplayed(homeroomCourse.name, homeroomAnnouncement.title) }
        Log.d(STEP_TAG, "Assert that the current day of the calendar is titled as 'Today'.")
        schedulePage.assertDayHeaderShownByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) {
            Log.d(STEP_TAG, "Assert that the previous day of the calendar is titled as 'Yesterday'.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday))
        }
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7) {
            Log.d(STEP_TAG, "Assert that the next day of the calendar is titled as 'Tomorrow'.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow))
        }
        Log.d(STEP_TAG, "Assert that the ${nonHomeroomCourses[2].name} course and the ${testMissingAssignment.name} assignment are displayed.")
        schedulePage.assertIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[2].name, testMissingAssignment.name)

        Log.d(STEP_TAG, "Scroll to 'Missing Items' section  and verify that a missing assignment (${testMissingAssignment.name}) is displayed there with 100 points.")
        schedulePage.scrollToItem(R.id.metaLayout, testMissingAssignment.name)
        schedulePage.assertMissingItemDisplayedOnPlannerItem(testMissingAssignment.name, nonHomeroomCourses[2].name, "100 pts")

        Log.d(STEP_TAG, "Refresh the Schedule Page. Assert that the items are still displayed correctly.")
        schedulePage.scrollToPosition(0)
        schedulePage.refresh()
        sleep(3000)

        Log.d(STEP_TAG, "Assert that the current day of the calendar is titled as 'Today'.")
        schedulePage.assertDayHeaderShownByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) {
            Log.d(STEP_TAG, "Assert that the previous day of the calendar is titled as 'Yesterday'.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday)) }
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7) {
            Log.d(STEP_TAG, "Assert that the next day of the calendar is titled as 'Tomorrow'.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow)) }

        Log.d(STEP_TAG, "Swipe two weeks back from the current week, by clicking on the 'Previous Week' button and then by 'manual swiping'.")
        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksBeforeCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            val twoWeeksBeforeDayString = twoWeeksBeforeCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
            Log.d(STEP_TAG, "Assert that two weeks BEFORE current week," +
                    "the corresponding day header is shown and assert that the ${nonHomeroomCourses[1].name} course and the ${testTwoWeeksBeforeAssignment.name} assignment are displayed as well.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(twoWeeksBeforeCalendar), twoWeeksBeforeDayString, twoWeeksBeforeDayString)
            schedulePage.assertIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[1].name,testTwoWeeksBeforeAssignment.name)
        }

        Log.d(STEP_TAG, "Swipe from 2 weeks before current week to 2 weeks after current week.")
        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksAfterCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            val twoWeeksAfterDayString = twoWeeksAfterCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)

            Log.d(STEP_TAG, "Assert that two weeks AFTER current week," +
                    "the corresponding day header is shown and assert that the ${nonHomeroomCourses[0].name} course and the ${testTwoWeeksAfterAssignment.name} assignment are displayed as well.")
            schedulePage.assertDayHeaderShownByItemName(concatDayString(twoWeeksAfterCalendar), twoWeeksAfterDayString, twoWeeksAfterDayString)
            schedulePage.assertIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[0].name,testTwoWeeksAfterAssignment.name)

            Log.d(STEP_TAG, "Open course and verify if we are landing on the ${nonHomeroomCourses[0].name} course's details page by checking it's title, which is actually is the course's name.")
            schedulePage.clickCourseHeader(nonHomeroomCourses[0].name)
            elementaryCoursePage.assertPageObjects()
            elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)

            Log.d(STEP_TAG, "Navigate back to Schedule Page and assert it is loaded.")
            Espresso.pressBack()
            sleep(3000)
            schedulePage.assertPageObjects()
        }

        Log.d(STEP_TAG, "Swipe back to current week.")
        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team

            Log.d(STEP_TAG, "Assert that the ${nonHomeroomCourses[2].name} course and the ${testMissingAssignment.name} assignment are displayed.")
            schedulePage.assertIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[2].name, testMissingAssignment.name)

            Log.d(STEP_TAG, "Open '${testMissingAssignment.name}' assignment and verify if we are landing on the ${testMissingAssignment.name} assignment's details page by checking it's title, which is actually is the assignment's name.")
            schedulePage.clickScheduleItem(testMissingAssignment.name)
            assignmentDetailsPage.assertPageObjects()
            assignmentDetailsPage.assertAssignmentTitle(testMissingAssignment.name)

            Log.d(STEP_TAG, "Navigate back to Schedule Page and assert it is loaded.")
            Espresso.pressBack()
            schedulePage.assertPageObjects()

            Log.d(STEP_TAG, "Swipe two weeks after the current week.")
            schedulePage.nextWeekButtonClick()
            schedulePage.swipeLeft()
            Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
            schedulePage.assertPageObjects()

            Log.d(STEP_TAG, "Click on 'Marked as Done' checkbox of ${testTwoWeeksAfterAssignment.name} assignment. Assert if 'You've marked as done' string appears.")
            clickAndAssertMarkedAsDone(testTwoWeeksAfterAssignment.name)
        }
    }

    private fun clickAndAssertMarkedAsDone(assignmentName: String) {
        schedulePage.scrollToItem(R.id.title, assignmentName, schedulePage.withAncestor(R.id.plannerItems))
        schedulePage.assertMarkedAsDoneNotShown()
        schedulePage.clickDoneCheckbox()
        Thread.sleep(2000)
        schedulePage.assertMarkedAsDoneShown()
    }

    private fun concatDayString(calendar: Calendar): String {
        val dayOfMonthIntValue = calendar.get(Calendar.DAY_OF_MONTH)
        return if(dayOfMonthIntValue < 10) calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " 0" + calendar.get(Calendar.DAY_OF_MONTH).toString()
        else calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.DAY_OF_MONTH).toString()
    }

    private fun getCustomDateCalendar(dayDiffFromToday: Int): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.DATE, dayDiffFromToday)
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 1)
        cal.set(Calendar.SECOND, 1)
        return cal
    }

}

