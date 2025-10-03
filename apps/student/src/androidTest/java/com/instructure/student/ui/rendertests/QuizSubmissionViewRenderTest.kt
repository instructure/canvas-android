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
package com.instructure.student.ui.rendertests

import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.page.onViewWithId
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.QuizSubmissionViewFragment
import com.instructure.student.ui.rendertests.renderpages.QuizSubmissionViewRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizSubmissionViewRenderTest : StudentRenderTest() {

    private val url = "https://www.google.com"
    private val page = QuizSubmissionViewRenderPage()

    @Test
    fun notDisplaysToolbar() {
        loadPageWithUrl(url)
        page.onViewWithId(R.id.toolbar).assertNotDisplayed()
    }

    @Test
    fun displaysProgressBarPriorToLoading() {
        loadPageWithUrl(url)
        page.assertDisplaysProgressBar()
    }

    @Test
    fun showsWebViewAndHidesProgressBarAfterLoading() {
        loadPageWithUrl(url)
        page.assertDisplaysLoadedPage()
    }

    @Test
    fun linkOpensWebView() {
        loadPageWithUrl(url)
        page.assertUrlMatches(url)
    }

    private fun loadPageWithUrl(url: String) = with(activityRule.activity) {
        runOnUiThread {
            val fragment = QuizSubmissionViewFragment.newInstance(url)
            loadFragment(fragment)
        }
    }
}
