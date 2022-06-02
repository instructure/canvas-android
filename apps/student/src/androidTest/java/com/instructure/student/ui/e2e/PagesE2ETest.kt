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
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.PagesApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PagesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.PAGES, TestCategory.E2E, false)
    fun testPagesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed some pages
        val pageUnpublished = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )

        val pagePublished = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token,
                body = "<h1 id=\"header1\">Regular Page Text</h1>"
        )

        val pagePublishedFront = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = true,
                token = teacher.token,
                body = "<h1 id=\"header1\">Front Page Text</h1>"
        )

        // Sign in our student
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Navigate to the Pages page of our course
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectPages()

        // Verify that our published pages show up, and unpublished ones do not
        pageListPage.assertFrontPageDisplayed(pagePublishedFront)
        pageListPage.assertRegularPageDisplayed(pagePublished)
        pageListPage.assertPageNotDisplayed(pageUnpublished)

        // Click into each page and verify the content of each
        pageListPage.selectFrontPage(pagePublishedFront)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "Front Page Text")
        )
        Espresso.pressBack()
        pageListPage.selectRegularPage(pagePublished)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "Regular Page Text")
        )

    }
}