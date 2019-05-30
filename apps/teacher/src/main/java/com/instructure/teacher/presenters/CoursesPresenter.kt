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
package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.viewinterface.CoursesView
import instructure.androidblueprint.SyncPresenter
import retrofit2.Call
import retrofit2.Response

class CoursesPresenter : SyncPresenter<Course, CoursesView>(Course::class.java) {

    override fun loadData(forceNetwork: Boolean) {
        if (forceNetwork) {
            clearData()
        }
        else if (data.size() > 0) {
            viewCallback?.checkIfEmpty()
            return
        }

        onRefreshStarted()
        CourseManager.getCourses(forceNetwork, mCoursesCallback)
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        mCoursesCallback.reset()
        mDashboardCallback?.reset()
        clearData()
        loadData(forceNetwork)
    }

    private var mDashboardCallback: StatusCallback<List<DashboardCard>>? = null
    private val mCoursesCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
            val courses = response.body()
            if (courses.isNullOrEmpty()) {
                notifyRefreshFinished()
            } else {
                addIfOnDashboard(courses)
            }
        }

        override fun onFail(call: Call<List<Course>>?, error: Throwable, response: Response<*>?) {
            super.onFail(call, error, response)
            notifyRefreshFinished()
        }
    }

    private fun addIfOnDashboard(courses: List<Course>) {
        mDashboardCallback = object : StatusCallback<List<DashboardCard>>() {
            override fun onResponse(
                response: Response<List<DashboardCard>>,
                linkHeaders: LinkHeaders,
                type: ApiType
            ) {
                val dashboardCourses = response.body() ?: return
                val courseMap = courses.associateBy { it.id }
                val validCourses = dashboardCourses.mapNotNull { courseMap[it.id] }

                data.addOrUpdate(validCourses)
                notifyRefreshFinished()
            }

            override fun onFail(
                call: Call<List<DashboardCard>>?,
                error: Throwable,
                response: Response<*>?
            ) {
                super.onFail(call, error, response)
                notifyRefreshFinished()
            }
        }

        CourseManager.getDashboardCourses(true, mDashboardCallback!!)
    }

    private fun notifyRefreshFinished() {
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }

    override fun areItemsTheSame(item1: Course, item2: Course): Boolean {
        return item1.contextId.hashCode() == item2.contextId.hashCode()
    }

    override fun areContentsTheSame(item1: Course, item2: Course): Boolean {
        return item1.contextId.hashCode() == item2.contextId.hashCode()
    }
}
