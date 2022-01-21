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
package com.instructure.teacher.features.speedgrader.commentlibrary.itemviewmodels

import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.teacher.R
import com.instructure.teacher.features.speedgrader.commentlibrary.HighlightedSuggestionViewData

class SuggestionItemViewModel(private val suggestion: String,
                              private val searchQuery: String,
                              private val onItemClick: (String) -> Unit): ItemViewModel {

    override val layoutId: Int = R.layout.item_comment_library_comment

    val commentItemData = createHighlightedItemData(suggestion, searchQuery)

    fun createHighlightedItemData(suggestion: String, searchQuery: String): HighlightedSuggestionViewData {
        val highlightStart = suggestion.indexOf(searchQuery, ignoreCase = true)
        val highlightEnd = highlightStart + searchQuery.length

        return HighlightedSuggestionViewData(suggestion, highlightStart, highlightEnd)
    }

    fun onClick() {
        onItemClick(suggestion)
    }
}