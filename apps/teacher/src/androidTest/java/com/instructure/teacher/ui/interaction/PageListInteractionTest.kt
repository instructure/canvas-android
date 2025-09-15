/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addPageToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PageListInteractionTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToPageListPage()
        pageListPage.assertPageObjects()
    }

    @Test
    fun searchesPages() {
        val pages = getToPageListPage(pageCount = 3)
        val searchPage = pages[2]
        pageListPage.assertPageCount(pages.size)
        pageListPage.searchable.clickOnSearchButton()
        pageListPage.searchable.typeToSearchBar(searchPage.title!!.take(searchPage.title!!.length / 2))
        pageListPage.assertPageCount(1)
        pageListPage.assertHasPage(searchPage)
    }

    private fun getToPageListPage(pageCount: Int = 1): List<Page> {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val pagesTab = Tab(position = 2, label = "Pages", visibility = "public", tabId = Tab.PAGES_ID)
        data.courseTabs[course.id]!! += pagesTab // TODO: MockCanvas.addTab()

        val pages = mutableListOf<Page>()
        repeat(pageCount) {
            pages += data.addPageToCourse(
                    courseId = course.id,
                    pageId = data.newItemId(),
                    published = true
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openPagesTab()
        return pages
    }


}
