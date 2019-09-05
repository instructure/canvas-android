/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import com.instructure.student.ui.utils.TypeInRCETextEditor
import instructure.rceditor.RCETextEditor
import org.hamcrest.Matcher

class DiscussionDetailsPage : BasePage(R.id.discussionDetailsPage) {
    private val discussionTopicTitle by OnViewWithId(R.id.discussionTopicTitle)
    private val replyButton by OnViewWithId(R.id.replyToDiscussionTopic)

    fun assertTitleText(titleText: String) {
        discussionTopicTitle.assertHasText(titleText)
    }

    fun clickReply() {
        replyButton.click()
    }

    fun assertRepliesDisplayed() {
        onView(withId(R.id.discussionTopicRepliesTitle)).scrollTo().assertDisplayed()
    }

    fun assertNoRepliesDisplayed() {
        onView(withId(R.id.discussionTopicRepliesTitle)).assertNotDisplayed()
    }

    fun sendReply(replyMessage: String) {
        clickReply()
        onView(withId(R.id.rce_webView)).perform(TypeInRCETextEditor(replyMessage))
        onView(withId(R.id.menu_send)).click()
    }
}

