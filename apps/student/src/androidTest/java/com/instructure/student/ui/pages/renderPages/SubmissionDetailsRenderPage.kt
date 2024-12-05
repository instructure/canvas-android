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
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.onViewWithText
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import com.instructure.student.ui.pages.SubmissionDetailsPage
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.Description


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
        onViewWithText(R.string.submissionDetailsErrorTitle).scrollTo().assertDisplayed()
        onViewWithText(R.string.error_loading_submission).scrollTo().assertDisplayed()
        onViewWithId(R.id.retryButton).scrollTo().assertDisplayed()
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
        slidingPanel.waitForCheck(matches(SlidingUpPanelOpenMatcher()))
    }

    fun clickTab(name: String) {
        onView(allOf(withAncestor(R.id.drawerTabLayout), withText(name))).click()
    }

    fun swipeDrawerTo(location: GeneralLocation) {
        slidingPanel.perform(GeneralSwipeAction(Swipe.FAST, tabLayoutCoordinates, location, Press.FINGER))
    }

    fun assertSpinnerMatchesText(text: String) {
        onView(allOf(withId(R.id.attemptDate), withText(text))).assertDisplayed()
    }

    fun assertSpinnerDropdownItemHasText(position: Int, text: String) {
        onData(anything()).atPosition(position).check(matches(withChild(withText(text))))
    }

    fun waitForDrawerRender() {
        onView(withId(R.id.drawerTabLayout)).waitForCheck(matches(isDisplayed()))
    }
}

// Using a ViewAction to do loopMainThreadUntilIdle doesn't seem to work here, likely from SlidingUpPanelLayout not
// using animators correctly. Instead we have to break the black box and check the panel's state to see when it updates
// from PanelState.DRAGGING to either ANCHORED or EXPANDED. This will tell us when the view has finished handling drags
// and the rest of the test can proceed.
class SlidingUpPanelOpenMatcher : BoundedMatcher<View, SlidingUpPanelLayout>(SlidingUpPanelLayout::class.java) {
    override fun describeTo(description: Description?) {
        description?.appendText("panel that is open (anchored or expanded)")
    }

    override fun matchesSafely(item: SlidingUpPanelLayout?): Boolean {
        return item?.panelState?.let { it == SlidingUpPanelLayout.PanelState.ANCHORED || it == SlidingUpPanelLayout.PanelState.EXPANDED }
            ?: false
    }
}
