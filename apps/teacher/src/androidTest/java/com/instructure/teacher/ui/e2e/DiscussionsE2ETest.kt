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
class DiscussionsE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E)
    fun testDiscussionE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, discussions = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val discussion = data.discussionsList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.waitForRender()

        Log.d(STEP_TAG,"Open Discussions Page and assert has discussion: ${discussion.title}.")
        courseBrowserPage.openDiscussionsTab()
        discussionsListPage.assertHasDiscussion(discussion)

        Log.d(STEP_TAG,"Click on '${discussion.title}' discussion and navigate to Discussions Details Page by clicking on 'Edit'.")
        discussionsListPage.clickDiscussion(discussion)
        discussionsDetailsPage.openEdit()

        val newTitle = "New Discussion"
        Log.d(STEP_TAG,"Edit the discussion's title to: '$newTitle'. Click on 'Save'.")
        editDiscussionsDetailsPage.editTitle(newTitle)
        editDiscussionsDetailsPage.clickSave()

        Log.d(STEP_TAG,"Refresh the page. Assert that the discussion's name has been changed to '$newTitle' and it is published.")
        discussionsDetailsPage.refresh()
        discussionsDetailsPage.assertDiscussionTitle(newTitle)
        discussionsDetailsPage.assertDiscussionPublished()

        Log.d(STEP_TAG,"Navigate to Discussions Details Page by clicking on 'Edit'. Unpublish the '$newTitle' discussion and click on 'Save'.")
        discussionsDetailsPage.openEdit()
        editDiscussionsDetailsPage.togglePublished()
        editDiscussionsDetailsPage.clickSave()

        Log.d(STEP_TAG,"Refresh the page. Assert that the '$newTitle' discussion has been unpublished.")
        discussionsDetailsPage.refresh()
        discussionsDetailsPage.assertDiscussionUnpublished()

        Log.d(STEP_TAG,"Navigate to Discussions Details Page by clicking on 'Edit'. Delete the '$newTitle' discussion.")
        discussionsDetailsPage.openEdit()
        editDiscussionsDetailsPage.deleteDiscussion()

        Log.d(STEP_TAG,"Refresh the page. Assert that there is no discussion, so the '$newTitle' discussion has been deleted successfully.")
        discussionsListPage.refresh()
        discussionsListPage.assertNoDiscussion()

        Log.d(STEP_TAG,"Click on '+' icon on the UI to create a new discussion.")
        discussionsListPage.createNewDiscussion()

        val newDiscussionTitle = "Test Discussion Mobile UI"
        Log.d(STEP_TAG,"Set '$newDiscussionTitle' as the discussion's title and set some description as well.")
        editDiscussionsDetailsPage.editTitle(newDiscussionTitle)
        editDiscussionsDetailsPage.editDescription("Mobile UI Discussion description")

        Log.d(STEP_TAG,"Toggle Publish checkbox and save the page.")
        editDiscussionsDetailsPage.togglePublished()
        editDiscussionsDetailsPage.clickSendNewDiscussion()

        Log.d(STEP_TAG,"Assert that '$newDiscussionTitle' discussion is displayed and published.")
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)
        discussionsListPage.clickDiscussion(newDiscussionTitle)
        discussionsDetailsPage.assertDiscussionPublished()
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on the Search icon and type some search query string which matches only with the previously created discussion's title.")
        discussionsListPage.openSearch()
        discussionsListPage.enterSearchQuery("Test Discussion")

        Log.d(STEP_TAG,"Assert that the '$newDiscussionTitle' discussion is displayed and it is the only one.")
        discussionsListPage.assertDiscussionCount(2) // header + single search result
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)
        Espresso.pressBack() // need to press back to exit from the search input field

        Log.d(STEP_TAG,"Collapse the discussion list and assert that the '$newDiscussionTitle' discussion can NOT be seen.")
        discussionsListPage.toggleCollapseExpandIcon()
        discussionsListPage.assertDiscussionCount(1) // header only
        discussionsListPage.assertDiscussionDoesNotExist(newDiscussionTitle)

        Log.d(STEP_TAG,"Expand the discussion list and assert that the '$newDiscussionTitle' discussion can be seen.")
        discussionsListPage.toggleCollapseExpandIcon()
        discussionsListPage.assertDiscussionCount(2) // header only + single search result
        discussionsListPage.assertHasDiscussion(newDiscussionTitle)

    }
}