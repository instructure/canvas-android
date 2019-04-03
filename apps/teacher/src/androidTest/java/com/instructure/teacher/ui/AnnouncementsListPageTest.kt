/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui

import com.instructure.dataseeding.api.SeedApi
import com.instructure.espresso.ditto.Ditto
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AnnouncementsListPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToAnnouncementsListPage()
        announcementsListPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun assertHasAnnouncement() {
        val announcement = getToAnnouncementsListPage().announcementsList.first()
        announcementsListPage.assertHasAnnouncement(announcement)
    }

    // FIXME: This should probably just be part of the page objects
    @Test
    @Ditto
    fun assertDisplaysFloatingActionButton() {
        getToAnnouncementsListPage()
//        val discussion = Data.getNextDiscussion()
//        announcementsListPage.assertHasAnnouncement(discussion)
    }

    @Test
    @Ditto
    fun searchesAnnouncements() {
        val announcements = getToAnnouncementsListPage(announcementCount = 3).announcementsList
        val searchAnnouncement = announcements[2]
        announcementsListPage.assertAnnouncementCount(announcements.size + 1) // +1 to account for header
        announcementsListPage.openSearch()
        announcementsListPage.enterSearchQuery(searchAnnouncement.title.take(searchAnnouncement.title.length / 2))
        announcementsListPage.assertAnnouncementCount(2) // header + single search result
        announcementsListPage.assertHasAnnouncement(searchAnnouncement)
    }

    private fun getToAnnouncementsListPage(announcementCount: Int = 1): SeedApi.SeededDataApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, announcements = announcementCount)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAnnouncementsTab()
        return data
    }
}
