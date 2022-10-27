/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages


import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.teacher.R

class PostSettingsPage : BasePage() {

    private val postPolicyTabLayout by WaitForViewWithId(R.id.postPolicyTabLayout)

    private val emptyView by WaitForViewWithId(R.id.postEmptyLayout, autoAssert = false)

    fun clickOnTab(tabName: String) {
        onView(withText(tabName)).click()
    }

    fun clickOnTab(tab: Int) {
        onView(withText(tab) + withAncestor(R.id.postPolicyTabLayout)).click()
    }

    fun assertPostPolicyStatusCount(expectedCount: Int, hidden: Boolean) {
        val statusMessageEnd = if(hidden) "hidden" else "posted"
        if (expectedCount == 1) onView(withId(R.id.postPolicyStatusCount) + withText("$expectedCount grade currently $statusMessageEnd")).assertDisplayed()
        else onView(withId(R.id.postPolicyStatusCount) + withText("$expectedCount grades currently $statusMessageEnd")).assertDisplayed()
    }

    fun clickOnPostGradesButton() {
        onView(withId(R.id.postGradeButton) + withText(R.string.postGradesTab)).click()
    }

    fun clickOnHideGradesButton() {
        onView(withId(R.id.postGradeButton) + withText(R.string.hideGradesTab)).click()
    }

    fun assertEmptyView() {
        onView(withId(R.id.postEmptyTitle) + withText(R.string.postPolicyAllPostedTitle)).assertDisplayed()
        onView(withId(R.id.postEmptyMessage) + withText(R.string.postPolicyAllPostedMessage)).assertDisplayed()
    }

}