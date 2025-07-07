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
package com.instructure.student.ui.pages

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.getText
import com.instructure.canvas.espresso.matchToolbarText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R
import org.hamcrest.Matchers

open class PairObserverPage : BasePage(R.id.pairObserverPage) {

    private val pairObserverContent by WaitForViewWithId(R.id.pairObserverContent)
    private val pairObserverQrCode by OnViewWithId(R.id.pairObserverQrCode)
    private val pairObserverRefresh by OnViewWithId(R.id.pairObserverRefresh)
    private val pairObserverCodeLabel by OnViewWithId(R.id.pairObserverCodeLabel)
    private val pairObserverCode by OnViewWithId(R.id.pairObserverCode)

    fun refreshPairingCode() {
        pairObserverRefresh.click()
    }

    fun hasPairingCode(code: String) {
        waitForView(withId(R.id.pairObserverCode) + withText(code)).assertDisplayed()
    }

    fun assertDescription() {
        onView(withText(R.string.pairWithObserverDetails) + withAncestor(R.id.pairObserverContent)).assertDisplayed()
    }

    fun assertToolbarTitle() {
        onView(ViewMatchers.withId(R.id.toolbar) + withParent(R.id.pairObserverPage)).assertDisplayed().check(
            ViewAssertions.matches(matchToolbarText(Matchers.`is`(getStringFromResource(R.string.pairWithObserver)), true)))
    }

    fun getPairingCode(): String {
        return getText(withId(R.id.pairObserverCode))
    }
}
