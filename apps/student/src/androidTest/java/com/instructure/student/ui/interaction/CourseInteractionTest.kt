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
 */
package com.instructure.student.ui.interaction

import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addPageToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class CourseInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    var mainCourse: Course? = null

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.COURSE, TestCategory.INTERACTION, false)
    fun testCourse_linkFromCoursePageToPublicCoursePage() {
        // Link from a course page to another public course should open in the app

        val data = getToCourse(courseCount = 2, favoriteCourseCount = 2)
        val course1 = data.courses.values.first()
        val course2 = data.courses.values.last()

        // Construct a page that has a link to course2
        val course2Url = "https://mock-data.instructure.com/api/v1/courses/${course2.id}" // TODO: Less hard-coded?
        val course2LinkElementId = "testLinkElement"
        val course2Html = "<a id=\"$course2LinkElementId\" href=\"$course2Url\">course2</a>"

        // Add a page for our course
        val page = data.addPageToCourse(
                courseId = course1.id,
                pageId = 1,
                title = "My Awesome Page",
                body = course2Html,
                published = true
        )

        courseBrowserPage.selectPages()
        pageListPage.selectRegularPage(page)

        // Click the link inside the webview
        onWebView()
                .withElement(findElement(Locator.ID, course2LinkElementId))
                .perform(webClick())

        courseBrowserPage.assertTitleCorrect(course2)
    }

    // Leaving this as stubbed for now, because we do not yet have the ability to open
    // mocked URLs in a webview.  MBL-13451.
    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.COURSE, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testCourse_openFile() {
        // user should be able to open/preview course file

        val data = getToCourse(
                courseCount = 1,
                favoriteCourseCount = 1
        )

        val course = data.courses.values.first()

        val displayName = "FamousQuote.html"
        val fileId = data.addFileToCourse(
                courseId = course.id,
                displayName = displayName,
                fileContent = "<!DOCTYPE html><html><h1>Famous Quote</h1><body>Ask not what your country can do for you, ask what you can do for your country -- JFK</body></html>",
                contentType = "text/html")

        courseBrowserPage.selectFiles()

        // This is as far as we go for now.  Actually opening the file for preview/download
        // won't work because the associated url is mocked.
        //fileListPage.selectItem(displayName)
    }

    /** Utility method to create mocked data, sign student 0 in, and navigate to course 0. */
    private fun getToCourse(
            courseCount: Int = 2, // Need to link from one to the other
            favoriteCourseCount: Int = 1): MockCanvas {
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = courseCount,
                favoriteCourseCount = favoriteCourseCount)

        val course1 = data.courses.values.first()
        val pagesTab = Tab(position = 2, label = "Pages", visibility = "public", tabId = Tab.PAGES_ID)
        val filesTab = Tab(position = 3, label = "Files", visibility = "public", tabId = Tab.FILES_ID)
        data.courseTabs[course1.id]!! += pagesTab
        data.courseTabs[course1.id]!! += filesTab

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        mainCourse = data.courses.values.first()

        dashboardPage.selectCourse(mainCourse!!)
        return data
    }
}

