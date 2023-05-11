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
package com.instructure.student.features.offline.coursebrowser

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.TabDao

class CourseBrowserLocalDataSource(
    private val tabDao: TabDao,
    private val pageDao: PageDao
) {
    suspend fun getTabs(canvasContext: CanvasContext): List<Tab> {
        return tabDao.findByCourseId(canvasContext.id).map {
            it.toApiModel()
        }
    }

    suspend fun getFrontPage(canvasContext: CanvasContext): Page {
        return pageDao.getFrontPage(canvasContext.id).toApiModel()
    }
}