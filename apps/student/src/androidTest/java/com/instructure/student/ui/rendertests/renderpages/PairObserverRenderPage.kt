/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.ui.rendertests.renderpages

import com.instructure.canvas.espresso.waitForMatcherWithSleeps
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R
import com.instructure.student.ui.pages.classic.PairObserverPage
import org.hamcrest.Matchers.allOf

class PairObserverRenderPage : PairObserverPage() {

    private val loadingView by OnViewWithId(R.id.pairObserverLoading)
    private val errorView by OnViewWithId(R.id.errorContainer)
    private val pairingView by OnViewWithId(R.id.pairObserverContent)

    fun assertDisplaysToolbarTitle(text: String) {
        onView(allOf(withText(text), withParent(R.id.toolbar)))
    }

    fun assertCodeDisplayed(itemText: String) {
        waitForMatcherWithSleeps(withId(R.id.pairObserverContent), timeout = 5000)
        errorView.assertGone()
        loadingView.assertGone()
        pairingView.assertDisplayed()

        onView(withText(itemText)).assertDisplayed()
    }

    fun assertDisplaysLoading() {
        errorView.assertGone()
        loadingView.assertDisplayed()
        pairingView.assertGone()
    }

    fun assertDisplaysError() {
        errorView.assertDisplayed()
        loadingView.assertGone()
        pairingView.assertGone()
    }
}
