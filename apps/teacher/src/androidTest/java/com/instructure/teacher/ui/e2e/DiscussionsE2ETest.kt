/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
class DiscussionsE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.E2E)
    fun testDiscussionE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1, discussions = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val discussion = data.discussionsList[0]

        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.openCourse(courseName = course.name)
        courseBrowserPage.waitForRender()

        //Check discussion is present, published and change title
        courseBrowserPage.openDiscussionsTab()
        discussionsListPage.assertHasDiscussion(discussion)
        discussionsListPage.clickDiscussion(discussion)
        discussionsDetatailsPage.openEdit()
        editDiscussionsDetailsPage.editTitle("New Discussion")
        editDiscussionsDetailsPage.clickSave()
        discussionsDetatailsPage.refresh()
        discussionsDetatailsPage.assertDiscussionTitle("New Discussion")
        discussionsDetatailsPage.assertDiscussionPublished()

        //Unpublish discussion
        discussionsDetatailsPage.openEdit()
        editDiscussionsDetailsPage.switchPublished()
        editDiscussionsDetailsPage.clickSave()
        discussionsDetatailsPage.refresh()
        discussionsDetatailsPage.assertDiscussionUnpublished()

        //Delete discussion
        discussionsDetatailsPage.openEdit()
        editDiscussionsDetailsPage.deleteDiscussion()
        discussionsListPage.refresh()
        discussionsListPage.assertNoDiscussion()
    }
}