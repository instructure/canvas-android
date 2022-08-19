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
 *
 */
package com.instructure.student.ui.e2e.k5

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.withAncestor
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.pages.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class ScheduleE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun scheduleE2ETest() {

        // Seed data for K5 sub-account
        val data = seedDataForK5(teachers = 1, students = 1, courses = 4, homeroomCourses = 1, announcements = 3)

        //Extract data from seeded data
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

        val testMissingAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[2].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.LETTER_GRADE,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = currentDateCalendar.time.toApiString()
            )
        )

        val testTwoWeeksBeforeAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[1].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.PERCENT,
                teacherToken = teacher.token,
                pointsPossible = 100.0,
                dueAt = twoWeeksBeforeCalendar.time.toApiString()
            )
        )

        val testTwoWeeksAfterAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = nonHomeroomCourses[0].id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 25.0,
                dueAt = twoWeeksAfterCalendar.time.toApiString()
            )
        )

        // Sign in with elementary (K5) student and navigate to Schedule tab
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.SCHEDULE)
        schedulePage.assertPageObjects()

        //Depends on how we handle Sunday, need to clarify with calendar team
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) {  schedulePage.verifyIfCourseHeaderAndScheduleItemDisplayed(homeroomCourse.name,homeroomAnnouncement.title) }
        schedulePage.assertDayHeaderShownByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { schedulePage.assertDayHeaderShownByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday))}
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7) { schedulePage.assertDayHeaderShownByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow))}
        schedulePage.verifyIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[2].name,testMissingAssignment.name)

        //Scroll to missing item's section and verify that a missing assignment is appearing there
        schedulePage.scrollToItem(R.id.missingItemLayout,testMissingAssignment.name)
        schedulePage.assertMissingItemDisplayed(testMissingAssignment.name, nonHomeroomCourses[2].name, "100 pts")

        //Refresh the page and assert that it's items are still displayed
        schedulePage.scrollToPosition(0)
        schedulePage.refresh()
        schedulePage.assertPageObjects()
        schedulePage.assertDayHeaderShownByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { schedulePage.assertDayHeaderShownByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday)) }
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7) { schedulePage.assertDayHeaderShownByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow)) }

        //Swipe to 2 week befeore current week
        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksBeforeCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            val twoWeeksBeforeDayString = twoWeeksBeforeCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
            schedulePage.assertDayHeaderShownByItemName(concatDayString(twoWeeksBeforeCalendar), twoWeeksBeforeDayString, twoWeeksBeforeDayString)
            schedulePage.verifyIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[1].name,testTwoWeeksBeforeAssignment.name)
        }

        //Swipe from 2 weeks before current week to 2 weeks after current week
        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksAfterCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            val twoWeeksAfterDayString = twoWeeksAfterCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
            schedulePage.assertDayHeaderShownByItemName(concatDayString(twoWeeksAfterCalendar), twoWeeksAfterDayString, twoWeeksAfterDayString)

            schedulePage.verifyIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[0].name,testTwoWeeksAfterAssignment.name)

            //Open course and verify if we are landing on the course details page by checking it's title
            schedulePage.clickCourseHeader(nonHomeroomCourses[0].name)
            elementaryCoursePage.assertPageObjects()
            elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)
            Espresso.pressBack()
        }

        //Swipe back to current week
        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team

            schedulePage.verifyIfCourseHeaderAndScheduleItemDisplayed(nonHomeroomCourses[2].name, testMissingAssignment.name)

            //Open assignment and verify if we are landing on the assignment details page by checking it's title
            schedulePage.clickScheduleItem(testMissingAssignment.name)
            assignmentDetailsPage.assertPageObjects()
            assignmentDetailsPage.verifyAssignmentTitle(testMissingAssignment.name)
            Espresso.pressBack()
            schedulePage.assertPageObjects()

            //Swipe to 2 weeks after current week
            schedulePage.nextWeekButtonClick()
            schedulePage.swipeLeft()
            Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
            schedulePage.assertPageObjects()

            //Click on 'Marked as Done' checkbox and assert if 'You've marked as done' string appears
            clickAndAssertMarkedAsDone(testTwoWeeksAfterAssignment.name)
        }
    }



    private fun clickAndAssertMarkedAsDone(assignmentName: String) {
        schedulePage.scrollToItem(R.id.title, assignmentName, schedulePage.withAncestor(R.id.plannerItems))
        schedulePage.assertMarkedAsDoneNotShown()
        schedulePage.clickDoneCheckbox()
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

