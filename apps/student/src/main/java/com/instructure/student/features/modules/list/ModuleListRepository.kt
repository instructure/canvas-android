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
package com.instructure.student.features.modules.list

import com.instructure.canvasapi2.managers.graphql.ModuleItemWithCheckpoints
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.Const.REPLY_TO_ENTRY
import com.instructure.pandautils.utils.Const.REPLY_TO_TOPIC
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.list.datasource.ModuleListDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListLocalDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListNetworkDataSource

class ModuleListRepository(
    localDataSource: ModuleListLocalDataSource,
    private val networkDataSource: ModuleListNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<ModuleListDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getAllModuleObjects(
        canvasContext: CanvasContext,
        forceNetwork: Boolean
    ): DataResult<List<ModuleObject>> {
        return dataSource().getAllModuleObjects(canvasContext, forceNetwork)
    }

    suspend fun getFirstPageModuleObjects(
        canvasContext: CanvasContext,
        forceNetwork: Boolean
    ): DataResult<List<ModuleObject>> {
        return dataSource().getFirstPageModuleObjects(canvasContext, forceNetwork)
    }

    suspend fun getNextPageModuleObjects(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleObject>> {
        return networkDataSource.getNextPageModuleObjects(nextUrl, forceNetwork)
    }

    suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val tabs = dataSource().getTabs(canvasContext, forceNetwork).dataOrNull ?: emptyList()
        return tabs.filter { !(it.isExternal && it.isHidden) }
    }

    suspend fun loadCourseSettings(courseId: Long, forceNetwork: Boolean): CourseSettings? {
        return dataSource().loadCourseSettings(courseId, forceNetwork)
    }

    suspend fun getFirstPageModuleItems(
        canvasContext: CanvasContext,
        moduleId: Long,
        forceNetwork: Boolean
    ): DataResult<List<ModuleItem>> {
        return dataSource().getFirstPageModuleItems(canvasContext, moduleId, forceNetwork)
    }

    suspend fun getNextPageModuleItems(nextUrl: String, forceNetwork: Boolean): DataResult<List<ModuleItem>> {
        return networkDataSource.getNextPageModuleItems(nextUrl, forceNetwork)
    }

    suspend fun getModuleItemCheckpoints(courseId: String, forceNetwork: Boolean): List<ModuleItemWithCheckpoints> {
        return dataSource().getModuleItemCheckpoints(courseId, forceNetwork).map {
            it.copy(checkpoints = it.checkpoints.sortedBy { checkpoint ->
                when (checkpoint.tag) {
                    REPLY_TO_TOPIC -> 0
                    REPLY_TO_ENTRY -> 1
                    else -> 2
                }
            })
        }
    }
}