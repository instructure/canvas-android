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

package com.instructure.student.adapter

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.instructure.student.R
import com.instructure.student.holders.CourseViewHolder
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave


class AllCoursesRecyclerAdapter(
        context: Activity,
        private val mAdapterToFragmentCallback: CourseAdapterToFragmentCallback
) : BaseListRecyclerAdapter<Course, CourseViewHolder>(context, Course::class.java) {

    init {
        loadData()
    }

    private var mApiCall: WeaveJob? = null

    override fun contextReady() = Unit
    override fun setupCallbacks() = Unit
    override fun itemLayoutResId(viewType: Int) = CourseViewHolder.HOLDER_RES_ID
    override fun createViewHolder(v: View, viewType: Int) = CourseViewHolder(v)

    override fun bindHolder(model: Course, holder: CourseViewHolder, position: Int) {
        holder.bind(model, mAdapterToFragmentCallback)
    }

    override fun loadData() {
        mApiCall?.cancel()
        mApiCall = tryWeave {
            val courses = awaitApi<List<Course>> { CourseManager.getCourses(isRefresh, it) }
                    .filter { !it.accessRestrictedByDate && !it.isInvited() }
            addAll(courses)
            notifyDataSetChanged()
            isAllPagesLoaded = true
            if (itemCount == 0) adapterToRecyclerViewCallback.setIsEmpty(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        } catch {
            if (!APIHelper.hasNetworkConnection()) {
                adapterToRecyclerViewCallback.setDisplayNoConnection(true)
            } else {
                adapterToRecyclerViewCallback.setIsEmpty(true)
                Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show()
            }
            mAdapterToFragmentCallback.onRefreshFinished()
        }
    }

    override fun cancel() {
        mApiCall?.cancel()
    }

    override fun refresh() {
        mApiCall?.cancel()
        super.refresh()
    }
}
