/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import com.instructure.canvas.espresso.common.interaction.SmartSearchInteractionTest
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.student.BuildConfig
import com.instructure.student.activity.LoginActivity
import com.instructure.student.ui.pages.DashboardPage
import com.instructure.student.ui.pages.DiscussionDetailsPage
import com.instructure.student.ui.pages.PageDetailsPage
import com.instructure.student.ui.utils.StudentActivityTestRule
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class StudentSmartSearchInteractionTest : SmartSearchInteractionTest() {

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = StudentActivityTestRule(LoginActivity::class.java)

    private val dashboardPage = DashboardPage()
    private val discussionDetailsPage = DiscussionDetailsPage(ModuleItemInteractions())
    private val pageDetailsPage = PageDetailsPage(ModuleItemInteractions())

    @Test
    fun dummyTest() {
        // This is a dummy test to prevent NoTestsRemainException
    }

    override fun initData(): MockCanvas {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )

        val course = data.courses.values.first()
        data.courseTabs[course.id]?.add(
            Tab(position = 3, label = "Smart Search", visibility = "public", tabId = Tab.SEARCH_ID)
        )
        data.courseTabs[course.id]?.add(
            Tab(position = 4, label = "Announcements", visibility = "public", tabId = Tab.ANNOUNCEMENTS_ID)
        )
        data.courseTabs[course.id]?.add(
            Tab(position = 5, label = "Discussions", visibility = "public", tabId = Tab.DISCUSSIONS_ID)
        )
        data.courseTabs[course.id]?.add(
            Tab(position = 6, label = "Pages", visibility = "public", tabId = Tab.PAGES_ID)
        )
        return data
    }

    override fun goToSmartSearch(data: MockCanvas, query: String) {
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        val course = data.courses.values.first()

        dashboardPage.selectCourse(course)
        composeTestRule.onNodeWithTag("searchButton").performClick()

        composeTestRule.onNodeWithTag("searchField")
            .requestFocus()
            .performClick()
            .performTextInput(query)

        composeTestRule.onNodeWithTag("searchField").performImeAction()

    }

    override fun assertDiscussionPage(discussionTopic: DiscussionTopicHeader) {
        discussionDetailsPage.assertToolbarDiscussionTitle(discussionTopic.title.orEmpty())
    }

    override fun assertPage(page: Page) {
        pageDetailsPage.assertToolbarTitle(page.id.toString())
    }


}