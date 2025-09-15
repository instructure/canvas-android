/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Tab
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class CourseDetailsInteractionTest : ParentComposeTest() {

    @Test
    fun courseDetailsDisplayed() {
        val data = initData()
        val course = data.courses.values.first()
        setupTabs(data, course)

        goToCourseDetails(data, course.name)

        composeTestRule.waitForIdle()
        courseDetailsPage.assertCourseDetailsDisplayed(course)
    }

    @Test
    fun changeTab() {
        val data = initData()
        val course = data.courses.values.first()
        setupTabs(data, course)

        goToCourseDetails(data, course.name)

        composeTestRule.waitForIdle()
        courseDetailsPage.selectTab("SYLLABUS")
        courseDetailsPage.assertTabSelected("SYLLABUS")
    }

    private fun initData(): MockCanvas {
        return MockCanvas.init(
            parentCount = 1,
            studentCount = 1,
            courseCount = 1
        )
    }

    private fun setupTabs(data: MockCanvas, course: Course) {
        course.homePage = Course.HomePage.HOME_SYLLABUS
        course.syllabusBody = "This is the syllabus"
        data.courseTabs[course.id]?.add(Tab(tabId = Tab.SYLLABUS_ID))
        data.courseSettings[course.id] = CourseSettings(
            courseSummary = true
        )
    }

    private fun goToCourseDetails(data: MockCanvas, courseName: String) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)
        coursesPage.clickCourseItem(courseName)
    }
}
