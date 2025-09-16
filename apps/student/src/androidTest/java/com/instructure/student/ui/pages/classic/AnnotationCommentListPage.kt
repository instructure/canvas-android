/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.replaceText
import com.instructure.student.R

open class AnnotationCommentListPage : BasePage(R.id.annotationCommentsListPage) {

    private val sendButton by OnViewWithId(R.id.sendCommentButton)
    private val commentEditText by OnViewWithId(R.id.commentEditText)

    fun sendComment(commentText: String) {
        commentEditText.replaceText(commentText)
        sendButton.click()
    }

    fun  assertCommentDisplayed(commentText: String) {
        val matcher = ViewMatchers.withText(commentText)
        scrollRecyclerView(R.id.annotationCommentsRecyclerView, matcher)
        waitForView(matcher).assertDisplayed()
    }

}