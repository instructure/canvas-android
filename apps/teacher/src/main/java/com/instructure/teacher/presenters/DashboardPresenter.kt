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
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.pandautils.blueprint.SyncPresenter
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.teacher.viewinterface.CoursesView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DashboardPresenter : SyncPresenter<Course, CoursesView>(Course::class.java) {

    private var dashboardJob: Job? = null

    override fun loadData(forceNetwork: Boolean) {
        if (forceNetwork) {
            clearData()
        }
        else if (data.size() > 0) {
            viewCallback?.checkIfEmpty()
            return
        }

        onRefreshStarted()

        dashboardJob = GlobalScope.launch(Dispatchers.Main) {
            ColorApiHelper.awaitSync()
            loadCourses(forceNetwork)
        }
    }

    private suspend fun loadCourses(forceNetwork: Boolean) {
        apiAsync<List<Course>> { CourseManager.getCoursesTeacher(forceNetwork, it) }.await()
            .onFailure { notifyRefreshFinished() }
            .onSuccess { courses ->
                // Make a call to get which courses are visible on the dashboard as well as their position
                loadCards(forceNetwork, courses)
            }
    }

    private suspend fun loadCards(forceNetwork: Boolean, courses: List<Course>) {
        apiAsync<List<DashboardCard>> { CourseManager.getDashboardCourses(forceNetwork, it) }.await()
            .onFailure { notifyRefreshFinished() }
            .onSuccess { dashboardCourses ->
                val courseMap = courses.associateBy { it.id }
                val validCourses = dashboardCourses.map { createCourseFromDashboardCard(it, courseMap) }
                    .filter { (it.isTeacher || it.isTA || it.isDesigner) }
                data.addOrUpdate(validCourses)
                notifyRefreshFinished()
            }
    }

    private fun createCourseFromDashboardCard(dashboardCard: DashboardCard, courseMap: Map<Long, Course>): Course {
        val course = courseMap[dashboardCard.id]
        return if (course != null) {
            course
        } else {
            Course(id = dashboardCard.id, name = dashboardCard.shortName ?: "", originalName = dashboardCard.originalName, courseCode = dashboardCard.courseCode)
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        dashboardJob?.cancel()
    }

    override fun refresh(forceNetwork: Boolean) {
        onRefreshStarted()
        dashboardJob?.cancel()
        clearData()
        loadData(forceNetwork)
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
