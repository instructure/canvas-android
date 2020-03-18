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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListItemViewState
import com.instructure.student.ui.pages.ConferenceListPage
import com.instructure.student.ui.pages.SyllabusPage
import com.instructure.student.ui.utils.assertIsRefreshing
import org.hamcrest.Matchers.allOf

class ConferenceListRenderPage : ConferenceListPage() {
    private val emptyView by OnViewWithId(R.id.conferenceListEmptyView)
    private val errorView by OnViewWithId(R.id.conferenceListErrorView)
    private val swipeRefreshLayout by OnViewWithId(R.id.swipeRefreshLayout)

    fun assertDisplaysToolbarTitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertDisplaysToolbarSubtitle(text: String) {
        findChildText(text, R.id.toolbar).assertDisplayed()
    }

    fun assertDisplaysError() {
        emptyView.check(doesNotExist())
        errorView.assertDisplayed()

        onViewWithText(R.string.conferenceListError).assertDisplayed()
        onViewWithId(R.id.conferenceListRetry).assertDisplayed()
    }

    fun assertDisplaysEmptyView() {
        errorView.check(doesNotExist())
        emptyView.assertDisplayed()

        onViewWithId(R.id.emptyIcon).assertDisplayed()
        onViewWithId(R.id.emptyTitle).assertDisplayed().assertHasText(R.string.noConferencesTitle)
        onViewWithId(R.id.emptyMessage).assertDisplayed().assertHasText(R.string.noConferencesMessage)
    }

    fun assertDisplaysListItems(states: List<ConferenceListItemViewState>) {
        states.forEach {
            when (it) {
                ConferenceListItemViewState.Empty -> assertDisplaysEmptyView()
                ConferenceListItemViewState.Error -> assertDisplaysError()
                is ConferenceListItemViewState.ConferenceHeader -> assertDisplaysConferenceHeader(it)
                is ConferenceListItemViewState.ConferenceItem -> assertDisplaysConferenceItems(it)
            }
        }
    }

    fun assertDisplaysConferenceHeader(state: ConferenceListItemViewState.ConferenceHeader) {
        onViewWithText(state.title).assertDisplayed()
    }

    fun assertDisplaysConferenceItems(state: ConferenceListItemViewState.ConferenceItem) {
        onViewWithText(state.title).assertDisplayed()
        onViewWithText(state.label).assertDisplayed()
        onViewWithText(state.subtitle).assertDisplayed()
    }

    fun assertDisplaysLaunching(isLaunching: Boolean) {
        if (isLaunching) {
            onViewWithId(R.id.openExternallyButton).check(doesNotExist())
            onViewWithId(R.id.launchInBrowserProgressBar).assertVisible()
        } else {
            onViewWithId(R.id.openExternallyButton).assertVisible()
            onViewWithId(R.id.launchInBrowserProgressBar).assertGone()
        }
    }
    fun assertLoading(isLoading: Boolean) {
        swipeRefreshLayout.assertIsRefreshing(isLoading)
    }

    private fun findChildText(text: String, parentId: Int) = onView(allOf(withText(text), withParent(parentId)))
}
