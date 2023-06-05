/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.student.features.dashboard

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.utils.NetworkStateProvider

class DashboardRepository(
    private val localDataSource: DashboardLocalDataSource,
    networkDataSource: DashboardNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    private val courseApi: CourseAPI.CoursesInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao
) : Repository<DashboardDataSource>(localDataSource, networkDataSource, networkStateProvider) {

    suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        return dataSource.getCourses(forceNetwork)
    }

    suspend fun getGroups(forceNetwork: Boolean): List<Group> {
        return dataSource.getGroups(forceNetwork)
    }

    suspend fun getDashboardCourses(forceNetwork: Boolean): List<DashboardCard> {
        val dashboardCards = dataSource.getDashboardCards(forceNetwork)
        if (isOnline()) {
            localDataSource.saveDashboardCards(dashboardCards)
        }
        return dashboardCards
    }

    suspend fun getSyncedCourseIds(): Set<Long> {
        val courseSyncSettings = courseSyncSettingsDao.findAll()
        return courseSyncSettings
            .filter { it.anySyncEnabled }
            .map { it.courseId }
            .toSet()
    }
}