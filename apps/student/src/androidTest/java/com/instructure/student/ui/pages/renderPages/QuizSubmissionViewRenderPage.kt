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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms.getCurrentUrl
import androidx.test.espresso.web.sugar.Web.onWebView
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers.containsString

class QuizSubmissionViewRenderPage : BasePage(R.id.activity_root) {

    private val progressBar by OnViewWithId(R.id.webViewLoading)
    private val webView by OnViewWithId(R.id.contentWebView)

    fun assertDisplaysProgressBar() {
        progressBar.assertDisplayed()
    }

    fun assertDisplaysLoadedPage() {
        webView.waitForCheck(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
        progressBar.assertNotDisplayed() // progress is hidden when webView is done loading
    }

    fun assertUrlMatches(url: String) {
        onWebView().check(webMatches(getCurrentUrl(), containsString(url)))
    }

}
