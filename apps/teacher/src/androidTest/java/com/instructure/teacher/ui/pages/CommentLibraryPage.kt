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

import android.util.Log
import com.instructure.espresso.*
import com.instructure.teacher.R
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText

class CommentLibraryPage : BasePage(R.id.commentLibraryRoot) {

    private val toolbar by OnViewWithId(R.id.commentLibraryToolbar)
    private val recyclerView by OnViewWithId(R.id.commentLibraryRecyclerView)
    private val emptyView by OnViewWithId(R.id.commentLibraryEmtpyView, autoAssert = false)

    companion object {
        const val ACTION_TAG = "CommentLibraryPage #ACTION# "
        const val ASSERTION_TAG = "CommentLibraryPage #ASSERT# "
    }

    fun assertSuggestionVisible(suggestion: String) {
        Log.d(ASSERTION_TAG, "Assert that '$suggestion' is visible.")
        onViewWithText(suggestion).assertDisplayed()
    }

    fun assertSuggestionsCount(expectedCount: Int) {
        Log.d(ASSERTION_TAG, "Assert that the number of suggestions (comments) is: $expectedCount")
        recyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    fun assertSuggestionsCountGreaterThan(count: Int) {
        recyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    fun assertSuggestionListNotVisible() {
        Log.d(ASSERTION_TAG,"Assert that suggestion list is not visible.")
        recyclerView.assertNotDisplayed()
    }

    fun assertEmptyViewVisible() {
        Log.d(ASSERTION_TAG, "Assert that empty view is visible.")
        emptyView.assertDisplayed()
    }

    fun selectSuggestion(suggestion: String) {
        Log.d(ACTION_TAG, "Select suggestion: $suggestion")
        onViewWithText(suggestion).click()
    }

    fun closeCommentLibrary() {
        Log.d(ACTION_TAG, "Close comment library.")
        onViewWithContentDescription(R.string.close).click()
    }

}