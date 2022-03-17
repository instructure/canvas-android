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

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.utils.hasActiveEnrollment
import com.instructure.teacher.viewinterface.AllCoursesView
import instructure.androidblueprint.SyncPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Locale

class AllCoursesPresenter : SyncPresenter<Course, AllCoursesView>(Course::class.java) {

    private var loadJob: Job? = null

    init {
        EventBus.getDefault().register(this)
    }

    override fun loadData(forceNetwork: Boolean) {
        // Do nothing if we already have data and we're not forcing a refresh
        if (data.size() > 0 && !forceNetwork) return

        // Cancel existing job, if any
        loadJob?.cancel()

        // Start new job to load data
        loadJob = GlobalScope.launch(Dispatchers.Main) {
            clearData()
            viewCallback?.onRefreshStarted()
            ColorApiHelper.awaitSync()
            try {
                val courses: List<Course> = awaitApi { CourseManager.getCoursesTeacher(forceNetwork, it) }
                val validCourses = courses.filter {
                    (it.isTeacher || it.isTA || it.isDesigner) && it.hasActiveEnrollment()
                }
                data.addOrUpdate(validCourses)
            } catch (ignore: Throwable) {
            }
            viewCallback?.onRefreshFinished()
            viewCallback?.checkIfEmpty()
        }
    }

    override fun refresh(forceNetwork: Boolean) = loadData(forceNetwork)

    override fun compare(item1: Course, item2: Course): Int =
            item1.name.lowercase(Locale.getDefault())
                .compareTo(item2.name.lowercase(Locale.getDefault()))

    override fun onDestroyed() {
        loadJob?.cancel()
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
