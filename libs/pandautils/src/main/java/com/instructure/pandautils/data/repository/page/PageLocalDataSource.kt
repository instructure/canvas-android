/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.pandautils.data.repository.page

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.room.offline.facade.PageFacade

class PageLocalDataSource(
    private val pageFacade: PageFacade
) : PageDataSource {

    override suspend fun getFrontPage(canvasContext: CanvasContext, forceRefresh: Boolean): DataResult<Page> {
        return pageFacade.getFrontPage(canvasContext.id)?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getPageDetails(canvasContext: CanvasContext, pageId: String, forceRefresh: Boolean): DataResult<Page> {
        return pageFacade.getPageDetails(canvasContext.id, pageId)?.let { DataResult.Success(it) } ?: DataResult.Fail()
    }

    override suspend fun getCoursePages(courseId: Long, forceRefresh: Boolean): DataResult<List<Page>> {
        return DataResult.Success(pageFacade.findByCourseId(courseId))
    }
}
