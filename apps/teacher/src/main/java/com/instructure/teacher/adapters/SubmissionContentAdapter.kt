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
package com.instructure.teacher.adapters

import androidx.viewpager.widget.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.teacher.view.SubmissionContentView
import java.util.*

class SubmissionContentAdapter(
        private val mAssignment: Assignment,
        private val mCourse: Course,
        private val mStudentSubmissions: List<GradeableStudentSubmission>
) : PagerAdapter() {

    var initialTabIdx = 0

    private val mContentMap = WeakHashMap<Int, SubmissionContentView>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return SubmissionContentView(
            context = container.context,
            studentSubmission = mStudentSubmissions[position],
            assignment = mAssignment,
            course = mCourse,
            initialTabIndex = initialTabIdx
        ).apply {
            container.addView(this)
            mContentMap += position to this
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as SubmissionContentView
        view.performCleanup()
        container.removeView(view)
        mContentMap -= position
    }

    override fun getCount() = mStudentSubmissions.size

    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    fun hasUnsavedChanges(position: Int) = mContentMap[position]?.hasUnsavedChanges ?: false

    fun invalidateSubmissionCache() {
        mStudentSubmissions.onEach { it.isCached = false }
    }

    fun updateAnnotations(position: Int) {
        mContentMap[position]?.updateAnnotations()
    }
}
