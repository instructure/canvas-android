/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.interaction

import android.os.Build
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCourseCalendarEvent
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addCourseSettings
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ActivityHelper
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class EditSyllabusInteractionTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    @Test
    fun testSaveEditedSyllabus() {
        goToEditSyllabus()

        editSyllabusPage.editSyllabusBody("Syllabus edited")
        editSyllabusPage.saveSyllabusEdit()

        syllabusPage.assertDisplaysSyllabus("Syllabus edited", true)
        if(Build.VERSION.SDK_INT != 30) syllabusPage.assertSuccessfulSave(ActivityHelper.currentActivity())
    }

    @Test
    fun testEditSyllabusAndDisableSummary() {
        goToEditSyllabus()

        editSyllabusPage.editSyllabusBody("Syllabus edited")
        editSyllabusPage.editSyllabusToggleShowSummary()
        editSyllabusPage.saveSyllabusEdit()

        syllabusPage.assertDisplaysSyllabus("Syllabus edited", false)
    }

    private fun goToEditSyllabus(): MockCanvas {

        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()

        data.addCoursePermissions(course.id, CanvasContextPermission(canManageContent = true))

        // Give the course a syllabus body
        val updatedCourse = course.copy(syllabusBody = "Syllabus Body")
        data.courses[course.id] = updatedCourse

        // Give the course a syllabus tab
        val syllabusTab = Tab(position = 2, label = "Syllabus", visibility = "public", tabId = Tab.SYLLABUS_ID)
        data.courseTabs[course.id]!! += syllabusTab

        // Enable the courseSummary setting to get a course summary
        data.addCourseSettings(course.id, CourseSettings(courseSummary = true))

        data.addCourseCalendarEvent(
            course = course,
            startDate = 2.days.fromNow.iso8601,
            title = "Test Calendar Event",
            description = "Calendar event: 1"
        )

        data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            dueAt = 2.days.fromNow.iso8601,
            name = "Assignment: 1"
        )

        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openSyllabus()
        syllabusPage.openEditSyllabus()

        return data
    }
}