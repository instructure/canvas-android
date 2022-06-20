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
import com.instructure.pandautils.utils.HighlightedTextData
import com.instructure.pandautils.utils.unaccent
import com.instructure.teacher.R

class SuggestionItemViewModel(private val suggestion: String,
                              private val searchQuery: String,
                              private val onItemClick: (String) -> Unit): ItemViewModel {

    override val layoutId: Int = R.layout.item_comment_library_comment

    val commentItemData = createHighlightedItemData(suggestion, searchQuery)

    fun createHighlightedItemData(suggestion: String, searchQuery: String): HighlightedTextData {
        val highlightStart = suggestion.unaccent().indexOf(searchQuery.unaccent(), ignoreCase = true)
        val highlightEnd = highlightStart + searchQuery.length

        return HighlightedTextData(suggestion, highlightStart, highlightEnd)
    }

    fun onClick() {
        onItemClick(suggestion)
    }
}