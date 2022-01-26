/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.instructure.canvasapi2.utils.tryOrNull

class SubmissionContentViewPager @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    var isPagingEnabled = true
    var isCommentLibraryOpen = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && !isCommentLibraryOpen && tryOrNull { super.onTouchEvent(ev) } ?: false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isPagingEnabled && !isCommentLibraryOpen && tryOrNull { super.onInterceptTouchEvent(ev) } ?: false
    }
}
