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
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.utils.hasActiveEnrollment
import com.instructure.teacher.viewinterface.AllCoursesView
import instructure.androidblueprint.SyncPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.Response

class AllCoursesPresenter : SyncPresenter<Course, AllCoursesView>(Course::class.java) {

    init {
        EventBus.getDefault().register(this)
    }

    override fun loadData(forceNetwork: Boolean) {
        if(forceNetwork) {
            mFavoriteCoursesCallback.reset()
            clearData()
        } else if (data.size() > 0) {
            return
        }
        viewCallback?.onRefreshStarted()
        CourseManager.getCoursesTeacher(forceNetwork, mFavoriteCoursesCallback)
    }

    override fun refresh(forceNetwork: Boolean) {
        mFavoriteCoursesCallback.reset()
        clearData()
        loadData(forceNetwork)
    }

    private val mFavoriteCoursesCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
            val courses = response.body() ?: return
            val validCourses = courses.filter {
                (it.isTeacher || it.isTA || it.isDesigner) && it.hasActiveEnrollment()
            }
            data.addOrUpdate(validCourses)
        }

        override fun onFinished(type: ApiType) {
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        }
    }

    override fun compare(item1: Course, item2: Course): Int =
            item1.name.toLowerCase().compareTo(item2.name.toLowerCase())

    override fun onDestroyed() {
        super.onDestroyed()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(sticky = true)
    fun onCourseEdited(event: CourseUpdatedEvent) {
        event.once(javaClass.simpleName) {
            refresh(true)
        }
    }
}
