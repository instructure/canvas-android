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
package com.instructure.student.ui.e2e

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
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedDataForK5
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class ScheduleE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.E2E)
    fun scheduleE2ETest() {

        // Seed data for K5 sub-account
        val data = seedDataForK5(
            teachers = 1,
            students = 1,
            courses = 4,
            homeroomCourses = 1,
            announcements = 3
        )
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val homeroomCourse = data.coursesList[0]
        val homeroomAnnouncement = data.announcementsList[0]
        val nonHomeroomCourses = data.coursesList.filter { !it.homeroomCourse }

        val yesterDayCalendar = getYesterdayDateCalendar("UTC")
        val tomorrowCalendar = getTomorrowDateCalendar("UTC")
        val currentDateCalendar = getCurrentDateCalendar("UTC")
        val twoWeeksBeforeCalendar = getCustomDateCalendar("UTC", -15)
        val twoWeeksAfterCalendar = getCustomDateCalendar("UTC", 15)

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

        // Sign in with elementary (K5) student
        tokenLoginElementary(student)
        elementaryDashboardPage.waitForRender()
        elementaryDashboardPage.selectScheduleTab()
        schedulePage.assertPageObjects()

        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            schedulePage.scrollToItem(R.id.scheduleCourseItemLayout, homeroomCourse.name)
            schedulePage.assertCourseHeaderDisplayed(homeroomCourse.name)
            schedulePage.scrollToItem(R.id.title,homeroomAnnouncement.title, schedulePage.withAncestor(R.id.plannerItems))
            schedulePage.assertScheduleItemDisplayed(homeroomAnnouncement.title)
        }
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1)
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday))
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7)
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow))

        schedulePage.scrollToItem(R.id.scheduleCourseItemLayout,nonHomeroomCourses[2].name)
        schedulePage.assertCourseHeaderDisplayed(nonHomeroomCourses[2].name)
        schedulePage.scrollToItem(R.id.title,testMissingAssignment.name, schedulePage.withAncestor(R.id.plannerItems))
        schedulePage.assertScheduleItemDisplayed(testMissingAssignment.name)

        schedulePage.scrollToItem(R.id.missingItemLayout,testMissingAssignment.name)
        schedulePage.assertMissingItemDisplayed(testMissingAssignment.name, nonHomeroomCourses[2].name, "100 pts")

        schedulePage.scrollToPosition(0)
        schedulePage.refresh()

        schedulePage.assertPageObjects()
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(currentDateCalendar), schedulePage.getStringFromResource(R.string.today), schedulePage.getStringFromResource(R.string.today))
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1)
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(yesterDayCalendar), schedulePage.getStringFromResource(R.string.yesterday), schedulePage.getStringFromResource(R.string.yesterday))
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 7)
        schedulePage.assertDayHeaderShownScrollToByItemName(concatDayString(tomorrowCalendar), schedulePage.getStringFromResource(R.string.tomorrow), schedulePage.getStringFromResource(R.string.tomorrow))

        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksBeforeCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            schedulePage.assertDayHeaderShownScrollToByItemName(
                concatDayString(twoWeeksBeforeCalendar),
                getDayString(twoWeeksBeforeCalendar.get(Calendar.DAY_OF_WEEK)),
                getDayString(twoWeeksBeforeCalendar.get(Calendar.DAY_OF_WEEK))
            )
            schedulePage.scrollToItem(R.id.scheduleCourseItemLayout,nonHomeroomCourses[1].name)
            schedulePage.assertCourseHeaderDisplayed(nonHomeroomCourses[1].name)
            schedulePage.scrollToItem(R.id.title,testTwoWeeksBeforeAssignment.name, schedulePage.withAncestor(R.id.plannerItems))
            schedulePage.assertScheduleItemDisplayed(testTwoWeeksBeforeAssignment.name)
        }

        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        schedulePage.nextWeekButtonClick()
        schedulePage.swipeLeft()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(twoWeeksAfterCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            schedulePage.assertDayHeaderShownScrollToByItemName(
                concatDayString(twoWeeksAfterCalendar),
                getDayString(twoWeeksAfterCalendar.get(Calendar.DAY_OF_WEEK)),
                getDayString(twoWeeksAfterCalendar.get(Calendar.DAY_OF_WEEK))
            )
            schedulePage.scrollToItem(R.id.scheduleCourseItemLayout,nonHomeroomCourses[0].name)
            schedulePage.assertCourseHeaderDisplayed(nonHomeroomCourses[0].name)
            schedulePage.scrollToItem(R.id.title,testTwoWeeksAfterAssignment.name, schedulePage.withAncestor(R.id.plannerItems))
            schedulePage.assertScheduleItemDisplayed(testTwoWeeksAfterAssignment.name)
            schedulePage.clickCourseHeader(nonHomeroomCourses[0].name)
            elementaryCoursePage.assertPageObjects()
            elementaryCoursePage.assertTitleCorrect(nonHomeroomCourses[0].name)
            Espresso.pressBack()
        }

        schedulePage.previousWeekButtonClick()
        schedulePage.swipeRight()
        Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
        if(currentDateCalendar.get(Calendar.DAY_OF_WEEK) != 1) { //Depends on how we handle Sunday, need to clarify with calendar team
            schedulePage.scrollToItem(R.id.scheduleCourseItemLayout, nonHomeroomCourses[2].name)
            schedulePage.assertCourseHeaderDisplayed(nonHomeroomCourses[2].name)
            schedulePage.scrollToItem(R.id.title,testMissingAssignment.name, schedulePage.withAncestor(R.id.plannerItems))
            schedulePage.assertScheduleItemDisplayed(testMissingAssignment.name)
            schedulePage.clickScheduleItem(testMissingAssignment.name)
            assignmentDetailsPage.assertPageObjects()
            assignmentDetailsPage.verifyAssignmentTitle(testMissingAssignment.name)
            Espresso.pressBack()
            schedulePage.assertPageObjects()
            schedulePage.nextWeekButtonClick()
            schedulePage.swipeLeft()
            Thread.sleep(5000) //This is mandatory here because after swiping back to "current week", the test would fail if we wouldn't wait enough for the page to be loaded.
            schedulePage.assertPageObjects()
            schedulePage.scrollToItem(R.id.title,testTwoWeeksAfterAssignment.name, schedulePage.withAncestor(R.id.plannerItems))
            schedulePage.assertMarkedAsDoneNotShown()
            schedulePage.clickDoneCheckbox()
            schedulePage.swipeDown()
            schedulePage.assertMarkedAsDoneShown()
        }
    }

    fun concatDayString(calendar: Calendar): String {
        val dayOfMonthIntValue = calendar.get(Calendar.DAY_OF_MONTH)
        return if(dayOfMonthIntValue < 10) getMonthString(calendar) + " 0" + calendar.get(Calendar.DAY_OF_MONTH).toString()
        else getMonthString(calendar) + " " + calendar.get(Calendar.DAY_OF_MONTH).toString()
    }

    fun getCurrentDateCalendar(timeZone: String): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone(timeZone))
    }

    fun getYesterdayDateCalendar(timeZone: String): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.add(Calendar.DATE, -1);
        return cal;
    }

    fun getTomorrowDateCalendar(timeZone: String): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.add(Calendar.DATE, 1);
        return cal;
    }

    fun getCustomDateCalendar(timeZone: String, dayDiffFromToday: Int): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
        cal.add(Calendar.DATE, dayDiffFromToday)
        return cal
    }

    fun getMonthString(calendar: Calendar): String {
        when (calendar.get(Calendar.MONTH)) {
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
            else -> {
                return ""
            }
        }
    }

    fun getDayString(calculatedDayIntValue: Int): String {
        when (calculatedDayIntValue) {
            1 -> return "Sunday"
            2 -> return "Monday"
            3 -> return "Tuesday"
            4 -> return "Wednesday"
            5 -> return "Thursday"
            6 -> return "Friday"
            7 -> return "Saturday"
            else -> {
                return ""
            }
        }
    }
}

