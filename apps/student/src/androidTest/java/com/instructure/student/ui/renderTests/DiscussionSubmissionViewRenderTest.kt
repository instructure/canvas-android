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

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.DiscussionSubmissionViewFragment
import com.instructure.student.ui.renderTests.renderPages.DiscussionSubmissionViewRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DiscussionSubmissionViewRenderTest : StudentRenderTest() {

    private val page = DiscussionSubmissionViewRenderPage()
    private val linkUrl = "https://www.google.com"

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun displaysProgressBarPriorToLoading() {
        loadPageWithUrl(linkUrl)
        page.assertDisplaysProgressBar()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun showsWebViewAndHidesProgressBarAfterLoading() {
        loadPageWithUrl(linkUrl)
        page.assertDisplaysLoadedPage()
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.RENDER)
    fun linkOpensWebView() {
        loadPageWithUrl(linkUrl)
        page.assertUrlMatches(linkUrl)
    }

    private fun loadPageWithUrl(url: String) = with(activityRule.activity) {
        runOnUiThread {
            val fragment = DiscussionSubmissionViewFragment.newInstance(url)
            activityRule.activity.loadFragment(fragment)
        }
    }
}
