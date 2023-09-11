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
package com.instructure.student.features.navigation

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.navigation.datasource.NavigationDataSource
import com.instructure.student.features.navigation.datasource.NavigationLocalDataSource
import com.instructure.student.features.navigation.datasource.NavigationNetworkDataSource

class NavigationRepository(
    private val localDataSource: NavigationLocalDataSource,
    private val networkDataSource: NavigationNetworkDataSource,
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider
) : Repository<NavigationDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getCourse(courseId: Long, forceNetwork: Boolean): Course? {
        return dataSource().getCourse(courseId, forceNetwork)
    }

    suspend fun isTokenValid(): Boolean {
        try {
            val result = networkDataSource.getSelf()
            return result.isSuccess
        } catch (e: Exception) {
            return false
        }
    }
}