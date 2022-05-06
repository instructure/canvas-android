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

private const val STEP_TAG = "AnnouncementsE2ETest #STEP# "
private const val PREPARATION_TAG = "AnnouncementsE2ETest #PREPARATION# "

@HiltAndroidTest
class AnnouncementsE2ETest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]

        Log.d(STEP_TAG,"Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Opens ${course.name} course and navigate to the Announcements Page. Assert if the seeded announcement has been displayed there.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openAnnouncementsTab()
        announcementsListPage.assertHasAnnouncement(announcement)

        Log.d(STEP_TAG,"Click on the announcement, open 'Edit', and try to edit the announcement's name, and click on Save.")
        announcementsListPage.clickDiscussion(announcement)
        editAnnouncementPage.openEdit()
        editAnnouncementPage.editAnnouncementName("Haha")
        editAnnouncementPage.saveEditAnnouncement()

        Log.d(STEP_TAG,"Navigate back the Announcement List Page and refresh the page. Assert that the announcement's name has been changed there as well.")
        Espresso.pressBack()
        announcementsListPage.refresh()
        announcementsListPage.assertHasAnnouncement("Haha")

        Log.d(STEP_TAG,"Delete the previously created (and then renamed) announcement.")
        announcementsListPage.clickDiscussion("Haha")
        editAnnouncementPage.openEdit()
        editAnnouncementPage.deleteAnnouncement()

        Log.d(STEP_TAG,"Assert that the announcement has been deleted, so that the empty view has been displayed.")
        announcementsListPage.refresh()
        announcementsListPage.assertEmpty()

        Log.d(STEP_TAG,"Create a 'valid' announcement and assert that it has been displayed on the Discussion List Page after creation.")
        announcementsListPage.createAnnouncement("Announcement Topic", "Awesome announcement topic")
        announcementsListPage.assertHasAnnouncement("Announcement Topic")

        Log.d(STEP_TAG,"Create an 'invalid' announcement with missing title. Assert that an announcement will be created with 'No Title' title.")
        announcementsListPage.createAnnouncement("", "Missing title announcement")
        announcementsListPage.assertHasAnnouncement("No Title")

        Log.d(STEP_TAG,"Create an 'invalid' announcement with missing description. Assert that the announcement cannot be created without a description and the user remains on the New Announcement Page.")
        announcementsListPage.createAnnouncement("Missing description announcement", "")
        announcementsListPage.assertOnNewAnnouncementPage()

    }
}