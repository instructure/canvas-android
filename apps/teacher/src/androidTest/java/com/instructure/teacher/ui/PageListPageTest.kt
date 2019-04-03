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
package com.instructure.teacher.ui

import com.instructure.dataseeding.model.PageApiModel
import com.instructure.espresso.ditto.Ditto
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedCoursePage
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class PageListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToPageListPage()
        pageListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun searchesPages() {
        val pages = getToPageListPage(pageCount = 3)
        val searchPage = pages[2]
        pageListPage.assertPageCount(pages.size)
        pageListPage.openSearch()
        pageListPage.enterSearchQuery(searchPage.title.take(searchPage.title.length / 2))
        pageListPage.assertPageCount(1)
        pageListPage.assertHasPage(searchPage)
    }

    private fun getToPageListPage(pageCount: Int = 1): List<PageApiModel> {
        val data = seedData(teachers = 1, favoriteCourses = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val pages = (0 until pageCount).map { seedCoursePage(course = course, teacher = teacher) }
        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openPagesTab()
        return pages
    }


}
