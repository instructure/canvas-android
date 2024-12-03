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
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onViewWithContentDescription
import com.instructure.espresso.pages.onViewWithText
import com.instructure.teacher.R

/**
 * Represents a page for the comment library.
 *
 * This class extends the `BasePage` class and provides methods for asserting the visibility and count of suggestions,
 * checking the visibility of the suggestion list and empty view, selecting a suggestion, and closing the comment library.
 *
 * @constructor Creates an instance of the `CommentLibraryPage` class.
 */
class CommentLibraryPage : BasePage(R.id.commentLibraryRoot) {

    private val toolbar by OnViewWithId(R.id.commentLibraryToolbar)
    private val recyclerView by OnViewWithId(R.id.commentLibraryRecyclerView)
    private val emptyView by OnViewWithId(R.id.commentLibraryEmtpyView, autoAssert = false)

    /**
     * Asserts the visibility of a suggestion with the provided text.
     *
     * @param suggestion The text of the suggestion to be asserted.
     * @throws AssertionError if the suggestion is not visible.
     */
    fun assertSuggestionVisible(suggestion: String) {
        onViewWithText(suggestion).assertDisplayed()
    }

    /**
     * Asserts the count of suggestions in the comment library.
     *
     * @param expectedCount The expected count of suggestions.
     * @throws AssertionError if the actual count of suggestions does not match the expected count.
     */
    fun assertSuggestionsCount(expectedCount: Int) {
        recyclerView.check(RecyclerViewItemCountAssertion(expectedCount))
    }

    /**
     * Asserts that the count of suggestions in the comment library is greater than the provided count.
     *
     * @param count The count to compare against.
     * @throws AssertionError if the count of suggestions is not greater than the provided count.
     */
    fun assertSuggestionsCountGreaterThan(count: Int) {
        recyclerView.check(RecyclerViewItemCountGreaterThanAssertion(count))
    }

    /**
     * Asserts that the suggestion list is not visible.
     *
     * @throws AssertionError if the suggestion list is visible.
     */
    fun assertSuggestionListNotVisible() {
        recyclerView.assertNotDisplayed()
    }

    /**
     * Asserts the visibility of the empty view in the comment library.
     *
     * @throws AssertionError if the empty view is not visible.
     */
    fun assertEmptyViewVisible() {
        emptyView.assertDisplayed()
    }

    /**
     * Selects a suggestion with the provided text.
     *
     * @param suggestion The text of the suggestion to be selected.
     */
    fun selectSuggestion(suggestion: String) {
        onViewWithText(suggestion).click()
    }

    /**
     * Closes the comment library.
     */
    fun closeCommentLibrary() {
        onViewWithContentDescription(R.string.close).click()
    }
}
