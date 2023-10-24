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
package com.instructure.student.features.modules.progression

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleItemSequence
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionDataSource
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionLocalDataSource
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionNetworkDataSource
import okhttp3.ResponseBody

class ModuleProgressionRepository(
    localDataSource: ModuleProgressionLocalDataSource,
    private val networkDataSource: ModuleProgressionNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val localFileDao: LocalFileDao
) : Repository<ModuleProgressionDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider) {

    suspend fun getAllModuleItems(canvasContext: CanvasContext, moduleId: Long, forceNetwork: Boolean): List<ModuleItem> {
        return dataSource().getAllModuleItems(canvasContext, moduleId, forceNetwork)
    }

    suspend fun getModuleItemSequence(canvasContext: CanvasContext, assetType: String, assetId: String, forceNetwork: Boolean): ModuleItemSequence {
        return dataSource().getModuleItemSequence(canvasContext, assetType, assetId, forceNetwork)
    }

    suspend fun getDetailedQuiz(url: String, quizId: Long, forceNetwork: Boolean): Quiz {
        return dataSource().getDetailedQuiz(url, quizId, forceNetwork)
    }

    suspend fun markAsNotDone(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return networkDataSource.markAsNotDone(canvasContext, moduleItem)
    }

    suspend fun markAsDone(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return networkDataSource.markAsDone(canvasContext, moduleItem)
    }

    suspend fun markAsRead(canvasContext: CanvasContext, moduleItem: ModuleItem): DataResult<ResponseBody> {
        return networkDataSource.markAsRead(canvasContext, moduleItem)
    }

    suspend fun getSyncedTabs(courseId: Long): Set<String> {
        val courseSyncSettings = courseSyncSettingsDao.findById(courseId)
        return courseSyncSettings?.tabs?.filter { it.value }?.keys ?: emptySet()
    }

    suspend fun getSyncedFileIds(courseId: Long): List<Long> {
        val syncedFiles = localFileDao.findByCourseId(courseId)
        return syncedFiles.map { it.id }
    }
}