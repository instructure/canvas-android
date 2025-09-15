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

import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.student.ui.utils.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.UrlSubmissionViewFragment
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class UrlSubmissionViewRenderTest : StudentRenderTest() {

    private val testUrl = "https://www.instructure.com"

    @Test
    fun displaysDisclaimer() {
        loadPageWithData(testUrl, "")
        urlSubmissionViewRenderPage.disclaimer.assertCompletelyDisplayed()
    }

    @Test
    fun displaysUrl() {
        loadPageWithData(testUrl, "")
        urlSubmissionViewRenderPage.url.assertHasText(testUrl)
    }

    private fun loadPageWithData(url: String, previewUrl: String) = with(activityRule.activity) {
        runOnUiThread {
            val fragment = UrlSubmissionViewFragment.newInstance(url, previewUrl)
            loadFragment(fragment)
        }
    }
}
