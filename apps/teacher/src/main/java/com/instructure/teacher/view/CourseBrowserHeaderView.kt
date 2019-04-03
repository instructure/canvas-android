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

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.instructure.teacher.R

class CourseBrowserHeaderView : LinearLayout {

    private val mTitle by lazy { findViewById<TextView>(R.id.courseBrowserHeaderTitle) }
    private val mSubtitle by lazy { findViewById<TextView>(R.id.courseBrowserHeaderSubtitle) as TextView }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setTitle(title: String?) {
        if (title == null || title.isEmpty()) {
            mTitle.text = ""
            return
        }
        mTitle.text = title
    }

    fun setSubtitle(subtitle: String?) {
        if (subtitle == null || subtitle.isEmpty()) {
            mSubtitle.text = ""
            return
        }
        mSubtitle.text = subtitle
    }

    fun setTitleAndSubtitle(title: String, subtitle: String) {
        setTitle(title)
        setSubtitle(subtitle)
    }
}
