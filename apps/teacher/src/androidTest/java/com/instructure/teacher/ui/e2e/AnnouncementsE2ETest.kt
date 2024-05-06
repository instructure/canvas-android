/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test


@HiltAndroidTest
class AnnouncementsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    //Because of naming conventions, we are using 'announcementDetailsPage' naming in this class to make the code more readable and straightforward.
    private val announcementDetailsPage = discussionsDetailsPage

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 2)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]
        val announcement2 = data.announcementsList[1]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Assert ${course.name} course is displayed.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to it's Announcements Page. Assert that the ${announcement.title} announcement has been displayed.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAnnouncementsTab()
        announcementsListPage.assertHasAnnouncement(announcement)

        Log.d(STEP_TAG, "Click on 'Search' (magnifying glass) icon and type '${announcement2.title}', one of the announcements' name to the search input field.")
        announcementsListPage.searchable.clickOnSearchButton()
        announcementsListPage.searchable.typeToSearchBar(announcement2.title)

        Log.d(STEP_TAG, "Assert that only 1 announcement matches for the search text, and it is '${announcement2.title}'.")
        announcementsListPage.assertSearchResultCount(1)
        announcementsListPage.assertHasAnnouncement(announcement2)

        Log.d(STEP_TAG, "Click on 'Reset' search (cross) icon and assert that all the announcements are displayed (2).")
        announcementsListPage.searchable.clickOnClearSearchButton()
        announcementsListPage.assertSearchResultCount(2)

        Log.d(STEP_TAG,"Edit ${announcement.title} announcement's name to 'Haha'. Save the modifications.")
        announcementsListPage.clickAnnouncement(announcement)
        announcementDetailsPage.openEdit()
        editAnnouncementDetailsPage.editAnnouncementTitle("Haha")
        editAnnouncementDetailsPage.saveAnnouncement()

        Log.d(STEP_TAG,"Navigate back to the Announcements Page. Refresh the page and assert that the announcement name has been changed to 'Haha'.")
        Espresso.pressBack()
        announcementsListPage.refresh()
        announcementsListPage.assertHasAnnouncement("Haha")

        Log.d(STEP_TAG,"Delete the 'Haha' titled announcement.")
        announcementsListPage.clickAnnouncement("Haha")
        announcementDetailsPage.openEdit()
        editAnnouncementDetailsPage.deleteAnnouncement()

        Log.d(STEP_TAG, "")
        announcementsListPage.clickAnnouncement(announcement2.title)
        announcementDetailsPage.openEdit()
        editAnnouncementDetailsPage.deleteAnnouncement()

        Log.d(STEP_TAG,"Refresh the Announcements Page and assert that there is no announcement displayed. Assert that empty view is displayed.")
        announcementsListPage.refresh()
        announcementsListPage.assertEmpty()

        Log.d(STEP_TAG,"Create a new valid announcement.")
        announcementsListPage.createAnnouncement(announcementName = "I am an announcement", announcementDetails = "I am the detail")

        Log.d(STEP_TAG,"Refresh the Announcements Page and assert that the previously created announcement is displayed.")
        announcementsListPage.refresh()
        announcementsListPage.assertHasAnnouncement("I am an announcement")
    }
}