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
 *
 */
package com.instructure.student.ui.pages.renderPages

import android.view.View
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import com.instructure.student.ui.pages.SubmissionDetailsPage
import org.hamcrest.CoreMatchers.*

class SubmissionDetailsRenderPage : SubmissionDetailsPage() {

    val toolbar by OnViewWithId(R.id.toolbar)
    val loadingView by OnViewWithId(R.id.loadingView)
    val errorView by OnViewWithId(R.id.errorContainer)
    val mainContent by OnViewWithId(R.id.contentWrapper)
    val drawerContent by OnViewWithId(R.id.drawerViewPager)
    val slidingPanel by OnViewWithId(R.id.slidingUpPanelLayout)
    val versionSpinner by OnViewWithId(R.id.submissionVersionsSpinner)

    /* Grabs the current coordinates of the center of drawerTabLayout */
    private val tabLayoutCoordinates = object : CoordinatesProvider {
        override fun calculateCoordinates(view: View): FloatArray {
            val tabs = view.findViewById<View>(R.id.drawerTabLayout)
            val xy = IntArray(2).apply { tabs.getLocationOnScreen(this) }
            val x = xy[0] + (tabs.width / 2f)
            val y = xy[1] + (tabs.height / 2f)
            return floatArrayOf(x, y)
        }
    }

    fun assertDisplaysToolbarTitle(text: String) {
        onViewWithText(text).assertDisplayed()
    }

    fun assertDisplaysLoadingView() {
        loadingView.assertDisplayed()
        errorView.assertGone()
        mainContent.assertGone()
    }

    fun assertDisplaysError() {
        loadingView.assertGone()
        mainContent.assertGone()
        errorView.assertVisible()
        onViewWithText(R.string.submissionDetailsErrorTitle).assertDisplayed()
        onViewWithText(R.string.error_loading_submission).assertDisplayed()
        onViewWithId(R.id.retryButton).assertDisplayed()
    }

    fun assertDisplaysContent() {
        mainContent.assertDisplayed()
        loadingView.assertGone()
        errorView.assertGone()
    }

    fun assertDisplaysEmptySubmissionContent() {
        mainContent.assertDisplayed()
        loadingView.assertGone()
        errorView.assertGone()
        onViewWithText("No Submission Yet").assertDisplayed()
        onViewWithText("Submit Assignment").assertDisplayed()
    }

    fun assertSubmitButtonNotDisplayed() {
        mainContent.assertDisplayed()
        loadingView.assertGone()
        errorView.assertGone()
        onViewWithText("No Submission Yet").assertDisplayed()
        onViewWithText("Submit Assignment").assertNotDisplayed()
    }

    fun assertDisplaysDrawerContent() {
        drawerContent.assertDisplayed()
    }

    fun clickTab(name: String) {
        onView(allOf(withAncestor(R.id.drawerTabLayout), withText(name))).click()
    }

    fun swipeDrawerTo(location: GeneralLocation) {
        slidingPanel.perform(GeneralSwipeAction(Swipe.FAST, tabLayoutCoordinates, location, Press.FINGER))
    }

    fun assertSpinnerMatchesText(text: String) {
        versionSpinner.check(matches(withSpinnerText(containsString(text))))
    }

    fun assertSpinnerDropdownItemHasText(position: Int, text: String) {
        onData(anything()).atPosition(position).check(matches(ViewMatchers.withText(text)))
    }
}
