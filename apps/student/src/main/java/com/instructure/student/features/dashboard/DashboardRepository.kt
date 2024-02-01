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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DashboardPositions
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

class DashboardRepository(
    private val localDataSource: DashboardLocalDataSource,
    private val networkDataSource: DashboardNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val courseDao: CourseDao
) : Repository<DashboardDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        return dataSource().getCourses(forceNetwork)
    }

    suspend fun getGroups(forceNetwork: Boolean): List<Group> {
        return dataSource().getGroups(forceNetwork)
    }

    suspend fun getDashboardCourses(forceNetwork: Boolean): List<DashboardCard> {
        var dashboardCards = dataSource().getDashboardCards(forceNetwork)
        if (dashboardCards.all { it.position == Int.MAX_VALUE }) {
            dashboardCards = dashboardCards.mapIndexed { index, dashboardCard -> dashboardCard.copy(position = index) }
        }
        if (isOnline() && isOfflineEnabled()) {
            localDataSource.saveDashboardCards(dashboardCards)
        }
        return dashboardCards.sortedBy { it.position }
    }

    suspend fun getSyncedCourseIds(): Set<Long> {
        if (!isOfflineEnabled()) return emptySet()

        val courseSyncSettings = courseSyncSettingsDao.findAll()
        val syncedCourseIds = courseSyncSettings
            .filter { it.anySyncEnabled }
            .map { it.courseId }
            .toSet()

        val syncedCourses = courseDao.findByIds(syncedCourseIds)
        return syncedCourses.map { it.id }.toSet()
    }

    suspend fun updateDashboardPositions(dashboardPositions: DashboardPositions): DataResult<DashboardPositions> {
        val result = networkDataSource.updateDashboardPositions(dashboardPositions)
        if (result is DataResult.Success) {
            localDataSource.updateDashboardCardsOrder(dashboardPositions)
        }
        return networkDataSource.updateDashboardPositions(dashboardPositions)
    }
}