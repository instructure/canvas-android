/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.RecyclerViewItemCountGreaterThanAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.teacher.R

class CommentLibraryPage : BasePage(R.id.commentLibraryRoot) {

    private val toolbar by OnViewWithId(R.id.commentLibraryToolbar)
    private val recyclerView by OnViewWithId(R.id.commentLibraryRecyclerView)
    private val emptyView by OnViewWithId(R.id.commentLibraryEmtpyView, autoAssert = false)

    fun assertSuggestionVisible(suggestion: String) {
        onViewWithText(suggestion).assertDisplayed()
    }

    fun assertSuggestionsCount(expectedCount: Int) {
        recyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    fun assertSuggestionsCountGreaterThan(count: Int) {
        recyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    fun assertSuggestionListNotVisible() {
        recyclerView.assertNotDisplayed()
    }

    fun assertEmptyViewVisible() {
        emptyView.assertDisplayed()
    }

    fun selectSuggestion(suggestion: String) {
        onViewWithText(suggestion).click()
    }

    fun closeCommentLibrary() {
        onViewWithContentDescription(R.string.close).click()
    }

}