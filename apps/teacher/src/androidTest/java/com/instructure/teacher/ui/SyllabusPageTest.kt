/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class SyllabusPageTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    // Tests that we can open an assignment from the syllabus/summary, and does some verification of the calendar event.
    @Test
    fun testSyllabus_openAssignmentDetails() {
        // We have to add this delay to be sure that the remote config is already fetched before we want to override remote config values.
        Thread.sleep(3000)
        RemoteConfigPrefs.putString(RemoteConfigParam.SHOW_TEACHER_SYLLABUS.rc_name, "true")
        val data = goToSyllabus(eventCount = 0, assignmentCount = 1)

        val assignment = data.assignments.values.first()

        syllabusPage.selectSummaryTab()
        syllabusPage.assertItemDisplayed(assignment.name!!)
        syllabusPage.selectSummaryEvent(assignment.name!!)
        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    private fun goToSyllabus(eventCount: Int, assignmentCount: Int): MockCanvas {

        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()

        data.addCoursePermissions(course.id, CanvasContextPermission())

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
                courseId = course.id,
                date = 2.days.fromNow.iso8601,
                title = "Test Calendar Event",
                description = "Calendar event: $it"
            )
        }

        repeat(assignmentCount) {
            data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                dueAt = 2.days.fromNow.iso8601,
                name = "Assignment: $it"
            )
        }

        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openSyllabus()

        return data
    }
}