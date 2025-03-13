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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CalendarEventApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.UpdateCourse
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Date

@HiltAndroidTest
class CourseDetailsSummaryE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE_DETAILS, TestCategory.E2E, SecondaryFeatureCategory.SUMMARY)
    fun testCourseDetailsSummaryE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]

        Log.d(PREPARATION_TAG, "Seed assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(
            course.id, teacher.token, submissionTypes = listOf(
                SubmissionType.ON_PAPER
            ), withDescription = true, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601
        )

        Log.d(PREPARATION_TAG, "Seed a calendar event because it should be displayed on the Summary Page.")
        val testCalendarEvent = CalendarEventApi.createCalendarEvent(
            teacher.token,
            CanvasContext.makeContextId(CanvasContext.Type.COURSE, course.id),
            "Test Calendar Event",
            Date().toApiString()
        )

        Log.d(PREPARATION_TAG,"Seed a published quiz because it should be displayed on the Summary Page.")
        val testPublishedQuiz = QuizzesApi.createAndPublishQuiz(course.id, teacher.token, listOf())

        Log.d(PREPARATION_TAG, "Update ${course.name} course to set Syllabus as Home Page (with some syllabus body) and enable 'Show Course Summary' setting to make the Summary Tab displayed in the Parent app.")
        CoursesApi.updateCourse(course.id, UpdateCourse(syllabusBody = "Test syllabus body...", homePage = "syllabus", showSummary = 1))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${course.name}' course.")
        dashboardPage.waitForRender()
        coursesPage.clickCourseItem(course.name)

        Log.d(STEP_TAG,"Navigate to Summary Page by selecting Summary Tab.")
        courseDetailsPage.selectTab("SUMMARY")

        Log.d(ASSERTION_TAG, "Assert that the 'SUMMARY' tab has been selected.")
        courseDetailsPage.assertTabSelected("SUMMARY")

        Log.d(STEP_TAG, "Assert that the' ${assignment.name}' assignment, '${testPublishedQuiz.title}' quiz and '${testCalendarEvent.title}' calendar event items are all displayed on the Summary Page. ")
        summaryPage.assertItemDisplayed(assignment.name)
        summaryPage.assertItemDisplayed(testCalendarEvent.title.orEmpty())
        summaryPage.assertItemDisplayed(testPublishedQuiz.title)

        Log.d(STEP_TAG, "Select '${assignment.name}' (assignment) Summary item to navigate to the Assignment Details Page.")
        summaryPage.selectItem(assignment.name)

        Log.d(STEP_TAG, "Assert that the Assignment Details Page is loaded successfully.")
        assignmentDetailsPage.assertAssignmentDetails(assignment)

        Log.d(STEP_TAG, "Navigate back to the Summary Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${testCalendarEvent.title}' Summary item (calendar event) to navigate to the Calendar Event Details Page.")
        summaryPage.selectItem(testCalendarEvent.title.orEmpty())

        Log.d(STEP_TAG, "Assert that the Calendar Event Details Page is loaded successfully.")
        calendarEventDetailsPage.assertEventTitle(testCalendarEvent.title.orEmpty())
    }

}