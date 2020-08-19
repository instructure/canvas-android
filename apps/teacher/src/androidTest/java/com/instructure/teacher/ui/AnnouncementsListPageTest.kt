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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import org.junit.Test

class AnnouncementsListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToAnnouncementsListPage()
        announcementsListPage.assertPageObjects()
    }

    @Test
    fun assertHasAnnouncement() {
        val data = getToAnnouncementsListPage()
        val course = data.courses.values.first()
        val announcement = data.courseDiscussionTopicHeaders[course.id]!!.filter { th -> th.announcement }.first()
        announcementsListPage.assertHasAnnouncement(announcement)
    }

    // FIXME: This should probably just be part of the page objects
    @Test
    fun assertDisplaysFloatingActionButton() {
        getToAnnouncementsListPage()
//        val discussion = Data.getNextDiscussion()
//        announcementsListPage.assertHasAnnouncement(discussion)
    }

    @Test
    fun searchesAnnouncements() {
        val data = getToAnnouncementsListPage(announcementCount = 3)
        val course = data.courses.values.first()
        val announcements = data.courseDiscussionTopicHeaders[course.id]!!.filter {th -> th.announcement}
        val searchAnnouncement = announcements[2]
        announcementsListPage.assertAnnouncementCount(announcements.size + 1) // +1 to account for header
        announcementsListPage.openSearch()
        announcementsListPage.enterSearchQuery(searchAnnouncement.title!!.take(searchAnnouncement.title!!.length / 2))
        announcementsListPage.assertAnnouncementCount(2) // header + single search result
        announcementsListPage.assertHasAnnouncement(searchAnnouncement)
    }

    private fun getToAnnouncementsListPage(announcementCount: Int = 1): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val announcementsTab = Tab(position = 2, label = "Announcements", visibility = "public", tabId = Tab.ANNOUNCEMENTS_ID)
        data.courseTabs[course.id]!! += announcementsTab // TODO: MockCanvas.addTab()

        repeat(announcementCount) {
            data.addDiscussionTopicToCourse(
                    course = course,
                    user = teacher,
                    isAnnouncement = true
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAnnouncementsTab()
        return data
    }
}
