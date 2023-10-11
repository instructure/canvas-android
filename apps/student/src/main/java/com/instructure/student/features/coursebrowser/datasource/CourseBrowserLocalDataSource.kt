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
package com.instructure.student.features.coursebrowser.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.utils.orDefault

class CourseBrowserLocalDataSource(
    private val tabDao: TabDao,
    private val pageFacade: PageFacade,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val fileSyncSettingsDao: FileSyncSettingsDao
) : CourseBrowserDataSource {
    override suspend fun getTabs(canvasContext: CanvasContext, forceNetwork: Boolean): List<Tab> {
        val courseSyncSettings = courseSyncSettingsDao.findById(canvasContext.id)
        var syncedTabs = courseSyncSettings?.tabs
        val filesSynced = courseSyncSettings?.fullFileSync == true || fileSyncSettingsDao.findByCourseId(canvasContext.id).isNotEmpty()

        if (filesSynced) {
            syncedTabs = syncedTabs?.plus(Pair(Tab.FILES_ID, true))
        }

        return tabDao.findByCourseId(canvasContext.id).map {
            it.toApiModel().copy(enabled = syncedTabs?.get(it.id).orDefault())
        }
    }

    override suspend fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): Page? {
        return pageFacade.getFrontPage(canvasContext.id)
    }
}