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

import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.TextSubmissionViewFragment
import com.instructure.student.ui.rendertests.renderpages.TextSubmissionViewRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class TextSubmissionViewRenderTest : StudentRenderTest() {

    private val page = TextSubmissionViewRenderPage()

    @Stub
    @Test
    fun displaysProgressBarPriorToLoading() {
        loadPageWithHtml("Sample Text")
        page.assertDisplaysProgressBar()
    }

    @Test
    fun showsWebViewAndHidesProgressBarAfterLoading() {
        loadPageWithHtml("Sample Text")
        page.assertDisplaysLoadedPage()
    }

    @Test
    fun displaysHtml() {
        val elementId = "text"
        val text = "This is some text"
        val html = """<p id="$elementId">$text</p>"""
        loadPageWithHtml(html)
        page.assertDisplaysHtmlText(text, elementId)
    }

    @Test
    fun linkOpensWebView() {
        val elementId = "link"
        val linkUrl = "https://www.google.com"
        val html = """<a id="$elementId" href="https://www.google.com">Click this link</a>"""
        loadPageWithHtml(html)
        page.clickElement(elementId)
        page.assertUrlMatches(linkUrl)
    }

    private fun loadPageWithHtml(html: String) = with(activityRule.activity) {
        runOnUiThread {
            val fragment = TextSubmissionViewFragment.newInstance(html)
            loadFragment(fragment)
        }
    }
}
