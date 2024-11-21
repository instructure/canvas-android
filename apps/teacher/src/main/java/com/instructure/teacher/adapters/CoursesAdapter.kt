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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.instructure.canvasapi2.models.Course
import com.instructure.teacher.databinding.AdapterCoursesBinding
import com.instructure.teacher.fragments.DashboardFragment
import com.instructure.teacher.holders.CoursesViewHolder
import com.instructure.teacher.presenters.DashboardPresenter
import com.instructure.teacher.viewinterface.CoursesView
import com.instructure.pandautils.blueprint.SyncRecyclerAdapter

class CoursesAdapter(
    context: Context,
    presenter: DashboardPresenter,
    private val mCourseCallback: DashboardFragment.CourseBrowserCallback?
) : SyncRecyclerAdapter<Course, CoursesViewHolder, CoursesView>(context, presenter) {

    override fun bindHolder(model: Course, holder: CoursesViewHolder, position: Int) {
        holder.bind(model, mCourseCallback)
    }

    override fun createViewHolder(binding: ViewBinding, viewType: Int) = CoursesViewHolder(binding as AdapterCoursesBinding)

    override fun bindingInflater(viewType: Int): (LayoutInflater, ViewGroup, Boolean) -> ViewBinding = AdapterCoursesBinding::inflate
}
