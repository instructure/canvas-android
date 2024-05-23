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

package com.instructure.student.features.pages.details.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.PageFacade

class PageDetailsLocalDataSource(
    private val pageFacade: PageFacade
) : PageDetailsDataSource {

    override suspend fun getFrontPage(canvasContext: CanvasContext, forceNetwork: Boolean): DataResult<Page> {
        return pageFacade.getFrontPage(canvasContext.id)?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getPageDetails(canvasContext: CanvasContext, pageId: String, forceNetwork: Boolean): DataResult<Page> {
        return pageFacade.getPageDetails(canvasContext.id, pageId)?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }
}
