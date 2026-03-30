/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard.widget.course

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.offline.HorizonOfflineRepository
import com.instructure.horizon.offline.SyncPolicy
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class DashboardCourseRepository @Inject constructor(
    private val networkDataSource: DashboardCourseNetworkDataSource,
    private val localDataSource: DashboardCourseLocalDataSource,
    private val localDataSync: DashboardCourseSyncer,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : HorizonOfflineRepository<DashboardCourseDataSource>(
    localDataSource = localDataSource,
    networkDataSource = networkDataSource,
    networkStateProvider = networkStateProvider,
    featureFlagProvider = featureFlagProvider,
) {

    suspend fun getEnrollments(): List<GetCoursesQuery.Enrollment> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getEnrollments().also { enrollments ->
                if (isOfflineEnabled()) localDataSync.syncCourses(enrollments, SyncPolicy.ALWAYS_REPLACE)
            }
        } else {
            localDataSource.getEnrollments()
        }
    }

    suspend fun acceptInvite(courseId: Long, enrollmentId: Long) {
        networkDataSource.acceptInvite(courseId, enrollmentId)
    }

    suspend fun getPrograms(): List<Program> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getPrograms().also { programs ->
                if (isOfflineEnabled()) localDataSync.syncPrograms(programs, SyncPolicy.ALWAYS_REPLACE)
            }
        } else {
            localDataSource.getPrograms()
        }
    }

    suspend fun getModuleItemsForCourse(courseId: Long): List<ModuleObject> {
        return if (shouldFetchFromNetwork()) {
            networkDataSource.getModuleItemsForCourse(courseId).also { modules ->
                if (isOfflineEnabled()) localDataSync.syncModuleItem(courseId, modules, SyncPolicy.ALWAYS_REPLACE)
            }
        } else {
            localDataSource.getModuleItemsForCourse(courseId)
        }
    }

    suspend fun getLastSyncedAt(): Long? = localDataSync.getLastSyncedAt()

    private suspend fun shouldFetchFromNetwork() = isOnline() || !isOfflineEnabled()
}
