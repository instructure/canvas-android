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

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.teacher.R

class CommentItemViewModel(private val comment: String,
                           private val searchQuery: String,
                           private val onItemClick: (String) -> Unit): ItemViewModel {

    override val layoutId: Int = R.layout.item_comment_library_comment

    val commentItemText = createCommentItemText(comment, searchQuery)

    fun createCommentItemText(comment: String, searchQuery: String): Spannable {
        val spanStart = comment.indexOf(searchQuery, ignoreCase = true)
        val spanEnd = spanStart + searchQuery.length

        return SpannableString(comment).apply {
            setSpan(StyleSpan(Typeface.BOLD),spanStart, spanEnd, 0)
        }
    }

    fun onClick() {
        onItemClick(comment)
    }
}