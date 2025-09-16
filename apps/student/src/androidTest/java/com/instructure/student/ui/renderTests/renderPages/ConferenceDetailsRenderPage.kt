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
package com.instructure.student.ui.renderTests.renderPages

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withAlpha
import com.instructure.canvas.espresso.assertIsRefreshing
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertGone
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertInvisible
import com.instructure.espresso.assertVisible
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceRecordingViewState
import com.instructure.student.ui.pages.classic.ConferenceDetailsPage
import org.hamcrest.Matchers.allOf

class ConferenceDetailsRenderPage : ConferenceDetailsPage() {
    private val swipeRefreshLayout by OnViewWithId(R.id.swipeRefreshLayout)

    // Title ans status
    private val title by OnViewWithId(R.id.title)
    private val inProgressIndicator by OnViewWithId(R.id.inProgressIndicator)
    private val inProgressDivider by OnViewWithId(R.id.inProgressIndicatorDivider)
    private val status by OnViewWithId(R.id.status)

    // Description
    private val description by OnViewWithId(R.id.description)

    // Recording List
    private val recordingSection by OnViewWithId(R.id.recordingsSection)
    private val recordingTitle by OnViewWithId(R.id.recordingTitle)
    private val recordingDate by OnViewWithId(R.id.recordingDate)
    private val recordingDuration by OnViewWithId(R.id.recordingDuration)
    private val recordingContent by OnViewWithId(R.id.recordingContent)
    private val recordingProgressBar by OnViewWithId(R.id.recordingProgressBar)

    // Join button
    private val joinContainer by OnViewWithId(R.id.joinContainer)
    private val joinButton by OnViewWithId(R.id.joinButton)
    private val joinProgressBar by OnViewWithId(R.id.joinProgressBar)

    private fun findChildText(text: String, parentId: Int) = onView(allOf(withText(text), withParent(parentId)))

    fun assertDisplaysToolbarTitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertDisplaysToolbarSubtitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertRefreshing(isRefreshing: Boolean) {
        swipeRefreshLayout.assertIsRefreshing(isRefreshing)
    }

    fun assertDisplaysJoinable() {
        joinContainer.assertVisible()
        joinButton.assertVisible()
        joinProgressBar.assertGone()
    }

    fun assertDisplaysJoining() {
        joinContainer.assertVisible()
        joinButton.assertInvisible()
        joinProgressBar.assertVisible()
    }

    fun assertDisplaysInProgressIndicator(shouldDisplay: Boolean) {
        if (shouldDisplay) {
            inProgressIndicator.assertVisible()
            inProgressDivider.assertVisible()
        } else {
            inProgressIndicator.assertGone()
            inProgressDivider.assertGone()
        }
    }

    fun assertDisplaysTitle(text: String) {
        title.assertDisplayed().assertHasText(text)
    }

    fun assertDisplaysStatus(text: String) {
        status.assertDisplayed().assertHasText(text)
    }

    fun assertDisplaysDescription(text: String) {
        description.assertDisplayed().assertHasText(text)
    }

    fun assertDisplaysRecordingSection(shouldDisplay: Boolean) {
        if (shouldDisplay) recordingSection.assertVisible() else recordingSection.assertGone()
    }

    fun assertDisplaysRecording(state: ConferenceRecordingViewState) {
        scrollTo(state.duration)
        recordingTitle.assertDisplayed().assertHasText(state.title)
        recordingDate.assertDisplayed().assertHasText(state.date)
        recordingDuration.assertDisplayed().assertHasText(state.duration)
        if (state.isLaunching) {
            recordingContent.check(matches(withAlpha(0.35f)))
            recordingProgressBar.assertVisible()
        } else {
            recordingContent.check(matches(withAlpha(1f)))
            recordingProgressBar.assertGone()
        }
    }
}
