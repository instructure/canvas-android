/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.adapters

import android.content.Context
import android.view.View
import com.instructure.canvasapi2.models.Course
import com.instructure.teacher.fragments.AllCoursesFragment
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.viewinterface.AllCoursesView
import instructure.androidblueprint.SyncPresenter
import instructure.androidblueprint.SyncRecyclerAdapter

class AllCoursesAdapter(context: Context,
                        presenter: SyncPresenter<Course, AllCoursesView>,
                        private val callback: AllCoursesFragment.CourseBrowserCallback?)
    : SyncRecyclerAdapter<Course, CoursesViewHolder, AllCoursesView>(context, presenter) {

    override fun itemLayoutResId(viewType: Int) = CoursesViewHolder.HOLDER_RES_ID

    override fun createViewHolder(v: View, viewType: Int) = CoursesViewHolder(v)

    override fun bindHolder(course: Course, holder: CoursesViewHolder, position: Int) = holder.bind(course, callback)
}
