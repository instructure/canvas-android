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
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.teacher.R

/**
 * Represents the Post Settings Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Post Settings" page.
 * It contains properties for accessing various views on the page such as the post policy tab layout and the empty view.
 * Additionally, it provides methods for clicking on tabs, asserting post policy status count, clicking on post grades and hide grades buttons, and asserting the empty view.
 */
class PostSettingsPage : BasePage() {

    private val postPolicyTabLayout by WaitForViewWithId(R.id.postPolicyTabLayout)

    private val emptyView by WaitForViewWithId(R.id.postEmptyLayout, autoAssert = false)

    /**
     * Clicks on the tab with the specified name.
     *
     * @param tabName The name of the tab to click.
     */
    fun clickOnTab(tabName: String) {
        onView(withText(tabName)).click()
    }

    /**
     * Clicks on the tab at the specified position.
     *
     * @param tab The position of the tab to click.
     */
    fun clickOnTab(tab: Int) {
        onView(withText(tab) + withAncestor(R.id.postPolicyTabLayout)).click()
    }

    /**
     * Asserts the post policy status count.
     *
     * @param expectedCount The expected count of the post policy status.
     * @param hidden Indicates whether the post policy status is hidden or not.
     */
    fun assertPostPolicyStatusCount(expectedCount: Int, hidden: Boolean) {
        val statusMessageEnd = if(hidden) "hidden" else "posted"
        if (expectedCount == 1) onView(withId(R.id.postPolicyStatusCount) + withText("$expectedCount grade currently $statusMessageEnd")).assertDisplayed()
        else onView(withId(R.id.postPolicyStatusCount) + withText("$expectedCount grades currently $statusMessageEnd")).assertDisplayed()
    }

    /**
     * Clicks on the "Post Grades" button.
     */
    fun clickOnPostGradesButton() {
        onView(withId(R.id.postGradeButton) + withText(R.string.postGradesTab)).click()
    }

    /**
     * Clicks on the "Hide Grades" button.
     */
    fun clickOnHideGradesButton() {
        onView(withId(R.id.postGradeButton) + withText(R.string.hideGradesTab)).click()
    }

    /**
     * Asserts the empty view.
     */
    fun assertEmptyView() {
        onView(withId(R.id.postEmptyTitle) + withText(R.string.postPolicyAllPostedTitle)).assertDisplayed()
        onView(withId(R.id.postEmptyMessage) + withText(R.string.postPolicyAllPostedMessage)).assertDisplayed()
    }

}