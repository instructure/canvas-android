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

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheckNames
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvas.espresso.mockCanvas.utils.Randomizer
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Tab
import com.instructure.espresso.page.getStringFromResource
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.Test

@HiltAndroidTest
class AnnouncementsListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToAnnouncementsListPage()
        announcementsListPage.assertPageObjects()
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = allOf(
            matchesCheckNames(`is`("SpeakableTextPresentViewCheck")),
            matchesViews(withId(R.id.announcementNameEditText))
        )

        super.enableAndConfigureAccessibilityChecks()
    }

    @Test
    fun assertHasAnnouncement() {
        val data = getToAnnouncementsListPage()
        val course = data.courses.values.first()
        val announcement =
            data.courseDiscussionTopicHeaders[course.id]!!.filter { th -> th.announcement }.first()

        announcementsListPage.assertHasAnnouncement(announcement)
    }

    @Test
    fun searchesAnnouncements() {
        val data = getToAnnouncementsListPage(announcementCount = 3)
        val course = data.courses.values.first()
        val announcements =
            data.courseDiscussionTopicHeaders[course.id]!!.filter { th -> th.announcement }
        val searchAnnouncement = announcements[2]

        announcementsListPage.assertAnnouncementCount(announcements.size + 1) // +1 to account for header
        announcementsListPage.searchable.clickOnSearchButton()
        announcementsListPage.searchable.typeToSearchBar(searchAnnouncement.title!!.take(searchAnnouncement.title!!.length / 2))
        announcementsListPage.assertSearchResultCount(1)
        announcementsListPage.assertHasAnnouncement(searchAnnouncement)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    fun createNewAnnouncementTest() {
        getToAnnouncementsListPage(announcementCount = 1)
        announcementsListPage.assertAnnouncementCount(2) // header + the one test announcement
        val announcementName = Randomizer.getLoremWords(4)

        announcementsListPage.createAnnouncement(announcementName, Randomizer.getLoremWords(12))
        announcementsListPage.assertHasAnnouncement(announcementName)
        announcementsListPage.assertAnnouncementCount(3) //header + the existing and the newly created one
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    fun createAndAbortNewAnnouncementTest() {
        val data = getToAnnouncementsListPage(announcementCount = 1)
        val course = data.courses.values.first()
        val announcement =
            data.courseDiscussionTopicHeaders[course.id]!!.filter { th -> th.announcement }.first()

        announcementsListPage.assertHasAnnouncement(announcement)
        announcementsListPage.assertAnnouncementCount(2) // header + the one test announcement
        announcementsListPage.clickOnCreateAnnouncementThenClose()
        announcementsListPage.verifyExitWithoutSavingDialog()
        announcementsListPage.acceptExitWithoutSaveDialog()
        announcementsListPage.assertHasAnnouncement(announcement)
        announcementsListPage.assertAnnouncementCount(2) // header + the one test announcement
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    fun createNewAnnouncementWithMissingDescriptionTest() {
        getToAnnouncementsListPage(announcementCount = 1)

        announcementsListPage.createAnnouncement(Randomizer.getLoremWords(4), "")
        announcementsListPage.assertOnNewAnnouncementPage()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    fun createNewAnnouncementWithMissingTitleTest() {
        getToAnnouncementsListPage(announcementCount = 1)

        announcementsListPage.createAnnouncement("", Randomizer.getLoremWords(12))
        announcementsListPage.assertHasAnnouncement(announcementsListPage.getStringFromResource(R.string.no_title))
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

        dashboardPage.openCourse(course)
        courseBrowserPage.openAnnouncementsTab()
        return data
    }
}
