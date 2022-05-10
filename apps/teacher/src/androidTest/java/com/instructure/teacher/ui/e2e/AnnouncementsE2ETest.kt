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
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AnnouncementsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]


        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Assert ${course.name} course is displayed.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to it's Announcements Page. Assert that the ${announcement.title} announcement has been displayed.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAnnouncementsTab()
        announcementsListPage.assertHasAnnouncement(announcement)

        Log.d(STEP_TAG,"Edit ${announcement.title} announcement's name to 'Haha'. Save the modifications.")
        announcementsListPage.clickDiscussion(announcement)
        editAnnouncementPage.openEdit()
        editAnnouncementPage.editAnnouncementName("Haha")
        editAnnouncementPage.saveEditAnnouncement()

        Log.d(STEP_TAG,"Navigate back to the Announcements Page. Refresh the page and assert that the announcement name has been changed to 'Haha'.")
        Espresso.pressBack()
        announcementsListPage.refresh()
        announcementsListPage.assertHasAnnouncement("Haha")

        Log.d(STEP_TAG,"Delete the 'Haha' titled announcement.")
        announcementsListPage.clickDiscussion("Haha")
        editAnnouncementPage.openEdit()
        editAnnouncementPage.deleteAnnouncement()

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