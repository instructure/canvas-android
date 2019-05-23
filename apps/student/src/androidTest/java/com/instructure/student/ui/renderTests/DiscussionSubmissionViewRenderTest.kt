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
 */
package com.instructure.student.ui.renderTests

import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.DiscussionSubmissionViewFragment
import com.instructure.student.ui.pages.renderPages.DiscussionSubmissionViewRenderPage
import org.junit.Test

class DiscussionSubmissionViewRenderTest : StudentRenderTest() {

    private val page = DiscussionSubmissionViewRenderPage()
    private val linkUrl = "https://www.google.com"

    @Test
    fun displaysProgressBarPriorToLoading() {
        loadPageWithUrl(linkUrl)
        page.assertDisplaysProgressBar()
    }

    @Test
    fun showsWebViewAndHidesProgressBarAfterLoading() {
        loadPageWithUrl(linkUrl)
        page.assertDisplaysLoadedPage()
    }

    @Test
    fun linkOpensWebView() {
        loadPageWithUrl(linkUrl)
        page.assertUrlMatches(linkUrl)
    }

    private fun loadPageWithUrl(url: String) {
        val fragment = DiscussionSubmissionViewFragment.newInstance(url)
        activityRule.activity.loadFragment(fragment)
    }

}
