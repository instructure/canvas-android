/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockCanvas.addCourseSettings
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class SyllabusInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // Tests that we can display a calendar event from the syllabus/summary,
    // and does some verification of the calendar event.
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.INTERACTION)
    fun testSyllabus_calendarEvent() {
        val data = goToSyllabus(eventCount = 1, assignmentCount = 0)

        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)
        val event = data.courseCalendarEvents[course.id]!!.first()

        syllabusPage.selectSummaryTab()
        syllabusPage.assertItemDisplayed(event.title!!)
        syllabusPage.selectSummaryEvent(event.title!!)
        calendarEventDetailsPage.assertEventTitle(event.title!!)
        calendarEventDetailsPage.verifyDescription(event.description!!)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.INTERACTION)
    fun testSyllabus_assignment() {
        val data = goToSyllabus(eventCount = 0, assignmentCount = 1)

        val course = data.courses.values.first()
        data.coursePermissions[course.id] = CanvasContextPermission(manageCalendar = true)
        val assignment = data.assignments.entries.firstOrNull()!!.value

        syllabusPage.selectSummaryTab()
        syllabusPage.assertItemDisplayed(assignment.name!!)
        syllabusPage.selectSummaryEvent(assignment.name!!)
        assignmentDetailsPage.assertAssignmentTitle(assignment.name!!)
    }

    private fun goToSyllabus(eventCount: Int, assignmentCount: Int) : MockCanvas {

        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)

        val course = data.courses.values.first()
        val student = data.students[0]

        // Give the course a syllabus body
        val updatedCourse = course.copy(syllabusBody = "Syllabus Body")
        data.courses[course.id] = updatedCourse

        // Give the course a syllabus tab
        val syllabusTab = Tab(position = 2, label = "Syllabus", visibility = "public", tabId = Tab.SYLLABUS_ID)
        data.courseTabs[course.id]!! += syllabusTab

        // Enable the courseSummary setting to get a course summary
        data.addCourseSettings(course.id, CourseSettings(courseSummary = true))

        repeat(eventCount) {
            data.addCourseCalendarEvent(
                    course = course,
                    startDate = 2.days.fromNow.iso8601,
                    title = "Test Calendar Event",
                    description = "The calendar event to end all calendar events"
            )
        }

        repeat(assignmentCount) {
            data.addAssignment(
                    courseId = course.id,
                    submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                    dueAt = 2.days.fromNow.iso8601
            )
        }

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.selectCourse(course)
        courseBrowserPage.selectSyllabus()

        return data
    }
}