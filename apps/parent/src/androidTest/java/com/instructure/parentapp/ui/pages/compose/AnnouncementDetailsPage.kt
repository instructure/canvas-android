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
 *
 */

package com.instructure.parentapp.ui.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandares.R

class AnnouncementDetailsPage(private val composeTestRule: ComposeTestRule) {

    fun assertCourseAnnouncementDetailsDisplayed(
        course: Course,
        discussionTopicHeader: DiscussionTopicHeader
    ) {
        composeTestRule.onNodeWithText(course.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(discussionTopicHeader.title.orEmpty()).assertIsDisplayed()
        val dateString = DateHelper.getDateAtTimeString(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.string.alertDateTime,
            discussionTopicHeader.postedDate
        )
        dateString?.let {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    fun assertGlobalAnnouncementDetailsDisplayed(accountNotification: AccountNotification) {
        composeTestRule.onNodeWithText("Global Announcement").assertIsDisplayed()
        composeTestRule.onNodeWithText(accountNotification.subject).assertIsDisplayed()
        val dateString = DateHelper.getDateAtTimeString(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.string.alertDateTime,
            accountNotification.startDate
        )
        dateString?.let {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }
}
